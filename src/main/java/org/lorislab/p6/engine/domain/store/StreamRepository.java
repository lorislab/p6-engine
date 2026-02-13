package org.lorislab.p6.engine.domain.store;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.StreamDataMapper;
import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.Stream_;
import org.lorislab.quarkus.data.sql.Insert;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class StreamRepository extends AbstractRepository {

    @Inject
    StreamDataMapper mapper;

    private static final String SQL_FIND_NEXT_COUNT = """
              SELECT count(se.id)
              FROM stream se
              WHERE NOT EXISTS ( SELECT 1 FROM stream_attempts a WHERE a.event_id = se.id )
            """;

    public Uni<Long> findNextExecuteCount() {
        return pool.preparedQuery(SQL_FIND_NEXT_COUNT)
                .execute()
                .map(RowSet::iterator)
                .map(i -> i.hasNext() ? i.next().getLong(0) : 0);
    }

    private static final String SQL_FIND_NEXT = """
              SELECT se.*
              FROM stream se
              WHERE NOT EXISTS ( SELECT 1 FROM stream_attempts a WHERE a.event_id = se.id )
              ORDER BY se.created_at
              FOR UPDATE SKIP LOCKED
              LIMIT 1
            """;

    public Uni<Stream> findNextExecute(SqlClient client) {
        return client.preparedQuery(SQL_FIND_NEXT)
                .mapping(mapper::map)
                .execute()
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    private static final String SQL_INSERT = Insert.insert()
            .columns(Stream_.TYPE, Stream_.VALUE, Stream_.PARENT_ID)
            .into(Stream_.TABLE_)
            .build();

    public Uni<Void> insert(SqlClient client, Stream data) {
        return client.preparedQuery(SQL_INSERT)
                .execute(tuple(data.getType(), data.getValue(), data.getParentId()))
                .replaceWithVoid();
    }

    private static final String SQL_INSERT_ALL = SQL_INSERT;

    public Uni<Void> insertAll(SqlClient client, List<Stream> data) {
        return client.preparedQuery(SQL_INSERT_ALL)
                .executeBatch(
                        data.stream()
                                .map(e -> tuple(e.getType(), e.getValue(), e.getParentId()))
                                .toList())
                .replaceWithVoid();
    }

    private static final String SQL_INSERT_RETRY = Insert.insert()
            .columns(Stream_.TYPE, Stream_.VALUE, Stream_.PARENT_ID, Stream_.ATTEMPT_NO)
            .into(Stream_.TABLE_)
            .build();

    public Uni<Void> insertRetry(SqlClient client, Stream data) {
        return client.preparedQuery(SQL_INSERT_RETRY)
                .execute(tuple(data.getType(), data.getValue(), data.getParentId(), data.getAttemptNo() + 1))
                .replaceWithVoid();
    }

}
