package org.lorislab.p6.engine.rs.common;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;

import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.context.ApplicationContext;
import org.lorislab.p6.context.Context;

import io.quarkus.arc.Unremovable;

@Provider
@Unremovable
@Priority(1)
public class RestContextInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var uuid = UUID.create();

        // create an application context
        var ctx = ApplicationContext.builder(uuid)
                .build();

        // start application context
        Context.set(ctx);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        var ctx = Context.get();
        responseContext.getHeaders().add("x-p6-request-id", ctx.getUUID());
        Context.close();
    }
}
