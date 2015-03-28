package sk.mito.jp.w;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import sk.mito.jp.w.domain.User;
import sk.mito.jp.w.domain.UserDao;

public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
    private final UserDao userDao;

    public BasicAuthenticator(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * just for example purposes all user will have the same password
     * normally it would be check against a salted&hashed password
     */
    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if ("secret".equals(credentials.getPassword())) {
            User user = userDao.findByUsername(credentials.getUsername());
            if (user != null) {
                return Optional.of(user);
            }
        }
        throw new AuthenticationException(credentials.getUsername() + " is not registered");
    }
}
