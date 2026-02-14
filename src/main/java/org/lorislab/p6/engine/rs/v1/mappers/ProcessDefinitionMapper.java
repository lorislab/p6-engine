package org.lorislab.p6.engine.rs.v1.mappers;

import org.lorislab.p6.common.rs.OffsetDateTimeMapper;
import org.lorislab.p6.engine.domain.store.model.ProcessDefinition;
import org.mapstruct.Mapper;

import gen.org.lorislab.p6.engine.rs.v1.model.ProcessDefinitionDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProcessDefinitionMapper {

    ProcessDefinitionDTO map(ProcessDefinition data);
}
