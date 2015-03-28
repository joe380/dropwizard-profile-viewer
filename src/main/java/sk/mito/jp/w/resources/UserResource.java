package sk.mito.jp.w.resources;

import io.dropwizard.hibernate.UnitOfWork;
import sk.mito.jp.w.domain.User;
import sk.mito.jp.w.domain.UserDao;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {
    private final UserDao userDao;

    public UserResource(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * \@PUT could be also used (for save or update...)
     * through filters/interceptors the PUT method can be mapped to POST
     */
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(@Valid User user) {
        Map<String, String> object = new HashMap<>();
        try {
            User userCheck = userDao.findByUsername(user.getUsername());
            if (userCheck == null) {
                return Response.ok((userDao.createUser(user))).build();
            }
            //409: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10
            object.put("message", "user already exists");
            return Response.status(Response.Status.CONFLICT).entity(object).build();
        } catch (Exception e) {
            //this could also be handled by an ExceptionMapper(ExceptionHandler)
            object.put("message", e.getMessage());
            return Response.serverError().entity(object).build();
        }
    }

    @GET
    @UnitOfWork
    public Response fetchUsers() {
        Map<String, String> object = new HashMap<>();
        try {
            return Response.ok(userDao.findAll()).build();
        } catch (Exception e) {
            object.put("message", e.getMessage());
            return Response.serverError().entity(object).build();
        }
    }

    /**
     * this method accepts a string as a path parameter => number or string is accepted =>
     * /users/0 and /users/{username} are both working
     */
    @GET
    @UnitOfWork
    @Path("{id}")
    public Response findByIdOrUsername(@PathParam("id") String idOrUsername) {
        Map<String, String> object = new HashMap<>();
        try {
            User user = fetchUser(idOrUsername);
            if (user != null) {
                return Response.ok(user).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(object).build();
        } catch (Exception e) {
            object.put("message", e.getMessage());
            return Response.serverError().entity(object).build();
        }
    }

    private User fetchUser(String id) {
        User user;
        Long userId = checkIfIdIsNumber(id);
        if (userId != null) {
            user = userDao.findById(userId);
        } else {
            user = userDao.findByUsername(id);
        }
        return user;
    }


}
