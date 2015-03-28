package sk.mito.jp.w.resources;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.caching.CacheControl;
import sk.mito.jp.w.domain.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Path("profiles")
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource extends BaseResource {

    private final UserDao userDao;
    private final ProfileDao profileDao;
    private final ProfileViewRecordDao profileViewRecordDao;

    public ProfileResource(UserDao userDao, ProfileDao profileDao, ProfileViewRecordDao profileViewRecordDao) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.profileViewRecordDao = profileViewRecordDao;
    }

    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProfile(@Auth User user, @Valid Profile profile) {
        User validUser = userDao.findByUsername(user.getUsername());
        Profile result = profileDao.createProfile(profile.setUser(validUser));
        return Response.ok(result).build();
    }

    @GET
    @UnitOfWork
    @Path("{id}")
    //a profile of a user can hardly change quicker than in a day :)
    //this should be used in a conjunction with "if-modified-since" -> http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#caching
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Response findByIdOrUsername(@Auth User user, @PathParam("id") String idOrUsername) {
        Map<String, String> object = new HashMap<>();
        Profile profile = fetchProfile(idOrUsername);
        if (profile != null) {
            //if user is watching his own profile we do not have to log it
            if (!profile.getUser().equals(user)) {
                profileViewRecordDao.createRecord(user, profile);
            }
            return Response.ok(profile).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(object).build();
    }

    private Profile fetchProfile(String id) {
        Profile profile;
        Long idIsNumber = checkIfIdIsNumber(id);
        if (idIsNumber != null) {
            profile = profileDao.findById(idIsNumber);
        } else {
            profile = profileDao.findByUsername(id);
        }
        return profile;
    }

    @GET
    @UnitOfWork
    @Path("{id}/views")
    public Response getLast10ViewsOfProfile(@Auth User user, @PathParam("id") String idOrUsername) {
        Long id = checkIfIdIsNumber(idOrUsername);
        if (id == null) {
            Profile profile = profileDao.findByUsername(idOrUsername);
            if (profile != null) {
                id = profile.getId();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        return Response.ok(profileViewRecordDao.getNewestViewRecordsForProfile(id)).build();

    }

}
