package org.lorislab.p6.engine.domain.executor.service;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.model.ServiceTask;
import org.lorislab.p6.bpmn2.model.ServiceTaskExtensionElements;
import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.engine.domain.models.TokenEventContext;
import org.lorislab.p6.engine.domain.services.StreamService;
import org.lorislab.p6.engine.domain.services.ValueMapper;
import org.lorislab.p6.engine.domain.store.JobRepository;
import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ServiceTaskExecutor {

    @Inject
    StreamService streamService;

    @Inject
    JobRepository jobRepository;

    @Inject
    ValueMapper valueMapper;

    public Uni<Void> execute(TokenEventContext<ServiceTask> ctx) {
        return switch (ctx.getEvent().getPhase()) {
            case OPEN -> serviceTaskOpen(ctx);
            case CLOSE -> serviceTaskClose(ctx);
        };
    }

    private Uni<Void> serviceTaskOpen(TokenEventContext<ServiceTask> ctx) {
        var element = ctx.getElement();
        var event = ctx.getEvent();
        var ztd = element.getExtensionElements().getZeebeTaskDefinition();
        var job = new Job();
        job.setId(UUID.create());
        job.setType(ztd.getType());
        job.setStatus(Job.Status.PENDING);
        job.setRetryCount(0);
        job.setProcessDefinitionId(event.getProcessDefinitionId());
        job.setProcessDefinitionVersion(event.getProcessDefinitionVersion());
        job.setProcessInstanceId(event.getProcessInstanceId());
        job.setProcessId(event.getProcessId());
        job.setProcessVersion(event.getProcessVersion());
        job.setElementId(element.getId());
        job.setState(Job.State.CREATED);
        job.setTokenType(ctx.getStream().getType());
        job.setToken(ctx.getStream().getValue());

        return valueMapper.writeVariables(event.getVariables())
                .onItem().transformToUni(variables -> {
                    job.setVariables(variables);

                    Map<String, Object> customHeaders = null;

                    // zeebe task headers / custom headers
                    if (element.getExtensionElements().getZeebeTaskHeaders() != null
                            && element.getExtensionElements().getZeebeTaskHeaders().getHeaders() != null) {

                        customHeaders = element.getExtensionElements().getZeebeTaskHeaders()
                                .getHeaders().stream()
                                .collect(Collectors.toMap(ServiceTaskExtensionElements.ZeebeTaskHeader::getKey,
                                        ServiceTaskExtensionElements.ZeebeTaskHeader::getValue));
                    }

                    if (customHeaders != null && !customHeaders.isEmpty()) {
                        return valueMapper.write(customHeaders)
                                .invoke(job::setCustomHeaders)
                                .chain(() -> jobRepository.insert(ctx.getClient(), job));
                    }
                    return jobRepository.insert(ctx.getClient(), job);
                });

    }

    private Uni<Void> serviceTaskClose(TokenEventContext<ServiceTask> ctx) {
        var element = ctx.getElement();
        var next = element.getOutgoing().getFirst();
        var result = TokenEvent.of(ctx.getEvent());
        result.setFromElementId(element.getId());
        result.setElementId(next);
        return streamService.insert(ctx.getClient(), result);
    }
}
