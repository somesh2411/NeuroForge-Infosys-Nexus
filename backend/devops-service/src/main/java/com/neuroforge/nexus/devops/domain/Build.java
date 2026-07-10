package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "builds")
public class Build extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;

    @Column(name = "build_number", nullable = false)
    private Integer buildNumber;

    @Column(name = "commit_id", length = 50)
    private String commitId;

    @Column(length = 50)
    private String branch;

    @Column(nullable = false, length = 50)
    private String status; // QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED

    @Column(name = "trigger_type", nullable = false, length = 50)
    private String triggerType; // MANUAL, COMMIT, SCHEDULED

    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "artifact_name")
    private String artifactName;

    @Column(name = "artifact_size")
    private Long artifactSize;

    @Column(name = "tests_total")
    private Integer testsTotal = 0;

    @Column(name = "tests_passed")
    private Integer testsPassed = 0;

    @Column(name = "tests_failed")
    private Integer testsFailed = 0;

    public Build() {}

    public Build(String id, Pipeline pipeline, Integer buildNumber, String commitId, String branch, 
                 String status, String triggerType, String triggeredBy, LocalDateTime startTime, 
                 LocalDateTime endTime, Long durationMs, String artifactName, Long artifactSize, 
                 Integer testsTotal, Integer testsPassed, Integer testsFailed) {
        this.id = id;
        this.pipeline = pipeline;
        this.buildNumber = buildNumber;
        this.commitId = commitId;
        this.branch = branch;
        this.status = status;
        this.triggerType = triggerType;
        this.triggeredBy = triggeredBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.artifactName = artifactName;
        this.artifactSize = artifactSize;
        this.testsTotal = testsTotal;
        this.testsPassed = testsPassed;
        this.testsFailed = testsFailed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
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

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public Long getArtifactSize() {
        return artifactSize;
    }

    public void setArtifactSize(Long artifactSize) {
        this.artifactSize = artifactSize;
    }

    public Integer getTestsTotal() {
        return testsTotal;
    }

    public void setTestsTotal(Integer testsTotal) {
        this.testsTotal = testsTotal;
    }

    public Integer getTestsPassed() {
        return testsPassed;
    }

    public void setTestsPassed(Integer testsPassed) {
        this.testsPassed = testsPassed;
    }

    public Integer getTestsFailed() {
        return testsFailed;
    }

    public void setTestsFailed(Integer testsFailed) {
        this.testsFailed = testsFailed;
    }

    public static BuildBuilder builder() {
        return new BuildBuilder();
    }

    public static class BuildBuilder {
        private String id;
        private Pipeline pipeline;
        private Integer buildNumber;
        private String commitId;
        private String branch;
        private String status;
        private String triggerType;
        private String triggeredBy;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private String artifactName;
        private Long artifactSize;
        private Integer testsTotal = 0;
        private Integer testsPassed = 0;
        private Integer testsFailed = 0;

        public BuildBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BuildBuilder pipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        public BuildBuilder buildNumber(Integer buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        public BuildBuilder commitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public BuildBuilder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public BuildBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BuildBuilder triggerType(String triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public BuildBuilder triggeredBy(String triggeredBy) {
            this.triggeredBy = triggeredBy;
            return this;
        }

        public BuildBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public BuildBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public BuildBuilder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public BuildBuilder artifactName(String artifactName) {
            this.artifactName = artifactName;
            return this;
        }

        public BuildBuilder artifactSize(Long artifactSize) {
            this.artifactSize = artifactSize;
            return this;
        }

        public BuildBuilder testsTotal(Integer testsTotal) {
            this.testsTotal = testsTotal;
            return this;
        }

        public BuildBuilder testsPassed(Integer testsPassed) {
            this.testsPassed = testsPassed;
            return this;
        }

        public BuildBuilder testsFailed(Integer testsFailed) {
            this.testsFailed = testsFailed;
            return this;
        }

        public Build build() {
            return new Build(id, pipeline, buildNumber, commitId, branch, status, triggerType, triggeredBy, startTime, endTime, durationMs, artifactName, artifactSize, testsTotal, testsPassed, testsFailed);
        }
    }
}
