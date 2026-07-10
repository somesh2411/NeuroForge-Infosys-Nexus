-- Initial Database Schema for devops-service

CREATE TABLE environments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE repositories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    default_branch VARCHAR(50) DEFAULT 'main',
    last_commit_id VARCHAR(50),
    last_commit_message TEXT,
    last_commit_author VARCHAR(100),
    last_commit_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE pipelines (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    repository_id VARCHAR(36) REFERENCES repositories(id),
    branch VARCHAR(50) DEFAULT 'main',
    build_tool VARCHAR(50) NOT NULL,
    pipeline_type VARCHAR(50) NOT NULL, -- MOCK, JENKINS, GITHUB_ACTIONS
    pipeline_template VARCHAR(50), -- JAVA_MAVEN, SPRING_BOOT, ANGULAR, DOCKER
    jenkins_job_name VARCHAR(255),
    github_workflow_path VARCHAR(255),
    is_enabled BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'IDLE',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE builds (
    id VARCHAR(36) PRIMARY KEY,
    pipeline_id VARCHAR(36) REFERENCES pipelines(id),
    build_number INTEGER NOT NULL,
    commit_id VARCHAR(50),
    branch VARCHAR(50),
    status VARCHAR(50) NOT NULL, -- QUEUED, RUNNING, SUCCESS, FAILED, CANCELLED
    trigger_type VARCHAR(50) NOT NULL, -- MANUAL, COMMIT, SCHEDULED
    triggered_by VARCHAR(100) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    artifact_name VARCHAR(255),
    artifact_size BIGINT,
    tests_total INTEGER DEFAULT 0,
    tests_passed INTEGER DEFAULT 0,
    tests_failed INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE pipeline_stages (
    id VARCHAR(36) PRIMARY KEY,
    build_id VARCHAR(36) REFERENCES builds(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- Build, Test, Quality, Docker, Deploy
    status VARCHAR(50) NOT NULL, -- QUEUED, RUNNING, SUCCESS, FAILED
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    stage_log TEXT
);

CREATE TABLE deployments (
    id VARCHAR(36) PRIMARY KEY,
    environment_id VARCHAR(36) REFERENCES environments(id),
    build_id VARCHAR(36) REFERENCES builds(id),
    status VARCHAR(50) NOT NULL, -- PENDING, DEPLOYING, SUCCESSFUL, FAILED, ROLLED_BACK
    version VARCHAR(50) NOT NULL,
    deployed_by VARCHAR(100) NOT NULL,
    deployed_at TIMESTAMP NOT NULL,
    duration_ms BIGINT,
    rollback_available BOOLEAN DEFAULT FALSE,
    rolled_back_from_deployment_id VARCHAR(36),
    rollback_reason TEXT,
    deployment_log TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE releases (
    id VARCHAR(36) PRIMARY KEY,
    version VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    release_notes TEXT,
    build_id VARCHAR(36) REFERENCES builds(id),
    status VARCHAR(50) NOT NULL, -- DRAFT, TESTING, APPROVED, RELEASED, ARCHIVED
    released_by VARCHAR(100),
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE audit_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Seed Initial Environments
INSERT INTO environments (id, name, description, is_enabled, created_at, created_by) VALUES
('env-dev', 'Development', 'Local integrated environment for active dev branches.', true, NOW(), 'SYSTEM'),
('env-qa', 'QA Testing', 'Testing sandbox for automated integration checks.', true, NOW(), 'SYSTEM'),
('env-staging', 'Staging', 'Pre-production replica cluster environment.', true, NOW(), 'SYSTEM'),
('env-prod', 'Production', 'High-availability customer deployment servers.', true, NOW(), 'SYSTEM');
