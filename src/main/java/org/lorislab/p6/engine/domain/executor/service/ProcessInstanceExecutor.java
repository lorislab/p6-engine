package org.lorislab.p6.engine.domain.executor.service;

import static org.lorislab.p6.bpmn2.model.FlowElement.unwrap;
import static org.lorislab.p6.bpmn2.model.FlowElement.FlowElementClass.START_EVENT;

import java.util.ArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.model.StartEvent;
import org.lorislab.p6.engine.domain.models.EventContext;
import org.lorislab.p6.engine.domain.services.ProcessDefinitionService;
import org.lorislab.p6.engine.domain.services.StreamService;
import org.lorislab.p6.engine.domain.store.ProcessInstanceRepository;
import org.lorislab.p6.engine.domain.store.model.ProcessInstance;
import org.lorislab.p6.engine.domain.store.model.event.ProcessInstanceEvent;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ProcessInstanceExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceExecutor.class);

    @Inject
    ProcessDefinitionService processDefinitionService;

    @Inject
    StreamService streamService;

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    public Uni<Void> execute(EventContext<ProcessInstanceEvent> ctx) {
        return switch (ctx.getEvent().getStatus()) {
            case START -> startProcessInstance(ctx);
            case COMPLETE -> completeProcessInstance(ctx);
            default -> notSupported(ctx);
        };
    }

    private Uni<Void> notSupported(EventContext<ProcessInstanceEvent> ctx) {
        log.warn("Not supported status of the process instance. Status: {}", ctx.getEvent().getStatus());
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> completeProcessInstance(EventContext<ProcessInstanceEvent> ctx) {
        return processInstanceRepository.findById(ctx.getClient(), ctx.getEvent().getProcessInstanceId())
                .onItem()
                .ifNotNull()
                .transformToUni(
                        pi -> processInstanceRepository.updateStatus(ctx.getClient(), pi.getId(),
                                ProcessInstance.Status.COMPLETED))
                .invoke(() -> log.info("Process instance finished! {}", ctx.getEvent().getProcessInstanceId()));
    }

    private Uni<Void> startProcessInstance(EventContext<ProcessInstanceEvent> ctx) {

        return processDefinitionService
                .loadProcessDefinitionBy(ctx.getEvent().getProcessDefinitionId(),
                        ctx.getEvent().getProcessDefinitionResourceId())
                .onItem().transform(pd -> pd.processDef().getElements().values().stream()
                        .filter(x -> x.getFlowElementClass() == START_EVENT)
                        .map(x -> unwrap(x, StartEvent.class))
                        .toList())

                .onItem().transform(startEvents -> {
                    var tokens = new ArrayList<TokenEvent>();
                    for (var se : startEvents) {
                        var event = ctx.getEvent();
                        var token = TokenEvent.of();
                        token.setProcessInstanceId(event.getProcessInstanceId());
                        token.setProcessDefinitionId(event.getProcessDefinitionId());
                        token.setProcessDefinitionResourceId(event.getProcessDefinitionResourceId());
                        token.setProcessDefinitionVersion(event.getProcessDefinitionVersion());
                        token.setVariables(event.getVariables());
                        token.setElementId(se.getId());
                        token.setProcessVersion(event.getProcessVersion());
                        token.setProcessId(event.getProcessId());
                        token.setScopeId(event.getProcessInstanceId());
                        token.addPath(se.getId());
                        tokens.add(token);
                    }
                    return tokens;
                })

                .onItem().transformToUni(
                        tokens -> processInstanceRepository.findById(ctx.getClient(), ctx.getEvent().getProcessInstanceId())
                                .onItem().ifNotNull()
                                .transformToUni(
                                        pi -> processInstanceRepository.updateStatus(ctx.getClient(), pi.getId(),
                                                ProcessInstance.Status.ACTIVE))
                                .onItem().transformToUni(x -> streamService.insert(ctx.getClient(), tokens))
                                .replaceWithVoid());
    }

}
