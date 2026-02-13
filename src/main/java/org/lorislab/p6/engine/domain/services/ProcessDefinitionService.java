package org.lorislab.p6.engine.domain.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.def.ProcessDef;
import org.lorislab.p6.engine.domain.models.ProcessDefinitionData;
import org.lorislab.p6.engine.domain.store.ProcessDefinitionRepository;
import org.lorislab.p6.engine.domain.store.ResourceDataRepository;

import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ProcessDefinitionService {

    @Inject
    ProcessDefinitionRepository processDefinitionRepository;

    @Inject
    ResourceDataRepository resourceDataRepository;

    @Inject
    BpmnService bpmnService;

    @CacheResult(cacheName = "process-definition")
    public Uni<ProcessDefinitionData> loadProcessDefinitionBy(String processDefinitionGuid,
            String resourceGuid) {

        return processDefinitionRepository.findById(processDefinitionGuid)
                .onItem().ifNotNull().transformToUni(pd -> resourceDataRepository.findById(resourceGuid)
                        .onItem().ifNotNull().transformToUni(resource -> bpmnService.loadDefinitions(resource.getData())
                                .map(definitions -> new ProcessDefinitionData(pd,
                                        ProcessDef.of(definitions, pd.getProcessId())))));
    }

}
