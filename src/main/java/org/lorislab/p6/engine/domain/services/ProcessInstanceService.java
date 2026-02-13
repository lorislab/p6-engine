package org.lorislab.p6.engine.domain.services;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.engine.domain.store.ProcessDefinitionRepository;
import org.lorislab.p6.engine.domain.store.ProcessInstanceRepository;
import org.lorislab.p6.engine.domain.store.model.ProcessDefinition;
import org.lorislab.p6.engine.domain.store.model.ProcessInstance;
import org.lorislab.p6.engine.domain.store.model.event.ProcessInstanceEvent;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

@ApplicationScoped
public class ProcessInstanceService {

    @Inject
    ProcessDefinitionRepository processDefinitionRepository;

    @Inject
    StreamService streamService;

    @Inject
    ProcessInstanceRepository processInstanceRepository;

    @Inject
    ValueMapper valueMapper;

    @Inject
    Pool pool;

    public Uni<ProcessInstance> createProcessInstance(String processId, String processVersion, Map<String, Object> variables) {
        return processDefinitionRepository.findByProcessId(processId, processVersion)
                .onItem().transformToUni(pd -> {
                    if (pd == null) {
                        return Uni.createFrom().nullItem();
                    }
                    return valueMapper.writeVariables(variables)
                            .onItem().transformToUni(values -> {
                                // create process instance
                                var processInstance = getProcessInstance(pd, values);
                                // create execution event
                                var event = getProcessInstanceEvent(pd, processInstance, variables);

                                return create(processInstance, event).replaceWith(processInstance);
                            });
                });
    }

    private static ProcessInstance getProcessInstance(ProcessDefinition pd, byte[] values) {
        var processInstance = new ProcessInstance();
        processInstance.setId(UUID.create());
        processInstance.setProcessId(pd.getProcessId());
        processInstance.setProcessVersion(pd.getProcessVersion());
        processInstance.setProcessDefinitionResourceId(pd.getResourceId());
        processInstance.setProcessDefinitionId(pd.getId());
        processInstance.setValues(values);
        processInstance.setStatus(ProcessInstance.Status.CREATED);
        return processInstance;
    }

    private static ProcessInstanceEvent getProcessInstanceEvent(ProcessDefinition pd, ProcessInstance processInstance,
            Map<String, Object> variables) {
        var event = new ProcessInstanceEvent();
        event.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        event.setStatus(ProcessInstanceEvent.Status.START);
        event.setProcessDefinitionResourceId(processInstance.getProcessDefinitionResourceId());
        event.setProcessDefinitionVersion(pd.getResourceVersion());
        event.setProcessInstanceId(processInstance.getId());
        event.setVariables(variables);
        event.setProcessVersion(pd.getProcessVersion());
        event.setProcessId(pd.getProcessId());
        return event;
    }

    public Uni<Void> create(ProcessInstance processInstance, ProcessInstanceEvent event) {
        return pool.withTransaction(conn -> streamService.insert(conn, event)
                .onItem().transformToUni(ignored -> processInstanceRepository.insert(conn, processInstance)));
    }

}
