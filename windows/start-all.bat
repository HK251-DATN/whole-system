@echo off
REM =============================================================================
REM One-shot startup: build service JARs, bring up all Docker services, seed
REM scenario data, and (optionally) start the frontend UIs as containers.
REM
REM Equivalent to running, in order:
REM   windows\build-local.bat
REM   windows\start.bat up
REM   docker compose --profile seed run --rm scenario-seeder all
REM   [docker compose --profile frontend up -d --build   # if --enable-front-end]
REM =============================================================================

setlocal enabledelayedexpansion
cd /d "%~dp0.."

set "ENABLE_FRONTEND=0"
set "SKIP_BUILD=0"
set "SKIP_SEED=0"
set "WITH_SETUP_REPOS=0"
set "FRESH=0"
set "WIPE_DATA=0"
set "SKIP_CONFIRM=0"

:parse_args
if "%~1"=="" goto :args_done

if /i "%~1"=="--enable-front-end" (
    set "ENABLE_FRONTEND=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--enable-frontend" (
    set "ENABLE_FRONTEND=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--with-setup-repos" (
    set "WITH_SETUP_REPOS=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--skip-build" (
    set "SKIP_BUILD=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--skip-seed" (
    set "SKIP_SEED=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--fresh" (
    set "FRESH=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--wipe-data" (
    set "WIPE_DATA=1"
    shift
    goto :parse_args
)
if /i "%~1"=="-y" (
    set "SKIP_CONFIRM=1"
    shift
    goto :parse_args
)
if /i "%~1"=="--yes" (
    set "SKIP_CONFIRM=1"
    shift
    goto :parse_args
)
if /i "%~1"=="-h" goto :show_usage
if /i "%~1"=="--help" goto :show_usage

echo [ERROR] Unknown option: %~1
echo.
goto :show_usage

:args_done

echo ========================================
echo Whole-System Startup
echo ========================================
echo.

if "%WITH_SETUP_REPOS%"=="1" (
    echo ========================================
    echo Step 0: Cloning repositories
    echo ========================================
    call windows\setup-repos.bat
)

echo Running pre-flight checks...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker first.
    exit /b 1
)
echo [OK] Docker is running

call :check_docker_compose
if errorlevel 1 exit /b 1
echo [OK] Using: %DOCKER_COMPOSE_CMD%

if not exist .env (
    echo [ERROR] .env file not found!
    echo Creating .env from .env.example...
    copy .env.example .env >nul
    echo [WARNING] Edit .env and add your Cloudflare R2 credentials, then re-run this script.
    exit /b 1
)
echo [OK] .env file found

if "%WIPE_DATA%"=="1" set "FRESH=1"

if "%FRESH%"=="1" (
    call :teardown
    if errorlevel 1 exit /b 1
)

if "%SKIP_BUILD%"=="1" (
    echo [WARNING] Skipping JAR build ^(--skip-build^)
) else (
    echo ========================================
    echo Step 1: Building service JARs
    echo ========================================
    call windows\build-local.bat
    if errorlevel 1 exit /b 1
)

echo ========================================
echo Step 2: Starting core services ^(Docker Compose^)
echo ========================================
%DOCKER_COMPOSE_CMD% up -d --build
if errorlevel 1 exit /b 1
echo [OK] Core services are up

if "%SKIP_SEED%"=="1" (
    echo [WARNING] Skipping scenario seeding ^(--skip-seed^)
) else (
    echo ========================================
    echo Step 3: Seeding scenario data
    echo ========================================
    %DOCKER_COMPOSE_CMD% --profile seed run --rm scenario-seeder all
    echo [OK] Scenario data seeded
)

if "%ENABLE_FRONTEND%"=="1" (
    echo ========================================
    echo Step 4: Building and starting frontend containers
    echo ========================================
    echo [WARNING] Frontend containers are experimental: each backend service only
    echo [WARNING] allows CORS requests from an allow-listed set of origins ^(see
    echo [WARNING] services\*\...\WebConfig.java^). If a UI can't reach the backend,
    echo [WARNING] check that http://localhost:PORT is in that service's list.
    %DOCKER_COMPOSE_CMD% --profile frontend up -d --build ecommerce-ui back-office-ui provider-ui
    echo [OK] Frontend containers started
) else (
    echo Frontend containers not started ^(pass --enable-front-end to include them^).
    echo Default dev workflow instead: cd frontend\APP_NAME ^&^& npm run dev
)

echo.
echo ========================================
echo Service URLs
echo ========================================
echo Identity Service:        http://localhost:9000
echo Back-Office Service:     http://localhost:9100
echo Product Storage Service: http://localhost:9200
echo Ecommerce Service:       http://localhost:9300
echo Search ^& Chat Service:   http://localhost:9400
echo Kafka UI:                http://localhost:9280
echo pgAdmin:                 http://localhost:5480
if "%ENABLE_FRONTEND%"=="1" (
    echo Ecommerce UI:            http://localhost:3000
    echo Back-Office UI:          http://localhost:5173
    echo Provider UI:             http://localhost:5273
)
echo.
echo ========================================
echo Useful Commands
echo ========================================
echo View logs:          windows\start.bat logs [service-name]
echo Stop all services:  windows\start.bat stop
echo Rebuild a service:  windows\service.bat rebuild ^<service-name^>
echo Re-seed scenarios:  docker compose --profile seed run --rm scenario-seeder all
echo.
echo [OK] Done.
exit /b 0

REM =============================================================================
REM Functions
REM =============================================================================

:check_docker_compose
where docker-compose >nul 2>&1
if %errorlevel% equ 0 (
    set "DOCKER_COMPOSE_CMD=docker-compose"
    exit /b 0
)
docker compose version >nul 2>&1
if %errorlevel% equ 0 (
    set "DOCKER_COMPOSE_CMD=docker compose"
    exit /b 0
)
echo [ERROR] docker-compose is not installed
exit /b 1

REM Tears down any already-running system so a fresh one can start cleanly.
REM COMPOSE_PROFILES ensures containers started under the "seed"/"frontend"
REM profiles are recognized too (e.g. a prior --enable-front-end run), and
REM --remove-orphans also catches containers left over from an older version
REM of the compose file.
:teardown
echo ========================================
echo Tearing down any already-running system
echo ========================================
if "%WIPE_DATA%"=="0" goto :teardown_no_wipe

echo [WARNING] This will WIPE all data volumes ^(Postgres, Kafka, pgAdmin, Elasticsearch^)!
if "%SKIP_CONFIRM%"=="1" goto :teardown_wipe_confirmed
set /p ANSWER="Type 'yes' to continue: "
if not "%ANSWER%"=="yes" (
    echo Aborted, nothing was touched.
    exit /b 1
)

:teardown_wipe_confirmed
set "COMPOSE_PROFILES=seed,frontend"
%DOCKER_COMPOSE_CMD% down --remove-orphans -v
set "COMPOSE_PROFILES="
echo [OK] Old containers, networks, and volumes removed
exit /b 0

:teardown_no_wipe
set "COMPOSE_PROFILES=seed,frontend"
%DOCKER_COMPOSE_CMD% down --remove-orphans
set "COMPOSE_PROFILES="
echo [OK] Old containers and networks removed
exit /b 0

:show_usage
echo Usage: windows\start-all.bat [OPTIONS]
echo.
echo Options:
echo   --enable-front-end   Also build and start the 3 frontend UIs as Docker
echo                        containers ^(ecommerce-ui:3000, back-office-ui:5173,
echo                        provider-ui:5273^). Experimental.
echo   --with-setup-repos   Run windows\setup-repos.bat first ^(clone missing repos^)
echo   --skip-build         Skip the Maven JAR build ^(windows\build-local.bat^)
echo   --skip-seed          Skip running the scenario-seeder after startup
echo   --fresh              Tear down any already-running containers first
echo                        ^(docker compose down --remove-orphans^) before starting.
echo                        Keeps data volumes.
echo   --wipe-data          Like --fresh, but also deletes data volumes
echo                        ^(Postgres, Kafka, pgAdmin, Elasticsearch^). Destructive!
echo   -y, --yes            Skip the confirmation prompt for --wipe-data
echo   -h, --help           Show this help message
echo.
echo Examples:
echo   windows\start-all.bat
echo   windows\start-all.bat --enable-front-end
echo   windows\start-all.bat --fresh
echo   windows\start-all.bat --wipe-data
exit /b 0
