package sk.mito.jp.w.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "profiles")
@NamedQueries({
        @NamedQuery(name = Profile.FIND_BY_USERNAME, query = "select p from Profile p join p.user u where u.username = :username")
})
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FIND_BY_USERNAME = "Profile.findByUsername";

    @Id
    @Column(nullable = false)
    private long id;

    @Size(min = 10, max = 255)
    @Column(name = "user_description")
    private String description;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    /**
     * profile in this case represents details about user like 'gender(enum)', 'marital status(enum)', 'schools the user attended()', etc...
     * for now the profile has only one attribute 'description' which can be anything you want
     */

    public Profile() {
        //jpa
    }

    public Profile(User user) {
        setUser(user);
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Profile setDescription(String description) {
        this.description = description;
        return this;
    }

    public Profile setUser(User user) {
        this.user = user;
        if (user != null) {
            this.id = user.getId();
        } else {
            this.id = 0;
        }
        return this;
    }

    /**
     * this method could also return a url like /users/{idOrUsername}
     * since this is a profile which should gave us all information => I leave the user here to see all its' attributes
     */
    public User getUser() {
        return user;
    }
}
