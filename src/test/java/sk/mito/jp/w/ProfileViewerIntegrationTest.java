package sk.mito.jp.w;

import com.google.common.io.BaseEncoding;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.joda.time.DateTime;
import org.junit.*;
import org.skife.jdbi.v2.DBI;
import sk.mito.jp.w.config.AppConfiguration;
import sk.mito.jp.w.domain.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileViewerIntegrationTest {
    private static final String TMP_FILE = createTestDbFile();

    private static SessionFactory sessionFactory;

    private static UserDao userDao;
    private static ProfileDao profileDao;
    private static ProfileViewRecordDao profileViewRecordDao;

    //helping DAO for DB manipulation
    private static ViewRecordDao viewRecordDao;

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE = new DropwizardAppRule<>(
            ProfileViewerApplication.class, ResourceHelpers.resourceFilePath("test-configuration.yml"),
            ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE));

    @BeforeClass
    public static void migrateDb() throws Exception {
        ProfileViewerApplication profileViewerApplication = RULE.getApplication();
        sessionFactory = profileViewerApplication.hibernateBundle.getSessionFactory();
        userDao = new UserDao(sessionFactory);
        profileDao = new ProfileDao(sessionFactory);
        profileViewRecordDao = new ProfileViewRecordDao(sessionFactory);

        DBIFactory dbiFactory = new DBIFactory();
        DBI jdbi = dbiFactory.build(RULE.getEnvironment(), RULE.getConfiguration().getDataSourceFactory(), "h2");
        viewRecordDao = jdbi.onDemand(ViewRecordDao.class);
    }

    private static String createTestDbFile() {
        try {
            File f = new File("target/test-db" + System.currentTimeMillis());
            boolean created = f.createNewFile();
            if (created) {
                return f.getAbsolutePath();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException();
    }

    private Client client;

    private Session session;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient();
        session = getSession();
        ManagedSessionContext.bind(session);
        session.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        session.getTransaction().commit();
        session.close();
    }

    private Session getSession() {
        try {
            session = sessionFactory.openSession();
        } catch (SessionException se) {
            session = sessionFactory.getCurrentSession();
        }
        return session;
    }

    @Test
    public void testUserRegistration() throws Exception {
        List<User> users = userDao.findAll();
        int size = users.size();

        User user = new User("jano");
        final User saved = webTargetUsersRequest().post(Entity.json(user)).readEntity(User.class);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo(user.getUsername());
        users = userDao.findAll();
        assertThat(users.size() - size).isEqualTo(1);
    }

    //this test shows that an api call should have user parameter (in this case it's a part of authentication)
    @Test
    public void testProfileCreationWithoutUserShouldFailWith401() throws Exception {
        Profile profile = new Profile().setDescription("annemari's profile description");
        Response response = webTargetProfiles().post(Entity.json(profile));
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testProfileCreation() throws Exception {
        String username = "annemari";
        String description = "annemari's profile description";

        User saved = webTargetUsersRequest().post(Entity.json(new User(username))).readEntity(User.class);
        assertThat(saved.getUsername()).isEqualTo(username);

        Profile profile = new Profile().setDescription(description);
        Profile savedProfile = addAuthenticationHeader(webTargetProfiles(), username).post(Entity.json(profile)).readEntity(Profile.class);
        Assert.assertNotNull(savedProfile);
        Assert.assertNotNull(description, savedProfile.getDescription());
        Assert.assertEquals(saved, savedProfile.getUser());
    }

    //testing actual assignment requirements
    @Test
    public void testCreationOfViewRecords() throws Exception {
        String user1 = "user1";
        String user2 = "user2";
        String[] userNames = new String[]{user1, user2};

        Map<String, Profile> userToProfile = new HashMap<>();
        registerUsersAndCreateProfiles(userNames, userToProfile);

        //user1 viewing its' own profile 3x => no log entry should be created
        for (int i = 0; i < 3; i++) {
            addAuthenticationHeader(getProfileTarget().path(user1).request(), user1).get();
        }
        List<ProfileViewRecord> records = profileViewRecordDao.getNewestViewRecordsForProfile(userToProfile.get(user1).getId());
        assertThat(records).isEmpty();

        //user1 viewing user2 profile 5x => we are expecting log entry to be created
        for (int i = 0; i < 5; i++) {
            addAuthenticationHeader(getProfileTarget().path(user2).request(), user1).get();
        }
        records = profileViewRecordDao.getNewestViewRecordsForProfile(userToProfile.get(user2).getId());
        int size = records.size();
        assertThat(records).isNotEmpty();
        ProfileViewRecord record = records.get(0);
        record.getId();

        //override timestamp to past to check the assignment requirement that only 10 days later
        //for now only 5 days => size should not change
        viewRecordDao.overrideTimestampOfRecord(record.getId(), record.getCreated().minusDays(5));
        records = profileViewRecordDao.getNewestViewRecordsForProfile(userToProfile.get(user2).getId());
        assertThat(records).hasSize(size);
        //lets go 15 days back => size should change by -1
        viewRecordDao.overrideTimestampOfRecord(record.getId(), record.getCreated().minusDays(15));
        records = profileViewRecordDao.getNewestViewRecordsForProfile(userToProfile.get(user2).getId());
        assertThat(records).hasSize(size - 1);

        //user1 viewing user2 profile 15x => we are expecting log entry to be created
        for (int i = 0; i < 15; i++) {
            addAuthenticationHeader(getProfileTarget().path(user2).request(), user1).get();
        }
        records = profileViewRecordDao.getNewestViewRecordsForProfile(userToProfile.get(user2).getId());
        assertThat(records).isNotEmpty();
        //it should only return 10(although we called it 15x) records if the records are all younger than 10 days...which are :)
        assertThat(records).hasSize(10);

        //all view records in this test case should have "user1" as username
        DateTime entryTimeStamp = DateTime.now();
        for (ProfileViewRecord pvr : records) {
            //only other users than "user2" should be in this list
            assertThat(pvr.getUsername()).isNotEqualTo(user2);
            //descending dates
            assertThat(pvr.getCreated()).isLessThan(entryTimeStamp);
            entryTimeStamp = pvr.getCreated();
        }
    }

    private void registerUsersAndCreateProfiles(String[] userNames, Map<String, Profile> userToProfile) {
        //create users
        for (String username : userNames) {
            webTargetUsersRequest().post(Entity.json(new User(username)));
            assertThat(userDao.findByUsername(username)).isNotNull();
        }

        //create profiles
        for (String username : userNames) {
            Profile profile = new Profile().setDescription(username + ".desc");
            addAuthenticationHeader(webTargetProfiles(), username).post(Entity.json(profile));
            Profile result = profileDao.findByUsername(username);
            assertThat(result).isNotNull();
            userToProfile.put(username, result);
        }
    }

    private javax.ws.rs.client.Invocation.Builder addAuthenticationHeader(javax.ws.rs.client.Invocation.Builder builder, String username) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Basic " + BaseEncoding.base64().encode((username + ":secret").getBytes()));
    }

    private javax.ws.rs.client.Invocation.Builder webTargetUsersRequest() {
        return client.target("http://localhost:" + RULE.getLocalPort() + "/app/users").request();
    }

    private javax.ws.rs.client.Invocation.Builder webTargetProfiles() {
        return getProfileTarget().request();
    }

    private WebTarget getProfileTarget() {
        return client.target("http://localhost:" + RULE.getLocalPort() + "/app/profiles");
    }
}
