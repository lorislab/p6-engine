package org.lorislab.p6.engine.rs.v1.mappers;

import java.util.Map;

import org.lorislab.p6.common.rs.OffsetDateTimeMapper;
import org.lorislab.p6.engine.domain.store.model.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.lorislab.p6.engine.rs.v1.model.CreateProcessInstanceResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ProcessInstanceDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProcessInstanceMapper {

    @Mapping(target = "processInstanceId", source = "id")
    CreateProcessInstanceResponseDTO response(ProcessInstance processInstance);

    @Mapping(target = "removeVariablesItem", ignore = true)
    ProcessInstanceDTO map(ProcessInstance model, Map<String, Object> variables);

}
