package org.lorislab.p6.engine.domain.models;

import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.event.Event;

import io.vertx.mutiny.sqlclient.SqlClient;

public class EventContext<T extends Event> {

    private final Stream stream;

    private final SqlClient client;

    private final T event;

    public static <E extends Event> EventContext<E> of(Stream stream, SqlClient client, E event) {
        return new EventContext<>(stream, client, event);
    }

    public EventContext(Stream stream, SqlClient client, T event) {
        this.stream = stream;
        this.client = client;
        this.event = event;
    }

    public Stream getStream() {
        return stream;
    }

    public SqlClient getClient() {
        return client;
    }

    public T getEvent() {
        return event;
    }
}
