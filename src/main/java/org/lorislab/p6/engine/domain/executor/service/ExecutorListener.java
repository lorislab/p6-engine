package org.lorislab.p6.engine.domain.executor.service;

import java.util.stream.LongStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.StreamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.pubsub.PgSubscriber;

@ApplicationScoped
public class ExecutorListener {

    private static final Logger log = LoggerFactory.getLogger(ExecutorListener.class);

    private static final String CHANNEL_EXECUTE = "execute";

    @Inject
    Vertx vertx;

    @Inject
    PgConnectOptions pgConnectOptions;

    @Inject
    ExecutorService executorService;

    @Inject
    StreamRepository streamRepository;

    void onStart(@Observes StartupEvent ev) {
        Multi.createBy().merging().streams(findMessages(), subscriber())
                .onItem().transformToUniAndConcatenate(this::execute)
                .subscribe().with(m -> log.debug("Executed message {} ", m), Throwable::printStackTrace);
    }

    public Uni<String> execute(String event) {
        return executorService.executeEvent(event);
    }

    private Multi<String> findMessages() {
        return streamRepository.findNextExecuteCount()
                .onItem().transformToMulti(i -> {
                    if (i == 0) {
                        return Multi.createFrom().nothing();
                    }
                    return Multi.createFrom().iterable(() -> LongStream.range(0, i).iterator()).map(Object::toString);
                });
    }

    private Multi<String> subscriber() {
        return Multi.createFrom().emitter(emitter -> {
            PgSubscriber subscriber = PgSubscriber.subscriber(vertx, pgConnectOptions);
            subscriber.connect(ar -> {
                if (ar.succeeded()) {
                    log.info("PgSubscriber connected, subscribing to channel: {}", CHANNEL_EXECUTE);
                    subscriber.channel(CHANNEL_EXECUTE).handler(emitter::emit);
                } else {
                    log.error("Failed to connect PgSubscriber: {}", ar.cause().getMessage(), ar.cause());
                }
            });
        });
    }

}
