package org.lorislab.p6.engine.domain.store.mapper;

import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.quarkus.data.Mapper;

import io.vertx.mutiny.sqlclient.Row;

@Mapper
public interface JobDataMapper {

    Job map(Row row);
}
