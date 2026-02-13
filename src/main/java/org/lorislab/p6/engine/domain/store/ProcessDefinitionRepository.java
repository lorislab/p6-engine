package org.lorislab.p6.engine.domain.store;

import static org.lorislab.quarkus.data.sql.Op.equal;
import static org.lorislab.quarkus.data.sql.Select.select;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.ProcessDefinitionDataMapper;
import org.lorislab.p6.engine.domain.store.model.ProcessDefinition;
import org.lorislab.p6.engine.domain.store.model.ProcessDefinition_;
import org.lorislab.quarkus.data.sql.Insert;
import org.lorislab.quarkus.data.sql.Update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ProcessDefinitionRepository extends AbstractRepository {

    @Inject
    ProcessDefinitionDataMapper mapper;

    public Uni<ProcessDefinition> findById(String id) {
        return findById(pool, id);
    }

    public Uni<ProcessDefinition> findById(SqlClient client, String id) {
        return client.preparedQuery(select().from(ProcessDefinition_.TABLE_).where(equal(ProcessDefinition_.ID)).build())
                .mapping(mapper::map)
                .execute(tuple(id))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    public Uni<ProcessDefinition> findByProcessId(String processId, String processVersion) {
        return pool
                .preparedQuery(select().from(ProcessDefinition_.TABLE_)
                        .where(equal(ProcessDefinition_.PROCESS_ID), equal(ProcessDefinition_.PROCESS_VERSION)).build())
                .mapping(mapper::map)
                .execute(tuple(processId, processVersion))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    private static final String SQL_INSERT_ALL = Insert.insert()
            .into(ProcessDefinition_.TABLE_)
            .columns(
                    ProcessDefinition_.ID, ProcessDefinition_.PROCESS_ID, ProcessDefinition_.PROCESS_VERSION,
                    ProcessDefinition_.PROCESS_NAME, ProcessDefinition_.RESOURCE_ID, ProcessDefinition_.RESOURCE_VERSION)
            .build();

    public Uni<Void> insertAll(SqlClient client, List<ProcessDefinition> resources) {
        return client.preparedQuery(SQL_INSERT_ALL)
                .executeBatch(
                        resources.stream().map(pd -> tuple(
                                pd.getId(), pd.getProcessId(), pd.getProcessVersion(), pd.getProcessName(),
                                pd.getResourceId(), pd.getResourceVersion()))
                                .toList())
                .replaceWithVoid();
    }

    private static final String SQL_UPDATE_ALL = Update.update(ProcessDefinition_.TABLE_)
            .set(
                    ProcessDefinition_.RESOURCE_ID, ProcessDefinition_.RESOURCE_VERSION, ProcessDefinition_.UPDATED_AT,
                    ProcessDefinition_.LOCK)
            .where(equal(ProcessDefinition_.ID))
            .build();

    public Uni<Void> updateAll(SqlClient client, List<ProcessDefinition> resources) {
        return client.preparedQuery(SQL_UPDATE_ALL).executeBatch(
                resources
                        .stream()
                        .map(pd -> tuple(pd.getResourceId(), pd.getResourceVersion(), pd.getUpdatedAt(), pd.getLock(),
                                pd.getId()))
                        .toList())
                .replaceWithVoid();
    }

}
