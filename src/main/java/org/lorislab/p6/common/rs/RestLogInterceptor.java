package org.lorislab.p6.common.rs;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.LoggerFactory;

import io.quarkus.arc.Unremovable;

@Provider
@Unremovable
@Priority(2)
public class RestLogInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String CONTEXT = "p6-log-context";

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        var uriInfo = requestContext.getUriInfo();

        RestLogContext restContext = new RestLogContext();
        restContext.setLogger(resourceInfo.getResourceClass().getName());
        restContext.setMethod(requestContext.getMethod());
        restContext.setPath(uriInfo.getPath());

        requestContext.setProperty(CONTEXT, restContext);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        RestLogContext rc = (RestLogContext) requestContext.getProperty(CONTEXT);
        Response.StatusType status = responseContext.getStatusInfo();

        // close rest log context
        rc.close();

        // log
        LoggerFactory.getLogger(rc.getLogger()).info("{} {}:{} [{}s]", rc.getMethod(), rc.getPath(), status.getStatusCode(),
                rc.getDurationString());
    }
}
