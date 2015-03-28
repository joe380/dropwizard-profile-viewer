package sk.mito.jp.w.domain;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;

import java.util.List;

public class ProfileViewRecordDao extends AbstractDAO<ProfileViewRecord> {
    public ProfileViewRecordDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public ProfileViewRecord createRecord(User viewer, Profile profile) {
        return persist(new ProfileViewRecord(viewer.getUsername(), profile.getId()));
    }

    public List<ProfileViewRecord> getNewestViewRecordsForProfile(long profileId) {
        return list(namedQuery(ProfileViewRecord.FIND_BY_PROFILE)
                .setParameter("profileId", profileId)
                .setParameter("olderThenTenDays", DateTime.now().minusDays(10))
                .setMaxResults(10));
    }
}
