package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deployments")
public class Deployment extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id")
    private Build build;

    @Column(nullable = false, length = 50)
    private String status; // PENDING, DEPLOYING, SUCCESSFUL, FAILED, ROLLED_BACK

    @Column(nullable = false, length = 50)
    private String version; // e.g. v1.0.0-build12

    @Column(name = "deployed_by", nullable = false, length = 100)
    private String deployedBy;

    @Column(name = "deployed_at", nullable = false)
    private LocalDateTime deployedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "rollback_available")
    private boolean rollbackAvailable = false;

    @Column(name = "rolled_back_from_deployment_id", length = 36)
    private String rolledBackFromDeploymentId;

    @Column(name = "rollback_reason", columnDefinition = "TEXT")
    private String rollbackReason;

    @Column(name = "deployment_log", columnDefinition = "TEXT")
    private String deploymentLog;

    public Deployment() {}

    public Deployment(String id, Environment environment, Build build, String status, String version, 
                      String deployedBy, LocalDateTime deployedAt, Long durationMs, boolean rollbackAvailable, 
                      String rolledBackFromDeploymentId, String rollbackReason, String deploymentLog) {
        this.id = id;
        this.environment = environment;
        this.build = build;
        this.status = status;
        this.version = version;
        this.deployedBy = deployedBy;
        this.deployedAt = deployedAt;
        this.durationMs = durationMs;
        this.rollbackAvailable = rollbackAvailable;
        this.rolledBackFromDeploymentId = rolledBackFromDeploymentId;
        this.rollbackReason = rollbackReason;
        this.deploymentLog = deploymentLog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isRollbackAvailable() {
        return rollbackAvailable;
    }

    public void setRollbackAvailable(boolean rollbackAvailable) {
        this.rollbackAvailable = rollbackAvailable;
    }

    public String getRolledBackFromDeploymentId() {
        return rolledBackFromDeploymentId;
    }

    public void setRolledBackFromDeploymentId(String rolledBackFromDeploymentId) {
        this.rolledBackFromDeploymentId = rolledBackFromDeploymentId;
    }

    public String getRollbackReason() {
        return rollbackReason;
    }

    public void setRollbackReason(String rollbackReason) {
        this.rollbackReason = rollbackReason;
    }

    public String getDeploymentLog() {
        return deploymentLog;
    }

    public void setDeploymentLog(String deploymentLog) {
        this.deploymentLog = deploymentLog;
    }

    public static DeploymentBuilder builder() {
        return new DeploymentBuilder();
    }

    public static class DeploymentBuilder {
        private String id;
        private Environment environment;
        private Build build;
        private String status;
        private String version;
        private String deployedBy;
        private LocalDateTime deployedAt;
        private Long durationMs;
        private boolean rollbackAvailable = false;
        private String rolledBackFromDeploymentId;
        private String rollbackReason;
        private String deploymentLog;

        public DeploymentBuilder id(String id) {
            this.id = id;
            return this;
        }

        public DeploymentBuilder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public DeploymentBuilder build(Build build) {
            this.build = build;
            return this;
        }

        public DeploymentBuilder status(String status) {
            this.status = status;
            return this;
        }

        public DeploymentBuilder version(String version) {
            this.version = version;
            return this;
        }

        public DeploymentBuilder deployedBy(String deployedBy) {
            this.deployedBy = deployedBy;
            return this;
        }

        public DeploymentBuilder deployedAt(LocalDateTime deployedAt) {
            this.deployedAt = deployedAt;
            return this;
        }

        public DeploymentBuilder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public DeploymentBuilder rollbackAvailable(boolean rollbackAvailable) {
            this.rollbackAvailable = rollbackAvailable;
            return this;
        }

        public DeploymentBuilder rolledBackFromDeploymentId(String rolledBackFromDeploymentId) {
            this.rolledBackFromDeploymentId = rolledBackFromDeploymentId;
            return this;
        }

        public DeploymentBuilder rollbackReason(String rollbackReason) {
            this.rollbackReason = rollbackReason;
            return this;
        }

        public DeploymentBuilder deploymentLog(String deploymentLog) {
            this.deploymentLog = deploymentLog;
            return this;
        }

        public Deployment build() {
            return new Deployment(id, environment, build, status, version, deployedBy, deployedAt, durationMs, rollbackAvailable, rolledBackFromDeploymentId, rollbackReason, deploymentLog);
        }
    }
}
