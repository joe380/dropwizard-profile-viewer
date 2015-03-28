package sk.mito.jp.w;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ViewRecordDao {
    @SqlUpdate("update profile_view_records set created = :created where id = :id")
    void overrideTimestampOfRecord(@Bind("id") long id, @Bind("created") DateTime created);
}
