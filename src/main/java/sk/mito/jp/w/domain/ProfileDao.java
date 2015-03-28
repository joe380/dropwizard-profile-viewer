package sk.mito.jp.w.domain;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class ProfileDao extends AbstractDAO<Profile> {
    public ProfileDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Profile findById(long id) {
        return get(id);
    }

    public Profile findByUsername(String username) {
        try {
            return uniqueResult(namedQuery(Profile.FIND_BY_USERNAME).setParameter("username", username));
        } catch (Exception e) {
            return null;
        }
    }

    public Profile createProfile(Profile profile) {
        return persist(profile);
    }
}
