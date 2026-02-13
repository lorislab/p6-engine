package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.Column;
import org.lorislab.quarkus.data.Entity;
import org.lorislab.quarkus.data.Id;
import org.lorislab.quarkus.data.Table;

@Entity
@Table(name = "RESOURCE")
public class Resource {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DEPLOYMENT_REQUEST_ID")
    private String deploymentRequestId;

    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "CHECKSUM")
    private Long checksum;

    @Column(name = "VERSION")
    private int version;

    @Column(name = "PROCESS_DEFINITION_ID")
    private String processDefinitionId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeploymentRequestId() {
        return deploymentRequestId;
    }

    public void setDeploymentRequestId(String deploymentRequestId) {
        this.deploymentRequestId = deploymentRequestId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getChecksum() {
        return checksum;
    }

    public void setChecksum(Long checksum) {
        this.checksum = checksum;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
