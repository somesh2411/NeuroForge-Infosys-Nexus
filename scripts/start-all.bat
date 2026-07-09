@echo off
title NeuroForge Nexus - Startup Launcher
echo ======================================================
echo          NEUROFORGE NEXUS STARTUP LAUNCHER
echo ======================================================
echo.

:: 1. Start Docker Containers
echo [1/3] Launching Docker Infrastructure (Postgres, Redis, Kafka, Keycloak)...
cd /d "%~dp0..\infrastructure"
docker-compose up -d
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to start Docker containers. Make sure Docker Desktop is running!
    pause
    exit /b %ERRORLEVEL%
)
echo Infrastructure is healthy.
echo.

:: Define portable Maven path
set MVN_PATH="%~dp0..\maven\apache-maven-3.9.6\bin\mvn.cmd"

:: 2. Launch Backend Microservices in separate Windows
echo [2/3] Starting Backend Microservices in new terminal windows...
echo.

echo Launching API Gateway (Port 8080)...
cd /d "%~dp0..\backend\gateway-service"
start "NeuroForge - Gateway Service" cmd /k %MVN_PATH% spring-boot:run

timeout /t 5 >nul

echo Launching User Service (Port 8081)...
cd /d "%~dp0..\backend\user-service"
start "NeuroForge - User Service" cmd /k %MVN_PATH% spring-boot:run

timeout /t 3 >nul

echo Launching Project Service (Port 8082)...
cd /d "%~dp0..\backend\project-service"
start "NeuroForge - Project Service" cmd /k %MVN_PATH% spring-boot:run

timeout /t 3 >nul

echo Launching Sprint Service (Port 8083)...
cd /d "%~dp0..\backend\sprint-service"
start "NeuroForge - Sprint Service" cmd /k %MVN_PATH% spring-boot:run

timeout /t 5 >nul
echo Backend microservices are booting up.
echo.

:: 3. Launch Frontend Client
echo [3/3] Launching Angular Frontend Development Server (Port 4200)...
cd /d "%~dp0..\frontend"
start "NeuroForge - Angular Client" cmd /k npm run start

echo.
echo ======================================================
echo NEUROFORGE NEXUS STARTUP SEQUENCE TRIGGERED SUCCESSFUL!
echo.
echo Once the Angular compilation completes, open:
echo http://localhost:4200
echo ======================================================
pause
