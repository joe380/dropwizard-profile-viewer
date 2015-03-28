package sk.mito.jp.w.domain;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class UserDao extends AbstractDAO<User> {
    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public User findByUsername(String username) {
        try {
            return uniqueResult(namedQuery(User.FIND_BY_USER_NAME).setParameter("username", username));
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> findAll() {
        return list(namedQuery(User.FIND_ALL));
    }

    public User findById(long id) {
        return get(id);
    }

    public User createUser(User user) {
        return persist(user);
    }
}
