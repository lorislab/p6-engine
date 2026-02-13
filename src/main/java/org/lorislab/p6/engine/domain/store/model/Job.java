package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.*;

@Entity
@Table(name = "JOB")
public class Job {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "WORKER")
    private String worker;

    @Column(name = "STATUS", enumType = EnumType.STRING)
    private Status status;

    @Column(name = "VARIABLES")
    private byte[] variables;

    @Column(name = "OUTPUT")
    private byte[] output;

    @Column(name = "CUSTOM_HEADERS")
    private byte[] customHeaders;

    @Column(name = "RETRY_COUNT")
    private int retryCount;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "LOCK_TO")
    private LocalDateTime lockTo;

    @Column(name = "TAKE_FROM")
    private LocalDateTime takeFrom;

    @Column(name = "LOCK")
    private Integer lock;

    @Column(name = "LOCK_KEY")
    private String lockKey;

    @Column(name = "PROCESS_DEFINITION_ID")
    private String processDefinitionId;

    @Column(name = "PROCESS_DEFINITION_VERSION")
    private int processDefinitionVersion;

    @Column(name = "PROCESS_INSTANCE_ID")
    private String processInstanceId;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "PROCESS_VERSION")
    private String processVersion;

    @Column(name = "ELEMENT_ID")
    private String elementId;

    @Column(name = "ERROR_CODE")
    private String errorCode;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "STATE", enumType = EnumType.STRING)
    private State state;

    @Column(name = "TOKEN")
    private byte[] token;

    @Column(name = "TOKEN_TYPE")
    private String tokenType;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public byte[] getVariables() {
        return variables;
    }

    public void setVariables(byte[] variables) {
        this.variables = variables;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
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

    public LocalDateTime getLockTo() {
        return lockTo;
    }

    public void setLockTo(LocalDateTime lockTo) {
        this.lockTo = lockTo;
    }

    public Integer getLock() {
        return lock;
    }

    public void setLock(Integer lock) {
        this.lock = lock;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public byte[] getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(byte[] customHeaders) {
        this.customHeaders = customHeaders;
    }

    public byte[] getOutput() {
        return output;
    }

    public void setOutput(byte[] output) {
        this.output = output;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public LocalDateTime getTakeFrom() {
        return takeFrom;
    }

    public void setTakeFrom(LocalDateTime takeFrom) {
        this.takeFrom = takeFrom;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public enum Status {

        PENDING,

        IN_PROGRESS,

        DONE,

    }

    public enum State {

        CREATED,

        COMPLETED,

        CANCELED,

        ERROR_THROWN,

        FAILED,

    }
}
