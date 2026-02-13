package org.lorislab.p6.engine.domain.store;

import static org.lorislab.quarkus.data.sql.Op.equal;
import static org.lorislab.quarkus.data.sql.Select.select;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.ResourceDataDataMapper;
import org.lorislab.p6.engine.domain.store.model.ResourceData;
import org.lorislab.p6.engine.domain.store.model.ResourceData_;
import org.lorislab.quarkus.data.sql.Insert;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ResourceDataRepository extends AbstractRepository {

    @Inject
    ResourceDataDataMapper mapper;

    public Uni<ResourceData> findById(String id) {
        return findById(pool, id);
    }

    public Uni<ResourceData> findById(SqlClient client, String id) {
        return client.preparedQuery(select().from(ResourceData_.TABLE_).where(equal(ResourceData_.ID)).build())
                .mapping(mapper::map)
                .execute(tuple(id))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    private static final String SQL_INSERT = Insert.insert()
            .into(ResourceData_.TABLE_)
            .columns(ResourceData_.ID, ResourceData_.DATA)
            .build();

    public Uni<Void> insertAll(SqlClient client, List<ResourceData> resources) {
        return client.preparedQuery(SQL_INSERT)
                .executeBatch(
                        resources
                                .stream()
                                .map(data -> tuple(data.getId(), data.getData()))
                                .toList())
                .replaceWithVoid();
    }

}
