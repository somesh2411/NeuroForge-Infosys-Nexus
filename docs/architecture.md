# NeuroForge Nexus Bounded Architecture

This document describes the architectural layout, technology layers, and synchronization paradigms used across the platform.

---

## 1. System Communication Models

### Synchronous Requests (REST)
All client requests route through the **Spring Cloud Gateway** (Port 8080).
The gateway inspects JWT signatures, then routes:
- `/api/v1/users/**` and `/api/v1/teams/**` to the **User Service** (Port 8081).
- `/api/v1/projects/**` and `/api/v1/milestones/**` to the **Project Service** (Port 8082).
- `/api/v1/sprints/**` to the **Sprint Service** (Port 8083).

### Asynchronous Event Replication (Kafka)
Microservices remain autonomous and decoupled by replicating essential domain references (read-models) using Kafka topics:
1. **User Service** publishes:
   - Topic `user-created` (payload containing ID, username, names).
   - Topic `team-created` (payload containing ID, name, code, lead).
   - Topic `team-deleted` (payload containing deleted team ID).
2. **Project Service** consumes:
   - Replicates user details to a local `users` read-model table in `project_db` database.
   - Replicates team details to a local `teams` read-model table in `project_db` database.
   - Publishes `project-created` topic events.
3. **Sprint Service** consumes:
   - Replicates project details to a local `projects` read-model table in `sprint_db` database.

---

## 2. Database Schema Strategy
Each microservice is bound to its own independent PostgreSQL database:
- `user_db`: Owns tables `users` and `teams`.
- `project_db`: Owns tables `projects`, `milestones`, and `project_team` (junction). Also contains light replicated cache tables for users and teams.
- `sprint_db`: Owns table `sprints`. Also contains a light replicated cache table for projects.
- `keycloak`: Keycloak identity management database.
This schema separation ensures that no service can query across contexts directly, enforcing Domain-Driven Bounded Context definitions.
