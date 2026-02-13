package org.lorislab.p6.engine.domain.models;

public record DeploymentResult(ResourceResult resource, ProcessDefinitionResult processDefinition) {

    public record ProcessDefinitionResult(String id, String processName, String processId, String processVersion,
            int version, String resourceId) {
    }

    public record ResourceResult(String id, String name, int version) {
    }
}
