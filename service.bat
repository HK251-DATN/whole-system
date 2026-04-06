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

:rebuild_service
echo [INFO] Building %SERVICE%...
docker-compose build --no-cache "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Build failed
    exit /b 1
)
echo [OK] Build successful
echo [INFO] Restarting %SERVICE%...
docker-compose up -d "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to restart service
    exit /b 1
)
echo [OK] Service restarted successfully
echo.
echo View logs with: service.bat logs %SERVICE_INPUT%
exit /b

:restart_service
echo [INFO] Restarting %SERVICE%...
docker-compose restart "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to restart service
    exit /b 1
)
echo [OK] Service restarted successfully
exit /b

:stop_service
echo [INFO] Stopping %SERVICE%...
docker-compose stop "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to stop service
    exit /b 1
)
echo [OK] Service stopped
exit /b

:start_service
echo [INFO] Starting %SERVICE%...
docker-compose up -d "%SERVICE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start service
    exit /b 1
)
echo [OK] Service started
exit /b

:view_logs
echo [INFO] Showing logs for %SERVICE% (Ctrl+C to exit)...
docker-compose logs -f --tail=100 "%SERVICE%"
exit /b

:show_status
echo [INFO] Status for %SERVICE%:
docker-compose ps "%SERVICE%"
exit /b

:exec_bash
echo [INFO] Opening bash in %SERVICE% container...
docker-compose exec "%SERVICE%" /bin/bash
exit /b
