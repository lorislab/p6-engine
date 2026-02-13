package org.lorislab.p6.engine.domain.models;

import org.lorislab.p6.bpmn2.def.ProcessDef;
import org.lorislab.p6.engine.domain.store.model.ProcessDefinition;

public record ProcessDefinitionData(ProcessDefinition processDefinition, ProcessDef processDef) {

}
