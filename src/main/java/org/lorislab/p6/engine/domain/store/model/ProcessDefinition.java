package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.Column;
import org.lorislab.quarkus.data.Entity;
import org.lorislab.quarkus.data.Id;
import org.lorislab.quarkus.data.Table;

@Entity
@Table(name = "PROCESS_DEFINITION")
public class ProcessDefinition {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @Column(name = "PROCESS_NAME")
    private String processName;

    @Column(name = "RESOURCE_ID")
    private String resourceId;

    @Column(name = "RESOURCE_VERSION")
    private int resourceVersion;

    private Integer lock;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
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

    public int getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(int resourceVersion) {
        this.resourceVersion = resourceVersion;
    }
}
