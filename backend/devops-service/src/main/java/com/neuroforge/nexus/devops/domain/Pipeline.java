package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "pipelines")
public class Pipeline extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(length = 50)
    private String branch = "main";

    @Column(name = "build_tool", nullable = false, length = 50)
    private String buildTool; // MAVEN, GRADLE, NPM, DOCKER

    @Column(name = "pipeline_type", nullable = false, length = 50)
    private String pipelineType; // MOCK, JENKINS, GITHUB_ACTIONS

    @Column(name = "pipeline_template", length = 50)
    private String pipelineTemplate; // JAVA_MAVEN, SPRING_BOOT, ANGULAR, DOCKER

    @Column(name = "jenkins_job_name")
    private String jenkinsJobName;

    @Column(name = "github_workflow_path")
    private String githubWorkflowPath;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    @Column(length = 50)
    private String status = "IDLE"; // IDLE, RUNNING, FAILING, SUCCESSFUL

    public Pipeline() {}

    public Pipeline(String id, String name, String projectId, Repository repository, String branch, 
                    String buildTool, String pipelineType, String pipelineTemplate, 
                    String jenkinsJobName, String githubWorkflowPath, boolean enabled, String status) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.repository = repository;
        this.branch = branch;
        this.buildTool = buildTool;
        this.pipelineType = pipelineType;
        this.pipelineTemplate = pipelineTemplate;
        this.jenkinsJobName = jenkinsJobName;
        this.githubWorkflowPath = githubWorkflowPath;
        this.enabled = enabled;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getPipelineType() {
        return pipelineType;
    }

    public void setPipelineType(String pipelineType) {
        this.pipelineType = pipelineType;
    }

    public String getPipelineTemplate() {
        return pipelineTemplate;
    }

    public void setPipelineTemplate(String pipelineTemplate) {
        this.pipelineTemplate = pipelineTemplate;
    }

    public String getJenkinsJobName() {
        return jenkinsJobName;
    }

    public void setJenkinsJobName(String jenkinsJobName) {
        this.jenkinsJobName = jenkinsJobName;
    }

    public String getGithubWorkflowPath() {
        return githubWorkflowPath;
    }

    public void setGithubWorkflowPath(String githubWorkflowPath) {
        this.githubWorkflowPath = githubWorkflowPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static PipelineBuilder builder() {
        return new PipelineBuilder();
    }

    public static class PipelineBuilder {
        private String id;
        private String name;
        private String projectId;
        private Repository repository;
        private String branch = "main";
        private String buildTool;
        private String pipelineType;
        private String pipelineTemplate;
        private String jenkinsJobName;
        private String githubWorkflowPath;
        private boolean enabled = true;
        private String status = "IDLE";

        public PipelineBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PipelineBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PipelineBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public PipelineBuilder repository(Repository repository) {
            this.repository = repository;
            return this;
        }

        public PipelineBuilder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public PipelineBuilder buildTool(String buildTool) {
            this.buildTool = buildTool;
            return this;
        }

        public PipelineBuilder pipelineType(String pipelineType) {
            this.pipelineType = pipelineType;
            return this;
        }

        public PipelineBuilder pipelineTemplate(String pipelineTemplate) {
            this.pipelineTemplate = pipelineTemplate;
            return this;
        }

        public PipelineBuilder jenkinsJobName(String jenkinsJobName) {
            this.jenkinsJobName = jenkinsJobName;
            return this;
        }

        public PipelineBuilder githubWorkflowPath(String githubWorkflowPath) {
            this.githubWorkflowPath = githubWorkflowPath;
            return this;
        }

        public PipelineBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public PipelineBuilder status(String status) {
            this.status = status;
            return this;
        }

        public Pipeline build() {
            return new Pipeline(id, name, projectId, repository, branch, buildTool, pipelineType, pipelineTemplate, jenkinsJobName, githubWorkflowPath, enabled, status);
        }
    }
}
