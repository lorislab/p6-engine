package org.lorislab.p6.engine.domain.store.mapper;

import org.lorislab.quarkus.data.Mapper;

import io.vertx.mutiny.sqlclient.Row;

@Mapper
public interface StreamAttemptsDataMapper {

    org.lorislab.p6.engine.domain.store.model.StreamAttempts map(Row row);
}
