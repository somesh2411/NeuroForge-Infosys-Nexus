-- Pre-seed default users matching Keycloak UUIDs inside project-service local read-models
INSERT INTO users (id, username, first_name, last_name) VALUES
('e838f7d9-c000-47bf-a111-111111111111', 'admin', 'Admin', 'User'),
('e838f7d9-c000-47bf-a111-222222222222', 'lead', 'Team', 'Lead'),
('e838f7d9-c000-47bf-a111-333333333333', 'dev', 'Dev', 'User')
ON CONFLICT (id) DO NOTHING;
