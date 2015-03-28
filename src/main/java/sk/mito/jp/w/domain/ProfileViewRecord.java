package sk.mito.jp.w.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profile_view_records", indexes = {
        @Index(columnList = "profile_id", name = "profile_record_index"),
        @Index(columnList = "created", name = "profile_record_index")
})
@NamedQueries({
        @NamedQuery(name = ProfileViewRecord.FIND_BY_PROFILE, query = "select pvr from ProfileViewRecord pvr where pvr.profileId=:profileId and pvr.created > :olderThenTenDays order by pvr.created desc")
})
public class ProfileViewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FIND_BY_PROFILE = "ProfileViewRecord.findByProfile";

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "seqProfileViewRecord", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "seqProfileViewRecord", sequenceName = "profile_view_rec_seq")
    private long id;

    @Column(name = "user_name", length = 50, nullable = false)
    private String username;

    //this is only a log entry and doesn't need to have a foreign key /reference to actual profile
    //we are referring to this records through existing profile
    //we only need last 10days view records..., older records should be deleted in a scheduled task run once a day
    @Column(name = "profile_id", nullable = false)
    private long profileId;

    @Column
    private DateTime created;

    ProfileViewRecord() {
        //jpa
    }

    public ProfileViewRecord(String username, long profileId) {
        this.username = username;
        this.profileId = profileId;
        this.created = DateTime.now();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @JsonProperty("user_uri")
    public String getUserUri() {
        return "/users/" + username;
    }

    @JsonProperty("profile_uri")
    public String getProfileUri() {
        return "/profiles/" + profileId;
    }

    public DateTime getCreated() {
        return created;
    }
}
