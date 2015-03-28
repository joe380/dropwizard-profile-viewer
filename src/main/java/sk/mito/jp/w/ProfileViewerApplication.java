package sk.mito.jp.w;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;
import sk.mito.jp.w.config.AppConfiguration;
import sk.mito.jp.w.domain.*;
import sk.mito.jp.w.resources.ProfileResource;
import sk.mito.jp.w.resources.UserResource;

public class ProfileViewerApplication extends Application<AppConfiguration> {
    final HibernateBundle<AppConfiguration> hibernateBundle = new HibernateBundle<AppConfiguration>(User.class, Profile.class, ProfileViewRecord.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    final MigrationsBundle<AppConfiguration> migrationsBundle = new MigrationsBundle<AppConfiguration>() {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "profile-viewer-application";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(migrationsBundle);
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) throws Exception {
        SessionFactory sessionFactory = hibernateBundle.getSessionFactory();
        UserDao userDao = new UserDao(sessionFactory);
        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new AppExceptionMapper());
        jersey.register(new UserResource(userDao));
        jersey.register(new ProfileResource(userDao, new ProfileDao(sessionFactory), new ProfileViewRecordDao(sessionFactory)));
        jersey.register(AuthFactory.binder(new BasicAuthFactory<>(new BasicAuthenticator(userDao), "SUPER SECRET STUFF", User.class)));
    }

    public static void main(String[] args) throws Exception {
        new ProfileViewerApplication().run(args);
    }
}
