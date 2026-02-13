package org.lorislab.p6.engine.pg;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.agroal.runtime.AgroalDataSourceUtil;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.runtime.DataSourcesRuntimeConfig;
import io.vertx.pgclient.PgConnectOptions;

public class PgConnectOptionsProducer {

    private static final Logger log = LoggerFactory.getLogger(PgConnectOptionsProducer.class);

    @Inject
    DataSourcesRuntimeConfig dataSourcesRuntimeConfig;

    @Produces
    public PgConnectOptions pgConnectOptions() {

        var ads = AgroalDataSourceUtil.dataSourceIfActive(DataSourceUtil.DEFAULT_DATASOURCE_NAME).orElse(null);
        if (ads == null) {
            log.error("Missing datasource: {}", DataSourceUtil.DEFAULT_DATASOURCE_NAME);
            throw new RuntimeException("Missing default database configuration!");
        }

        var cfc = ads.getConfiguration().connectionPoolConfiguration().connectionFactoryConfiguration();
        var url = cfc.jdbcUrl().replaceFirst("jdbc:", "");

        var dsc = dataSourcesRuntimeConfig.dataSources().get(DataSourceUtil.DEFAULT_DATASOURCE_NAME);
        var username = dsc.username().orElse(null);
        if (username == null) {
            log.error("Missing datasource {} username configuration!", DataSourceUtil.DEFAULT_DATASOURCE_NAME);
            throw new RuntimeException("Missing default database username configuration!");
        }
        var password = dsc.password().orElse(null);
        if (password == null) {
            log.error("Missing datasource {} password configuration!", DataSourceUtil.DEFAULT_DATASOURCE_NAME);
            throw new RuntimeException("Missing default database password configuration!");
        }

        log.info("PgConnectOptions [ username:{}, password:{}, url:{} ]", username, password, url);

        PgConnectOptions pgConnectOptions = PgConnectOptions.fromUri(url);
        pgConnectOptions.setUser(username);
        pgConnectOptions.setPassword(password);
        return pgConnectOptions;
    }
}
