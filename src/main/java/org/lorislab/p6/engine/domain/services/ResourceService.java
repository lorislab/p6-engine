package org.lorislab.p6.engine.domain.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.bpmn2.def.ProcessDef;
import org.lorislab.p6.bpmn2.model.Process;
import org.lorislab.p6.common.checksum.Checksum;
import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.engine.domain.models.DeploymentResource;
import org.lorislab.p6.engine.domain.models.DeploymentResult;
import org.lorislab.p6.engine.domain.store.ProcessDefinitionRepository;
import org.lorislab.p6.engine.domain.store.ResourceDataRepository;
import org.lorislab.p6.engine.domain.store.ResourceRepository;
import org.lorislab.p6.engine.domain.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    @Inject
    Pool pool;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    ResourceDataRepository resourceDataRepository;

    @Inject
    ProcessDefinitionRepository processDefinitionRepository;

    @Inject
    BpmnService bpmnService;

    public Uni<List<DeploymentResult>> deployResource(String requestId,
            List<DeploymentResource> resources) {

        var result = new DeployResourceItems(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());

        return Multi.createFrom().iterable(resources)
                .onItem().transformToUniAndConcatenate(resource -> deployResource(requestId, result, resource))
                .collect().last()
                .onItem().transformToUni(x -> {
                    if (!result.resources.isEmpty()) {
                        return update(pool, result)
                                .replaceWith(result.deployments);
                    }
                    return Uni.createFrom().item(result.deployments);
                });
    }

    public Uni<Void> update(SqlClient client, DeployResourceItems result) {
        return Uni.combine().all().unis(
                resourceRepository.insertAll(client, result.resources),
                resourceDataRepository.insertAll(client, result.resourceData),
                result.pdCreate.isEmpty()
                        ? Uni.createFrom().voidItem()
                        : processDefinitionRepository.insertAll(client, result.pdCreate),
                result.pdUpdate.isEmpty()
                        ? Uni.createFrom().voidItem()
                        : processDefinitionRepository.updateAll(client, result.pdUpdate))
                .discardItems();
    }

    private Uni<Void> deployResource(String requestId, DeployResourceItems result, DeploymentResource input) {

        return bpmnService.loadDefinitions(input.data())
                .onItem().ifNull().continueWith(() -> {
                    log.error("No process definitions found!");
                    return null;
                })
                .onItem().transformToUni(definitions -> {

                    var processes = definitions.getRootElement().getProcess();
                    if (processes.size() != 1) {
                        log.error("Too many processes {}", processes.size());
                        return Uni.createFrom().nullItem();
                    }

                    var process = processes.getFirst();
                    var processDef = ProcessDef.of(definitions, process);
                    long checksum = Checksum.checksum(input.data());

                    return processDefinitionRepository
                            .findByProcessId(processDef.getProcess().getId(), processDef.getVersion())
                            .onItem().transformToUni(existing -> {
                                if (existing != null) {
                                    return handleUpdate(requestId, result, input, existing, process, processDef, checksum);
                                }
                                return handleCreate(requestId, result, input, process, processDef, checksum);
                            });
                });

    }

    private Uni<Void> handleUpdate(String requestId,
            DeployResourceItems result,
            DeploymentResource input,
            ProcessDefinition processDefinition,
            Process process,
            ProcessDef processDef,
            long checksum) {

        return resourceRepository.findById(processDefinition.getResourceId())
                .onItem().ifNull().continueWith(() -> {
                    log.error("Process definition without resource. PD: {}", processDefinition.getId());
                    return null;
                })
                .onItem().transformToUni(lastResource -> {
                    if (lastResource == null || lastResource.getChecksum() == checksum) {
                        if (lastResource != null) {
                            log.warn("Checksum equal. File {}", input.filename());
                        }
                        return Uni.createFrom().voidItem();
                    }

                    var now = LocalDateTime.now();

                    var resource = new Resource();
                    resource.setId(UUID.create());
                    resource.setChecksum(checksum);
                    resource.setFilename(input.filename());
                    resource.setVersion(processDefinition.getResourceVersion() + 1);
                    resource.setDeploymentRequestId(requestId);
                    resource.setProcessDefinitionId(processDefinition.getId());

                    var resourceData = new ResourceData();
                    resourceData.setId(resource.getId());
                    resourceData.setData(input.data());

                    processDefinition.setResourceId(resource.getId());
                    processDefinition.setResourceVersion(resource.getVersion());
                    processDefinition.setUpdatedAt(now);

                    result.pdUpdate.add(processDefinition);
                    result.resources.add(resource);
                    result.resourceData.add(resourceData);

                    var pdResult = new DeploymentResult.ProcessDefinitionResult(
                            processDefinition.getId(),
                            process.getName(),
                            process.getId(),
                            processDef.getVersion(),
                            resource.getVersion(),
                            processDefinition.getResourceId());

                    var rResult = new DeploymentResult.ResourceResult(
                            resource.getId(),
                            resource.getFilename(),
                            resource.getVersion());

                    result.deployments.add(new DeploymentResult(rResult, pdResult));

                    return Uni.createFrom().voidItem();
                });
    }

    private Uni<Void> handleCreate(String requestId,
            DeployResourceItems result,
            DeploymentResource input,
            Process process,
            ProcessDef processDef,
            long checksum) {

        var resource = new Resource();
        resource.setId(UUID.create());
        resource.setChecksum(checksum);
        resource.setFilename(input.filename());
        resource.setDeploymentRequestId(requestId);
        resource.setVersion(1);

        var resourceData = new ResourceData();
        resourceData.setId(resource.getId());
        resourceData.setData(input.data());

        var processDefinition = new ProcessDefinition();
        processDefinition.setId(UUID.create());
        processDefinition.setProcessId(process.getId());
        processDefinition.setProcessVersion(processDef.getVersion());
        processDefinition.setProcessName(process.getName());
        processDefinition.setResourceId(resource.getId());
        processDefinition.setResourceVersion(1);

        resource.setProcessDefinitionId(processDefinition.getId());

        result.pdCreate.add(processDefinition);
        result.resources.add(resource);
        result.resourceData.add(resourceData);

        var pdResult = new DeploymentResult.ProcessDefinitionResult(
                processDefinition.getId(),
                process.getName(),
                process.getId(),
                processDef.getVersion(),
                resource.getVersion(),
                processDefinition.getResourceId());

        var rResult = new DeploymentResult.ResourceResult(
                resource.getId(),
                resource.getFilename(),
                resource.getVersion());

        result.deployments.add(new DeploymentResult(rResult, pdResult));

        return Uni.createFrom().voidItem();
    }

    public record DeployResourceItems(List<DeploymentResult> deployments, List<Resource> resources,
            List<ResourceData> resourceData, List<ProcessDefinition> pdCreate, List<ProcessDefinition> pdUpdate) {
    }
}
