package org.lorislab.p6.engine.rs.v1.mappers;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.lorislab.p6.engine.rs.v1.model.ProblemDetailErrorDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ProblemDetailsDTO;

@Mapper
public interface ExceptionMapper {

    default Response constraint(ConstraintViolationException ex) {
        var data = new ProblemDetailsDTO();
        data.status(Response.Status.BAD_REQUEST.getStatusCode());
        data.setCode("VALIDATION_ERROR");
        data.setDetail(ex.getMessage());
        data.setErrors(createProblemDetailErrors(ex.getConstraintViolations()));
        return Response.status(Response.Status.BAD_REQUEST).entity(data).build();
    }

    List<ProblemDetailErrorDTO> createProblemDetailErrors(Set<ConstraintViolation<?>> constraintViolation);

    @Mapping(target = "name", source = "propertyPath")
    @Mapping(target = "detail", source = "message")
    @Mapping(target = "code", constant = "PROPERTY_ERROR")
    ProblemDetailErrorDTO createProblemDetailError(ConstraintViolation<?> constraintViolation);

    default String mapPath(Path path) {
        return path.toString();
    }
}
