package com.neuroforge.nexus.devops.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    private String id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditEvent() {}

    public AuditEvent(String id, String eventType, String message, String actor, LocalDateTime timestamp) {
        this.id = id;
        this.eventType = eventType;
        this.message = message;
        this.actor = actor;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    public static class AuditEventBuilder {
        private String id;
        private String eventType;
        private String message;
        private String actor;
        private LocalDateTime timestamp;

        public AuditEventBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AuditEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public AuditEventBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AuditEventBuilder actor(String actor) {
            this.actor = actor;
            return this;
        }

        public AuditEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(id, eventType, message, actor, timestamp);
        }
    }
}
