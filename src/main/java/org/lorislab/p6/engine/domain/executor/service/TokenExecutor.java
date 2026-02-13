package org.lorislab.p6.engine.domain.executor.service;

import static org.lorislab.p6.bpmn2.model.FlowElement.unwrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.model.*;
import org.lorislab.p6.engine.domain.models.EventContext;
import org.lorislab.p6.engine.domain.models.ProcessDefinitionData;
import org.lorislab.p6.engine.domain.models.TokenEventContext;
import org.lorislab.p6.engine.domain.services.ProcessDefinitionService;
import org.lorislab.p6.engine.domain.services.StreamService;
import org.lorislab.p6.engine.domain.store.model.event.ProcessInstanceEvent;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class TokenExecutor {

    private static final Logger log = LoggerFactory.getLogger(TokenExecutor.class);

    @Inject
    ProcessDefinitionService processDefinitionService;

    @Inject
    StreamService streamService;

    @Inject
    ServiceTaskExecutor serviceTaskExecutor;

    @Inject
    ParallelGatewayExecutor parallelGatewayExecutor;

    @Inject
    ScriptTaskExecutor scriptTaskExecutor;

    public Uni<Void> execute(EventContext<TokenEvent> ctx) {
        var event = ctx.getEvent();
        return processDefinitionService
                .loadProcessDefinitionBy(event.getProcessDefinitionId(), event.getProcessDefinitionResourceId())
                .onItem().ifNull().continueWith(() -> {
                    log.warn("ProcessDefinition {} not found", event.getProcessDefinitionId());
                    return null;
                })
                .onItem().transformToUni(pd -> {
                    if (pd == null) {
                        return Uni.createFrom().voidItem();
                    }

                    var element = pd.processDef().getElements().get(event.getElementId());
                    if (element == null) {
                        return Uni.createFrom().voidItem();
                    }
                    return handleElement(ctx, pd, pd.processDef().getElements().get(event.getElementId()));
                });
    }

    private Uni<Void> handleElement(EventContext<TokenEvent> ctx, ProcessDefinitionData pd, FlowElement<?> element) {

        return switch (element.getFlowElementClass()) {
            case START_EVENT -> startEvent(TokenEventContext.of(ctx, pd, unwrap(element, StartEvent.class)));
            case END_EVENT -> endEvent(TokenEventContext.of(ctx, pd, unwrap(element, EndEvent.class)));
            case SEQUENCE_FLOW -> sequenceFlow(TokenEventContext.of(ctx, pd, unwrap(element, SequenceFlow.class)));
            case PARALLEL_GATEWAY ->
                parallelGatewayExecutor.execute(TokenEventContext.of(ctx, pd, unwrap(element, ParallelGateway.class)));
            case SERVICE_TASK -> serviceTaskExecutor.execute(TokenEventContext.of(ctx, pd, unwrap(element, ServiceTask.class)));
            case SCRIPT_TASK -> scriptTaskExecutor.execute(TokenEventContext.of(ctx, pd, unwrap(element, ScriptTask.class)));
            default -> notSupported(TokenEventContext.of(ctx, pd, element));
        };
    }

    private Uni<Void> notSupported(TokenEventContext<?> ctx) {
        log.warn("Not supported element class: {}", ctx.getElement().getFlowElementClass());
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> sequenceFlow(TokenEventContext<SequenceFlow> ctx) {
        var element = ctx.getElement();
        var next = element.getTargetRef();
        var result = TokenEvent.of(ctx.getEvent());
        result.setFromElementId(element.getId());
        result.setElementId(next);
        return streamService.insert(ctx.getClient(), result);
    }

    private Uni<Void> endEvent(TokenEventContext<EndEvent> ctx) {
        var token = ctx.getEvent();
        var result = new ProcessInstanceEvent();
        result.setProcessDefinitionId(token.getProcessDefinitionId());
        result.setProcessDefinitionResourceId(token.getProcessDefinitionResourceId());
        result.setProcessInstanceId(token.getProcessInstanceId());
        result.setVariables(token.getVariables());
        result.setStatus(ProcessInstanceEvent.Status.COMPLETE);
        return streamService.insert(ctx.getClient(), result);
    }

    private Uni<Void> startEvent(TokenEventContext<StartEvent> ctx) {
        var next = ctx.getElement().getOutgoing().getFirst();
        var result = TokenEvent.of(ctx.getEvent());
        result.setElementId(next);
        return streamService.insert(ctx.getClient(), result);
    }

}
