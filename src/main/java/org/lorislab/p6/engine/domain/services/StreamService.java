package org.lorislab.p6.engine.domain.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.engine.domain.store.StreamRepository;
import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.event.Event;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class StreamService {

    @Inject
    StreamRepository repository;

    @Inject
    ValueMapper mapper;

    public <E extends Event> Uni<Void> insert(SqlClient client, E event) {
        return map(event)
                .chain(mapped -> repository.insert(client, mapped));
    }

    public <E extends Event> Uni<Void> insert(SqlClient client, List<E> events) {
        return Multi.createFrom().iterable(events)
                .onItem().transformToUniAndMerge(this::map)
                .collect().asList()
                .chain(m -> repository.insertAll(client, m).replaceWithVoid());
    }

    private <E extends Event> Uni<Stream> map(E event) {
        return mapper.write(event)
                .map(bytes -> {
                    Stream e = new Stream();
                    e.setParentId(UUID.create());
                    e.setType(event.getClass().getSimpleName());
                    e.setValue(bytes);
                    return e;
                });
    }
}
