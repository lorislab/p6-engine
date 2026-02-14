package org.lorislab.p6.engine.domain.store.model.event;

import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ProcessInstanceEvent implements Event {

    private String processInstanceId;

    private String processId;

    private String processVersion;

    private Status status;

    private String processDefinitionResourceId;

    private String processDefinitionId;

    private int processDefinitionVersion;

    private Map<String, Object> variables;

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getProcessDefinitionResourceId() {
        return processDefinitionResourceId;
    }

    public void setProcessDefinitionResourceId(String processDefinitionResourceId) {
        this.processDefinitionResourceId = processDefinitionResourceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public enum Status {

        START,

        COMPLETE,

        CANCEL,

        INCIDENT,
    }
}
