package org.lorislab.p6.engine.rs.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.lorislab.p6.engine.domain.services.ProcessInstanceService;
import org.lorislab.p6.engine.domain.services.ValueMapper;
import org.lorislab.p6.engine.domain.store.ProcessInstanceRepository;
import org.lorislab.p6.engine.rs.v1.mappers.ProcessInstanceMapper;

import gen.org.lorislab.p6.engine.rs.v1.ProcessInstanceApiService;
import gen.org.lorislab.p6.engine.rs.v1.model.CreateProcessInstanceRequestDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.CreateProcessInstanceResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ProcessInstanceDTO;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ProcessInstanceRestController implements ProcessInstanceApiService {

    @Inject
    ProcessInstanceService service;

    @Inject
    ProcessInstanceRepository repository;

    @Inject
    ProcessInstanceMapper mapper;

    @Inject
    ValueMapper valueMapper;

    @Override
    public Uni<RestResponse<CreateProcessInstanceResponseDTO>> createProcessInstance(
            CreateProcessInstanceRequestDTO dto) {
        return service.createProcessInstance(dto.getProcessId(), dto.getProcessVersion(), dto.getVariables())
                .onItem().transform(processInstance -> {
                    if (processInstance == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(mapper.response(processInstance));
                });
    }

    @Override
    public Uni<RestResponse<ProcessInstanceDTO>> getProcessInstance(String id) {
        return repository.findById(id)
                .onItem().transformToUni(data -> {
                    if (data == null) {
                        return Uni.createFrom().item(RestResponse.notFound());
                    }
                    return valueMapper.readVariables(data.getValues())
                            .map(variables -> mapper.map(data, variables))
                            .map(RestResponse::ok);
                });
    }
}
