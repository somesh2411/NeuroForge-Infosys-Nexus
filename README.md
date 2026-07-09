# NeuroForge Nexus
### Enterprise Cloud-Native Software Development Lifecycle (SDLC) Management Platform

NeuroForge Nexus is a cloud-native enterprise platform that manages the complete Software Development Lifecycle (SDLC) from requirements through sprint planning, task management, CI/CD pipelines, release history, and DevOps metrics.

---

## 1. Project Directory Structure
```text
neuroforge-nexus/
├── backend/
│   ├── gateway-service/      # Spring Cloud Gateway (Port 8080)
│   ├── user-service/         # User Profiles, Teams, and Keycloak Sync (Port 8081)
│   ├── project-service/      # Projects, Milestone Tracking (Port 8082)
│   ├── sprint-service/       # Sprint Planning, Goals, and Capacity (Port 8083)
│   ├── shared-library/       # Common JPA, Auditing, Exceptions, and Utilities
│   └── pom.xml               # Parent Maven POM
├── frontend/                 # Angular 19/20 Standalone client app (Port 4200)
├── infrastructure/
│   ├── database/             # Postgres multi-db initialization scripts
│   ├── keycloak/             # Keycloak Realm export with OIDC configuration
│   └── docker-compose.yml    # Runs Postgres, Redis, Kafka, and Keycloak
├── database/                 # Symlinked/Replicated DB schema resources
├── docs/                     # Architecture designs, specs, and diagrams
└── scripts/                  # Command-line utility runner scripts
```

---

## 2. Prerequisites
Ensure you have the following installed on your machine:
1. **Java Development Kit (JDK) 22+** (Java 22 detected on environment)
2. **Node.js 22.14.0+** & **npm 11+**
3. **Docker Desktop** (with compose support)
4. **Apache Maven** (or run standard Maven wrapper/IDE integrations)

---

## 3. Quick Start Guide

### Step 1: Spin up the Infrastructure
Launch PostgreSQL, Redis, Kafka, and Keycloak using Docker Compose:
```bash
docker compose -f infrastructure/docker-compose.yml up -d
```
Verify the services are running and keycloak imports the realm on port `9000`.

### Step 2: Build and Build Backend Services
Compile the parent Maven project:
```bash
cd backend
mvn clean install
```
This builds `shared-library` first, followed by the individual gateway, user, project, and sprint services.

### Step 3: Run Backend Services
Launch each microservice in separate terminal shells or within your IDE (e.g. IntelliJ IDEA):
- **Gateway Service**: `mvn spring-boot:run -pl gateway-service` (Port 8080)
- **User Service**: `mvn spring-boot:run -pl user-service` (Port 8081)
- **Project Service**: `mvn spring-boot:run -pl project-service` (Port 8082)
- **Sprint Service**: `mvn spring-boot:run -pl sprint-service` (Port 8083)

### Step 4: Run the Angular Frontend
Navigate to the frontend directory, install dependencies, and start the development server:
```bash
cd ../frontend
npm install
npm run start
```
The application will launch at `http://localhost:4200/`.

---

## 4. Default Test Credentials & Role Verification Codes

### Existing User Credentials

| Username | Email | Password | Role Assignment | Login Verification Code |
| :--- | :--- | :--- | :--- | :--- |
| **admin** | `admin@neuroforge.com` | `NeuroForge1!` | Admin | `A0621` |
| **admin1** | `admin1@neuroforge.com` | `NeuroForge1!` | Admin | `A0621` |
| **dev** | `dev@neuroforge.com` | `NeuroForge1!` | Software Developer | `D0621` (or `SD0621`) |
| **lead** | `lead@neuroforge.com` | `NeuroForge1!` | Team Lead | `TL0621` |

*Note: All current users' passwords have been updated in Keycloak to adhere to the secure password standard (1 uppercase, 1 lowercase, 1 special char, 1 number, minimum length 12).*

### Role Codes Reference Table (For Registration & Sign In)

| Role Assignment | Registration Role Code | Sign-in Verification Code |
| :--- | :--- | :--- |
| **Organization Owner** | `OW0621` | `OW0621` |
| **Team Lead** | `TL0621` | `TL0621` |
| **Software Developer** | `SD0621` | `D0621` (or `SD0621`) |
| **Quality Assurance (QA)** | `QA0621` | `QA0621` |
| **Stakeholder** | `S0621` | `S0621` |
| **Admin** | *N/A (Disabled)* | `A0621` |

---

## 5. Security Upgrades & Custom Validations

We have implemented strict client-side and server-side verification features:
1. **Dynamic Password Checklist**: Displays checkboxes that dynamically evaluate password strength in real-time on keypress (requires at least 1 uppercase letter, 1 lowercase letter, 1 number, 1 special character `!&#64;#$%^&*`, and a length of 12+).
2. **Strict Registration Validations**:
   - First and Last Name must not be empty.
   - Email address must end with `@neuroforge.com`.
   - Secret **Role Verification Code** input must exactly match the selected role.
   - Dynamic error banners appear when fields are invalid or empty.
3. **Login Two-Step Verification (Multi-Role Support)**:
   - Added a "Two-Step Verification Code" input.
   - Upon successful primary auth, compares the code against all of the user's active roles retrieved from the Keycloak JWT token (e.g. allowing `S0621` or `D0621` if the user is assigned both `STAKEHOLDER` and `DEVELOPER` roles simultaneously).
4. **Layout & Styling Enhancements**:
   - Added the Google Material Icons stylesheet to `index.html` to fix input icon glyphs rendering as plain truncated text (like `"perol"`, `"lock"`, `"secu"`).
   - Replaced `overflow: hidden;` with `overflow-y: auto;` in the `.unauth-viewport` wrapper class (`app.component.css`) and changed body to `min-height: 100vh;` in `styles.css`. This enables native vertical page scrolling for both unauthenticated pages (login/signin and register/signup) when viewport height is tight.
5. **Sprint Component Dropdown Fix**:
   - Resolved a binding mismatch in the Project selection dropdown on the Sprint Planning view. The component now accepts project string IDs and maps them correctly to resolve the project object reference, preventing `POST /api/v1/sprints/project/undefined` API failures.
