package com.neuroforge.nexus.devops.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_stages")
public class PipelineStage {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id")
    private Build build;

    @Column(nullable = false, length = 100)
    private String name; // Build, Test, Quality, Docker, Deploy

    @Column(nullable = false, length = 50)
    private String status; // QUEUED, RUNNING, SUCCESS, FAILED

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "stage_log", columnDefinition = "TEXT")
    private String stageLog;

    public PipelineStage() {}

    public PipelineStage(String id, Build build, String name, String status, 
                         LocalDateTime startTime, LocalDateTime endTime, Long durationMs, String stageLog) {
        this.id = id;
        this.build = build;
        this.name = name;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.stageLog = stageLog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStageLog() {
        return stageLog;
    }

    public void setStageLog(String stageLog) {
        this.stageLog = stageLog;
    }

    public static PipelineStageBuilder builder() {
        return new PipelineStageBuilder();
    }

    public static class PipelineStageBuilder {
        private String id;
        private Build build;
        private String name;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private String stageLog;

        public PipelineStageBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PipelineStageBuilder build(Build build) {
            this.build = build;
            return this;
        }

        public PipelineStageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PipelineStageBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PipelineStageBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public PipelineStageBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public PipelineStageBuilder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public PipelineStageBuilder stageLog(String stageLog) {
            this.stageLog = stageLog;
            return this;
        }

        public PipelineStage build() {
            return new PipelineStage(id, build, name, status, startTime, endTime, durationMs, stageLog);
        }
    }
}
