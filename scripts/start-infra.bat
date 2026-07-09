@echo off
echo Starting NeuroForge Nexus Infrastructure (Postgres, Redis, Kafka, Keycloak)...
docker-compose -f ../infrastructure/docker-compose.yml up -d
echo Infrastructure containers started. Verify status using: docker ps
pause
