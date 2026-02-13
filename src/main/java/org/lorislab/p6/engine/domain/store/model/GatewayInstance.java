package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.Column;
import org.lorislab.quarkus.data.Entity;
import org.lorislab.quarkus.data.Id;
import org.lorislab.quarkus.data.Table;

@Entity
@Table(name = "GATEWAY_INSTANCE")
public class GatewayInstance {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "SCOPE_ID")
    private String scopeId;

    @Column(name = "ELEMENT_ID")
    private String elementId;

    @Column(name = "PROCESS_INSTANCE_ID")
    private String processInstanceId;

    @Column(name = "FROM_ELEMENT_ID")
    private String fromElementId;

    @Column(name = "TOKEN")
    private byte[] token;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFromElementId() {
        return fromElementId;
    }

    public void setFromElementId(String fromElementId) {
        this.fromElementId = fromElementId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
