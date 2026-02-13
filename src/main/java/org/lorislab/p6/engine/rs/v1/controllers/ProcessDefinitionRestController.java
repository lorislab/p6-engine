package org.lorislab.p6.engine.rs.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.lorislab.p6.engine.domain.store.ProcessDefinitionRepository;
import org.lorislab.p6.engine.domain.store.ResourceDataRepository;
import org.lorislab.p6.engine.rs.v1.mappers.ProcessDefinitionMapper;

import gen.org.lorislab.p6.engine.rs.v1.ProcessDefinitionApiService;
import gen.org.lorislab.p6.engine.rs.v1.model.ProcessDefinitionDTO;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ProcessDefinitionRestController implements ProcessDefinitionApiService {

    @Inject
    ProcessDefinitionMapper mapper;

    @Inject
    ProcessDefinitionRepository processDefinitionRepository;

    @Inject
    ResourceDataRepository resourceDataRepository;

    @Override
    public Uni<RestResponse<ProcessDefinitionDTO>> getProcessDefinition(String id) {
        return processDefinitionRepository.findById(id)
                .onItem().transform(data -> {
                    if (data == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(mapper.map(data));
                });
    }

    @Override
    public Uni<RestResponse<String>> getProcessDefinitionXml(String id) {
        return processDefinitionRepository.findById(id)
                .onItem().transformToUni(pd -> {
                    if (pd == null) {
                        return Uni.createFrom().item(RestResponse.notFound());
                    }
                    return resourceDataRepository.findById(pd.getResourceId())
                            .onItem().transform(r -> {
                                if (r == null) {
                                    return RestResponse.notFound();
                                }
                                return RestResponse.ok(new String(r.getData()));
                            });
                });
    }
}
