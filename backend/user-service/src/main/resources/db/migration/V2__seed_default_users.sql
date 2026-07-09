-- Pre-seed default users matching Keycloak UUIDs in user-service
INSERT INTO users (id, username, email, first_name, last_name, created_at, created_by, is_deleted) VALUES
('e838f7d9-c000-47bf-a111-111111111111', 'admin', 'admin@neuroforge.com', 'Admin', 'User', CURRENT_TIMESTAMP, 'SYSTEM', FALSE),
('e838f7d9-c000-47bf-a111-222222222222', 'lead', 'lead@neuroforge.com', 'Team', 'Lead', CURRENT_TIMESTAMP, 'SYSTEM', FALSE),
('e838f7d9-c000-47bf-a111-333333333333', 'dev', 'dev@neuroforge.com', 'Dev', 'User', CURRENT_TIMESTAMP, 'SYSTEM', FALSE)
ON CONFLICT (id) DO NOTHING;
