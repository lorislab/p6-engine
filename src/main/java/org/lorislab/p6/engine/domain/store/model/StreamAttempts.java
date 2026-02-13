package org.lorislab.p6.engine.domain.store.model;

import java.time.LocalDateTime;

import org.lorislab.quarkus.data.*;

@Entity
@Table(name = "STREAM_ATTEMPTS")
public class StreamAttempts {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "STATUS", enumType = EnumType.STRING)
    private Status status;

    @Column(name = "ERROR")
    private String error;

    @Column(name = "PROCESS_ID")
    private Long processorId;

    @Column(name = "ATTEMPT_AT")
    private LocalDateTime attemptAt;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public LocalDateTime getAttemptAt() {
        return attemptAt;
    }

    public void setAttemptAt(LocalDateTime attemptAt) {
        this.attemptAt = attemptAt;
    }

    public enum Status {
        OK,
        ER;
    }
}
