package org.lorislab.p6.engine.domain.executor.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.model.ParallelGateway;
import org.lorislab.p6.engine.domain.models.TokenEventContext;
import org.lorislab.p6.engine.domain.services.StreamService;
import org.lorislab.p6.engine.domain.services.ValueMapper;
import org.lorislab.p6.engine.domain.store.GatewayInstanceRepository;
import org.lorislab.p6.engine.domain.store.model.GatewayInstance;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ParallelGatewayExecutor {

    @Inject
    GatewayInstanceRepository gatewayInstanceRepository;

    @Inject
    StreamService streamService;

    @Inject
    ValueMapper valueMapper;

    public Uni<Void> execute(TokenEventContext<ParallelGateway> ctx) {
        return switch (ctx.getEvent().getPhase()) {
            case OPEN -> open(ctx);
            case CLOSE -> close(ctx);
        };
    }

    private Uni<Void> close(TokenEventContext<ParallelGateway> ctx) {

        var element = ctx.getElement();
        var incoming = element.getIncoming();

        return gatewayInstanceRepository
                .findByScopeCount(ctx.getClient(), ctx.getEvent().getScopeId(), ctx.getEvent().getProcessInstanceId(),
                        element.getId())
                .onItem().transformToUni(count -> {
                    if (count != incoming.size()) {
                        return Uni.createFrom().nullItem();
                    }
                    return gatewayInstanceRepository
                            .findByScope(ctx.getClient(), ctx.getEvent().getScopeId(), ctx.getEvent().getProcessInstanceId(),
                                    element.getId())
                            .onItem().transformToUni(items -> createTokens(ctx, items));
                });
    }

    private Uni<Void> createTokens(TokenEventContext<ParallelGateway> ctx, List<GatewayInstance> items) {
        var elementId = ctx.getElement().getId();

        return Multi.createFrom().iterable(items)
                .onItem().transformToUniAndConcatenate(token -> valueMapper.read(token.getToken(), TokenEvent.class))
                .collect().asList()
                .onItem().transformToUni(tokens -> {
                    var token = tokens.getFirst();
                    token.setFromElementId(elementId);

                    for (var item : tokens) {
                        token.getVariables().putAll(item.getVariables());
                        token.getPath().addAll(item.getPath());
                    }

                    List<TokenEvent> events = ctx.getElement().getOutgoing().stream().map(next -> {
                        var result = TokenEvent.of(token);
                        result.setElementId(next);
                        return result;
                    }).toList();

                    return streamService.insert(ctx.getClient(), events);
                });

    }

    private Uni<Void> open(TokenEventContext<ParallelGateway> ctx) {
        var event = ctx.getEvent();
        event.setPhase(TokenEvent.Phase.CLOSE);

        var gi = new GatewayInstance();
        gi.setScopeId(event.getScopeId());
        gi.setElementId(event.getElementId());
        gi.setFromElementId(event.getFromElementId());
        gi.setProcessInstanceId(event.getProcessInstanceId());
        gi.setToken(ctx.getStream().getValue());

        return gatewayInstanceRepository.insert(ctx.getClient(), gi)
                .chain(x -> streamService.insert(ctx.getClient(), event));
    }
}
