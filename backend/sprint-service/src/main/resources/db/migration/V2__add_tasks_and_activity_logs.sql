-- Replicated read-model for Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100)
);

-- Core Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(50) PRIMARY KEY,
    sprint_id VARCHAR(50),
    project_id VARCHAR(50) NOT NULL,
    title VARCHAR(250) NOT NULL,
    description TEXT,
    assigned_developer_id VARCHAR(50),
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(50) NOT NULL DEFAULT 'TO_DO', -- TO_DO, IN_PROGRESS, CODE_REVIEW, TESTING, DONE
    story_points INT NOT NULL DEFAULT 1,
    due_date TIMESTAMP,
    labels VARCHAR(500),
    estimated_hours DOUBLE PRECISION DEFAULT 0.0,
    actual_hours DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    version INT NOT NULL DEFAULT 0, -- For optimistic locking
    CONSTRAINT fk_task_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_task_developer FOREIGN KEY (assigned_developer_id) REFERENCES users(id)
);

-- Task Blockers Table
CREATE TABLE IF NOT EXISTS blockers (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, RESOLVED
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT fk_blocker_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Task Comments Table
CREATE TABLE IF NOT EXISTS task_comments (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    author_username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Activity Logs Table
CREATE TABLE IF NOT EXISTS activity_logs (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50),
    sprint_id VARCHAR(50),
    event_type VARCHAR(100) NOT NULL, -- TASK_CREATED, TASK_ASSIGNED, TASK_STATUS_CHANGED, BLOCKER_ADDED, BLOCKER_RESOLVED, COMMENT_ADDED, SPRINT_STARTED, SPRINT_COMPLETED
    message TEXT NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
