package org.lorislab.p6.engine.domain.store;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.GatewayInstanceDataMapper;
import org.lorislab.p6.engine.domain.store.model.GatewayInstance;
import org.lorislab.p6.engine.domain.store.model.GatewayInstance_;
import org.lorislab.quarkus.data.sql.Insert;
import org.lorislab.quarkus.data.sql.Op;
import org.lorislab.quarkus.data.sql.Select;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class GatewayInstanceRepository extends AbstractRepository {

    @Inject
    GatewayInstanceDataMapper mapper;

    private static final String SQL_INSERT = Insert.insert()
            .into(GatewayInstance_.TABLE_)
            .columns(GatewayInstance_.ELEMENT_ID, GatewayInstance_.PROCESS_INSTANCE_ID, GatewayInstance_.FROM_ELEMENT_ID,
                    GatewayInstance_.SCOPE_ID, GatewayInstance_.TOKEN)
            .build();

    public Uni<Void> insert(SqlClient client, GatewayInstance gatewayInstance) {
        return client.preparedQuery(SQL_INSERT)
                .execute(tuple(
                        gatewayInstance.getElementId(), gatewayInstance.getProcessInstanceId(),
                        gatewayInstance.getFromElementId(), gatewayInstance.getScopeId(),
                        gatewayInstance.getToken()))
                .replaceWithVoid();
    }

    private static final String SQL_FIND_BY_SCOPE = Select.select()
            .from(GatewayInstance_.TABLE_)
            .where(Op.equal(GatewayInstance_.SCOPE_ID), Op.equal(GatewayInstance_.PROCESS_INSTANCE_ID),
                    Op.equal(GatewayInstance_.ELEMENT_ID))
            .build();

    public Uni<List<GatewayInstance>> findByScope(SqlClient client, String scopeId, String processInstanceId,
            String elementId) {
        return client.preparedQuery(SQL_FIND_BY_SCOPE)
                .mapping(mapper::map)
                .execute(tuple(scopeId, processInstanceId, elementId))
                .map(results -> results.stream().toList());

    }

    private static final String SQL_FIND_BY_SCOPE_COUNT = Select.select(Select.count(GatewayInstance_.ID))
            .from(GatewayInstance_.TABLE_)
            .where(Op.equal(GatewayInstance_.SCOPE_ID), Op.equal(GatewayInstance_.PROCESS_INSTANCE_ID),
                    Op.equal(GatewayInstance_.ELEMENT_ID))
            .build();

    public Uni<Long> findByScopeCount(SqlClient client, String scopeId, String processInstanceId, String elementId) {
        return client.preparedQuery(SQL_FIND_BY_SCOPE_COUNT)
                .execute(tuple(scopeId, processInstanceId, elementId))
                .map(RowSet::iterator)
                .map(i -> i.hasNext() ? i.next().getLong(0) : 0);

    }
}
