package org.lorislab.p6.engine.domain.executor.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.models.EventContext;
import org.lorislab.p6.engine.domain.services.ValueMapper;
import org.lorislab.p6.engine.domain.store.StreamAttemptsRepository;
import org.lorislab.p6.engine.domain.store.StreamRepository;
import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.event.ProcessInstanceEvent;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ExecutorService {

    private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);

    @Inject
    ValueMapper valueMapper;

    @Inject
    ProcessInstanceExecutor processInstanceExecutor;

    @Inject
    TokenExecutor tokenExecutor;

    @Inject
    Pool pool;

    @Inject
    StreamRepository streamRepository;

    @Inject
    StreamAttemptsRepository streamAttemptsRepository;

    public Uni<String> executeEvent(String eventId) {
        return executeEventTx(eventId)
                .onFailure().recoverWithUni(ex -> {
                    log.error("Transaction exception for eventId {}", eventId, ex);
                    return Uni.createFrom().item(eventId);
                });

    }

    public Uni<String> executeEventTx(String eventId) {
        return pool.withTransaction(conn -> streamRepository.findNextExecute(conn)
                .onItem().transformToUni(event -> {

                    if (event == null) {
                        log.info("No execute event found for eventId {}", eventId);
                        return Uni.createFrom().voidItem();
                    }

                    log.debug("Execute event {} {}", event.getId(), event.getType());

                    return executeByType(conn, event)
                            .onItem().transformToUni(v -> streamAttemptsRepository.insertOk(conn, event.getId()))
                            .onFailure().recoverWithUni(ex -> {
                                log.error("Error execute event {} {}", event.getId(), event.getType(), ex);
                                return streamRepository.insertRetry(conn, event)
                                        .onItem().transformToUni(
                                                v -> streamAttemptsRepository.insertError(conn, event.getId(), ex.toString()));
                            });
                })).replaceWith(eventId);
    }

    private Uni<Void> executeByType(SqlClient client, Stream event) {
        if (ProcessInstanceEvent.class.getSimpleName().equals(event.getType())) {
            return valueMapper.read(event, ProcessInstanceEvent.class)
                    .chain(e -> processInstanceExecutor.execute(EventContext.of(event, client, e)));
        }
        if (TokenEvent.class.getSimpleName().equals(event.getType())) {
            return valueMapper.read(event, TokenEvent.class)
                    .chain(e -> tokenExecutor.execute(EventContext.of(event, client, e)));
        }
        return Uni.createFrom().voidItem();
    }
}
