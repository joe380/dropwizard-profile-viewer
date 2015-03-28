package sk.mito.jp.w.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "user_name", name = "user_name_index")
})
@NamedQueries({
        @NamedQuery(name = User.FIND_BY_USER_NAME, query = "select e from User e where e.username = :username"),
        @NamedQuery(name = User.FIND_ALL, query = "select e from User e")
})
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FIND_BY_USER_NAME = "User.findByUserName";
    public static final String FIND_ALL = "User.findAll";

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "seqUser", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seqUser", sequenceName = "user_seq")
    private long id;

    @Size(min = 3, max = 50)
    @Column(name = "user_name", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * here would be other user attributes like 'password'
     * more user specific stuff which would not be part of the profile
     */
    User() {
        //jpa
    }

    public User(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return !(username != null ? !username.equals(user.username) : user.username != null);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
