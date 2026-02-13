package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.*;

@Entity
@Table(name = "PROCESS_INSTANCE")
public class ProcessInstance {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @Column(name = "PROCESS_DEFINITION_ID")
    private String processDefinitionId;

    @Column(name = "PROCESS_DEFINITION_RESOURCE_ID")
    private String processDefinitionResourceId;

    @Column(name = "VALUES")
    private byte[] values;

    @Column(name = "STATUS", enumType = EnumType.STRING)
    private Status status;

    private Integer lock;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionResourceId() {
        return processDefinitionResourceId;
    }

    public void setProcessDefinitionResourceId(String processDefinitionResourceId) {
        this.processDefinitionResourceId = processDefinitionResourceId;
    }

    public byte[] getValues() {
        return values;
    }

    public void setValues(byte[] values) {
        this.values = values;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getLock() {
        return lock;
    }

    public void setLock(Integer lock) {
        this.lock = lock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum Status {

        CREATED,

        ACTIVE,

        COMPLETED,

        CANCELED,

        INCIDENT,
    }
}
