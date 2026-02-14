package org.lorislab.p6.engine.rs.v1.mappers;

import java.nio.file.Files;
import java.util.List;

import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.lorislab.p6.common.rs.OffsetDateTimeMapper;
import org.lorislab.p6.engine.domain.models.DeploymentResource;
import org.lorislab.p6.engine.domain.models.DeploymentResult;
import org.lorislab.p6.engine.domain.store.model.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.lorislab.p6.engine.rs.v1.model.DeploymentResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.DeploymentResultDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ResourceDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ResourceMapper {

    ResourceDTO map(Resource data);

    default byte[] loadContent(FileUpload upload) {
        try {
            return Files.readAllBytes(upload.filePath());
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    default List<DeploymentResource> request(List<FileUpload> resources) {
        if (resources == null) {
            return List.of();
        }
        return resources.stream()
                .map(r -> new DeploymentResource(r.name(), r.fileName(), loadContent(r))).toList();
    }

    @Mapping(target = "removeDeploymentsItem", ignore = true)
    DeploymentResponseDTO response(String id, List<DeploymentResult> deployments);

    DeploymentResultDTO map(DeploymentResult result);

    class Error extends RuntimeException {
        public Error(Throwable throwable) {
            super(throwable);
        }
    }
}
