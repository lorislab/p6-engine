package org.lorislab.p6.engine.domain.store;

import jakarta.enterprise.context.ApplicationScoped;

import org.lorislab.p6.engine.domain.store.model.StreamAttempts;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class StreamAttemptsRepository extends AbstractRepository {

    private static final String SQL_INSERT_ERROR = """
            INSERT INTO stream_attempts (event_id, status, processor_id, error) VALUES ($1, $2, $3, $4)
            """;

    public Uni<Void> insertError(SqlClient client, Long eventId, String error) {
        return client.preparedQuery(SQL_INSERT_ERROR)
                .execute(tuple(eventId, StreamAttempts.Status.ER, "--", error))
                .replaceWithVoid();
    }

    private static final String SQL_INSERT_OK = """
            INSERT INTO stream_attempts (event_id, status, processor_id) VALUES ($1, $2, $3)
            """;

    public Uni<Void> insertOk(SqlClient client, Long eventId) {
        return client.preparedQuery(SQL_INSERT_OK)
                .execute(tuple(eventId, StreamAttempts.Status.OK, "--"))
                .replaceWithVoid();
    }

}
