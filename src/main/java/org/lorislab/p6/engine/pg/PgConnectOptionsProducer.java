package org.lorislab.p6.engine.pg;

import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.pgclient.PgConnectOptions;

public class PgConnectOptionsProducer {

    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    String url;

    @ConfigProperty(name = "quarkus.datasource.username")
    String username;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    @Produces
    public PgConnectOptions pgConnectOptions() {
        PgConnectOptions pgConnectOptions = PgConnectOptions.fromUri(url.substring("vertx-reactive:".length()));
        pgConnectOptions.setUser(username);
        pgConnectOptions.setPassword(password);
        return pgConnectOptions;
    }
}
