package org.lorislab.p6.engine.domain.store.model.event;

import java.util.*;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TokenEvent implements Event {

    private String elementId;

    private String fromElementId;

    private String processInstanceId;

    private String processId;

    private String processVersion;

    private String processDefinitionResourceId;

    private String processDefinitionId;

    private int processDefinitionVersion;

    private Map<String, Object> variables;

    private Phase phase;

    private Set<String> path;

    private String scopeId;

    public static TokenEvent of() {
        var result = new TokenEvent();
        result.setPath(new HashSet<>());
        result.setPhase(Phase.OPEN);
        return result;
    }

    public static TokenEvent of(TokenEvent token) {
        var result = of();
        result.setProcessInstanceId(token.getProcessInstanceId());
        result.setProcessDefinitionId(token.getProcessDefinitionId());
        result.setProcessDefinitionVersion(token.getProcessDefinitionVersion());
        result.setProcessDefinitionResourceId(token.getProcessDefinitionResourceId());
        result.setVariables(token.getVariables());
        result.setProcessId(token.getProcessId());
        result.setProcessVersion(token.getProcessVersion());
        result.setFromElementId(token.getFromElementId());
        result.setScopeId(token.getScopeId());
        result.setPath(token.getPath());
        return result;
    }

    public void addPath(String path) {
        this.path.add(path);
    }

    public Set<String> getPath() {
        return path;
    }

    public void setPath(Set<String> path) {
        this.path = path;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getFromElementId() {
        return fromElementId;
    }

    public void setFromElementId(String fromElementId) {
        this.fromElementId = fromElementId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
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

    public enum Phase {

        OPEN,

        CLOSE,
    }

    public static class Scope {

        private String id;

        private String elementId;

        public Scope(String id, String elementId) {
            this.id = id;
            this.elementId = elementId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getElementId() {
            return elementId;
        }

        public void setElementId(String elementId) {
            this.elementId = elementId;
        }
    }
}
