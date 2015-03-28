package sk.mito.jp.w;

import io.dropwizard.auth.basic.BasicAuthFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class AppExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        //little hack due to some buggy behavior in dropwizard / or I just did not find the place how to register auth exception mapper
        Map<String, String> object = new HashMap<>();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement stackItem = stackTrace[0];
            if (BasicAuthFactory.class.getName().endsWith(stackItem.getClassName())) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(object).build();
            }
        }
        object.put("message", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(object).build();
    }
}
