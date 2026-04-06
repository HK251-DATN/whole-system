@echo off
REM =============================================================================
REM Service Management Script (Windows)
REM Manage individual microservices during development
REM =============================================================================

setlocal enabledelayedexpansion

if "%1"=="" goto :show_usage
if "%1"=="help" goto :show_usage
if "%1"=="-h" goto :show_usage
if "%1"=="--help" goto :show_usage

set COMMAND=%1
set SERVICE_INPUT=%2

if "%SERVICE_INPUT%"=="" (
    echo [ERROR] Service name required
    echo.
    goto :show_usage
)

REM Map service aliases to actual names
call :map_service_name "%SERVICE_INPUT%"

REM Detect docker-compose or docker compose
call :check_docker_compose

REM Execute command
if "%COMMAND%"=="rebuild" goto :rebuild_service
if "%COMMAND%"=="restart" goto :restart_service
if "%COMMAND%"=="stop" goto :stop_service
if "%COMMAND%"=="start" goto :start_service
if "%COMMAND%"=="logs" goto :view_logs
if "%COMMAND%"=="status" goto :show_status
if "%COMMAND%"=="exec" goto :exec_bash

echo [ERROR] Unknown command '%COMMAND%'
echo.
goto :show_usage

REM =============================================================================
REM Functions
REM =============================================================================

:show_usage
echo Usage:
echo   service.bat ^<command^> ^<service-name^>
echo.
echo Commands:
echo   rebuild     - Rebuild and restart a service (apply code changes)
echo   restart     - Restart a service without rebuilding
echo   stop        - Stop a service
echo   start       - Start a service
echo   logs        - View logs for a service (follow mode)
echo   status      - Show status of a service
echo   exec        - Execute bash inside a service container
echo.
echo Service names:
echo   identity         - Identity Service (port 9000)
echo   back-office      - Back Office Service (port 9100)
echo   product-storage  - Product Storage Service (port 9200)
echo   ecommerce        - Ecommerce Service (port 9301)
echo   postgres / db    - PostgreSQL Database
echo   kafka            - Kafka Broker
echo   kafka-ui         - Kafka UI
echo.
echo Examples:
echo   service.bat rebuild identity          # Rebuild identity service after code changes
echo   service.bat restart product-storage   # Restart product storage service
echo   service.bat logs ecommerce            # View ecommerce service logs
echo   service.bat stop back-office          # Stop back office service
echo   service.bat exec identity             # Open bash in identity service container
echo.
exit /b

:map_service_name
set INPUT=%~1
if "%INPUT%"=="identity" set SERVICE=identity-service& exit /b
if "%INPUT%"=="back-office" set SERVICE=back-office-service& exit /b
if "%INPUT%"=="backoffice" set SERVICE=back-office-service& exit /b
if "%INPUT%"=="product-storage" set SERVICE=product-storage-service& exit /b
if "%INPUT%"=="product" set SERVICE=product-storage-service& exit /b
if "%INPUT%"=="ecommerce" set SERVICE=ecommerce-service& exit /b
if "%INPUT%"=="postgres" set SERVICE=postgres& exit /b
if "%INPUT%"=="db" set SERVICE=postgres& exit /b
if "%INPUT%"=="kafka" set SERVICE=kafka& exit /b
if "%INPUT%"=="kafka-ui" set SERVICE=kafka-ui& exit /b
set SERVICE=%INPUT%
exit /b

:check_docker_compose
REM Check if docker-compose command exists
where docker-compose >nul 2>&1
if %errorlevel% equ 0 (
    set DOCKER_COMPOSE_CMD=docker-compose
    exit /b
)
REM Check if docker compose command exists
docker compose version >nul 2>&1
if %errorlevel% equ 0 (
    set DOCKER_COMPOSE_CMD=docker compose
    exit /b
)
REM Neither command found
echo [ERROR] docker-compose is not installed
exit /b 1

:rebuild_service
REM Check if service is a microservice
set IS_MICROSERVICE=0
if "%SERVICE%"=="identity-service" set IS_MICROSERVICE=1& set SERVICE_DIR=services\identity-service
if "%SERVICE%"=="back-office-service" set IS_MICROSERVICE=1& set SERVICE_DIR=services\back-office-service
if "%SERVICE%"=="product-storage-service" set IS_MICROSERVICE=1& set SERVICE_DIR=services\product_storage_service
if "%SERVICE%"=="ecommerce-service" set IS_MICROSERVICE=1& set SERVICE_DIR=services\ecommerce-service

if %IS_MICROSERVICE%==0 (
    echo [WARN] %SERVICE% is not a microservice. Use 'restart' instead.
    goto :restart_service
)

echo ========================================
echo Rebuilding %SERVICE%
echo ========================================

REM Step 1: Stop container
echo [1/6] Stopping container...
%DOCKER_COMPOSE_CMD% stop "%SERVICE%" 2>nul
echo [OK] Container stopped

REM Step 2: Remove container
echo [2/6] Removing container...
%DOCKER_COMPOSE_CMD% rm -f "%SERVICE%" 2>nul
echo [OK] Container removed

REM Step 3: Remove Docker image
echo [3/6] Removing Docker image...
docker rmi "whole-system-%SERVICE%" 2>nul
echo [OK] Docker image removed

REM Step 4: Build JAR locally
echo [4/6] Building JAR with Maven...
cd "%SERVICE_DIR%"
if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd not found in %SERVICE_DIR%
    cd ..\..
    exit /b 1
)
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed
    cd ..\..
    exit /b 1
)
cd ..\..
echo [OK] JAR built successfully

REM Step 5: Build Docker image
echo [5/6] Building Docker image...
%DOCKER_COMPOSE_CMD% build "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Docker build failed
    exit /b 1
)
echo [OK] Docker image built

REM Step 6: Start container
echo [6/6] Starting container...
%DOCKER_COMPOSE_CMD% up -d "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start container
    exit /b 1
)
echo [OK] Container started

echo.
echo ========================================
echo [OK] %SERVICE% rebuilt successfully
echo ========================================
echo.
echo View logs with: service.bat logs %SERVICE_INPUT%
exit /b

:restart_service
echo [INFO] Restarting %SERVICE%...
%DOCKER_COMPOSE_CMD% restart "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to restart service
    exit /b 1
)
echo [OK] Service restarted successfully
exit /b

:stop_service
echo [INFO] Stopping %SERVICE%...
%DOCKER_COMPOSE_CMD% stop "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to stop service
    exit /b 1
)
echo [OK] Service stopped
exit /b

:start_service
echo [INFO] Starting %SERVICE%...
%DOCKER_COMPOSE_CMD% up -d "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start service
    exit /b 1
)
echo [OK] Service started
exit /b

:view_logs
echo [INFO] Showing logs for %SERVICE% (Ctrl+C to exit)...
%DOCKER_COMPOSE_CMD% logs -f --tail=100 "%SERVICE%"
exit /b

:show_status
echo [INFO] Status for %SERVICE%:
%DOCKER_COMPOSE_CMD% ps "%SERVICE%"
exit /b

:exec_bash
echo [INFO] Opening bash in %SERVICE% container...
%DOCKER_COMPOSE_CMD% exec "%SERVICE%" /bin/bash
exit /b
