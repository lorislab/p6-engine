package org.lorislab.p6.engine.domain.store;

import static org.lorislab.quarkus.data.sql.Op.equal;
import static org.lorislab.quarkus.data.sql.Select.select;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.ResourceDataMapper;
import org.lorislab.p6.engine.domain.store.model.Resource;
import org.lorislab.p6.engine.domain.store.model.Resource_;
import org.lorislab.quarkus.data.sql.Insert;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ResourceRepository extends AbstractRepository {

    @Inject
    ResourceDataMapper mapper;

    public Uni<Resource> findById(String id) {
        return findById(pool, id);
    }

    public Uni<Resource> findById(SqlClient client, String id) {
        return client.preparedQuery(select().from(Resource_.TABLE_).where(equal(Resource_.ID)).build())
                .mapping(mapper::map)
                .execute(tuple(id))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    private static final String SQL_INSERT = Insert.insert()
            .into(Resource_.TABLE_)
            .columns(
                    Resource_.ID, Resource_.DEPLOYMENT_REQUEST_ID, Resource_.FILENAME, Resource_.CHECKSUM,
                    Resource_.PROCESS_DEFINITION_ID, Resource_.VERSION)
            .build();

    public Uni<Void> insertAll(SqlClient client, List<Resource> resources) {
        return client.preparedQuery(SQL_INSERT)
                .executeBatch(
                        resources
                                .stream()
                                .map(resource -> tuple(
                                        resource.getId(), resource.getDeploymentRequestId(), resource.getFilename(),
                                        resource.getChecksum(), resource.getProcessDefinitionId(), resource.getVersion()))
                                .toList())
                .replaceWithVoid();
    }

}
