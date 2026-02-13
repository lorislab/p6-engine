package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.*;

@Entity
@Table(name = "STREAM")
public class Stream {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PARENT_ID")
    private String parentId;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "VALUE")
    private byte[] value;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "ATTEMPT_NO")
    private Integer attemptNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
    }
}
