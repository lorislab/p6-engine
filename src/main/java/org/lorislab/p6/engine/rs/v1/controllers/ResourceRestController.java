package org.lorislab.p6.engine.rs.v1.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.lorislab.p6.common.uuid.UUID;
import org.lorislab.p6.engine.domain.services.ResourceService;
import org.lorislab.p6.engine.domain.store.ResourceDataRepository;
import org.lorislab.p6.engine.domain.store.ResourceRepository;
import org.lorislab.p6.engine.rs.v1.mappers.ResourceMapper;

import gen.org.lorislab.p6.engine.rs.v1.ResourceApiService;
import gen.org.lorislab.p6.engine.rs.v1.model.DeploymentResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ResourceDTO;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ResourceRestController implements ResourceApiService {

    @Inject
    ResourceService resourceService;

    @Inject
    ResourceMapper mapper;

    @Inject
    ResourceDataRepository resourceDataRepository;

    @Inject
    ResourceRepository resourceRepository;

    @Override
    public Uni<RestResponse<DeploymentResponseDTO>> deployResource(List<FileUpload> resources) {
        var requestId = UUID.create();
        return resourceService.deployResource(requestId, mapper.request(resources))
                .onItem().transform(response -> {
                    if (response == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(mapper.response(requestId, response));
                });
    }

    @Override
    public Uni<RestResponse<ResourceDTO>> getResource(String id) {
        return resourceRepository.findById(id)
                .onItem().transform(entity -> {
                    if (entity == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(mapper.map(entity));
                });
    }

    @Override
    public Uni<RestResponse<String>> getResourceXml(String id) {
        return resourceDataRepository.findById(id)
                .onItem().transform(entity -> {
                    if (entity == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(new String(entity.getData()));
                });
    }

}
