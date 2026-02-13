package org.lorislab.p6.engine.domain.store;

import static org.lorislab.quarkus.data.sql.Op.equal;
import static org.lorislab.quarkus.data.sql.Select.select;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.ProcessInstanceDataMapper;
import org.lorislab.p6.engine.domain.store.model.ProcessInstance;
import org.lorislab.p6.engine.domain.store.model.ProcessInstance_;
import org.lorislab.quarkus.data.sql.Insert;
import org.lorislab.quarkus.data.sql.Op;
import org.lorislab.quarkus.data.sql.Update;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ProcessInstanceRepository extends AbstractRepository {

    @Inject
    ProcessInstanceDataMapper mapper;

    public Uni<ProcessInstance> findById(String id) {
        return findById(pool, id);
    }

    public Uni<ProcessInstance> findById(SqlClient client, String id) {
        return client.preparedQuery(select().from(ProcessInstance_.TABLE_).where(equal(ProcessInstance_.ID)).build())
                .mapping(mapper::map)
                .execute(tuple(id))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    private static final String SQL_UPDATE_STATUS = Update.update(ProcessInstance_.TABLE_)
            .set(ProcessInstance_.STATUS)
            .where(Op.equal(ProcessInstance_.ID_))
            .build();

    public Uni<Void> updateStatus(SqlClient client, String id, ProcessInstance.Status status) {
        return client.preparedQuery(SQL_UPDATE_STATUS).execute(tuple(status, id)).replaceWithVoid();
    }

    private static final String SQL_INSERT = Insert.insert()
            .into(ProcessInstance_.TABLE_)
            .columns(ProcessInstance_.ID_, ProcessInstance_.PROCESS_ID, ProcessInstance_.PROCESS_VERSION,
                    ProcessInstance_.PROCESS_DEFINITION_ID, ProcessInstance_.PROCESS_DEFINITION_RESOURCE_ID,
                    ProcessInstance_.STATUS, ProcessInstance_.VALUES)
            .build();

    public Uni<Void> insert(SqlClient client, ProcessInstance pi) {
        return client.preparedQuery(SQL_INSERT)
                .execute(tuple(
                        pi.getId(), pi.getProcessId(), pi.getProcessVersion(),
                        pi.getProcessDefinitionId(), pi.getProcessDefinitionResourceId(),
                        pi.getStatus(), pi.getValues()))
                .replaceWithVoid();
    }

}
