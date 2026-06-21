@echo off
setlocal enabledelayedexpansion

REM Colors simulation for Windows (limited)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Check if .env file exists
if not exist .env (
    echo %RED%Error: .env file not found!%NC%
    echo %BLUE%Creating .env from .env.example...%NC%
    copy .env.example .env
    echo %YELLOW%Please edit .env file and add your Cloudflare R2 credentials:%NC%
    echo %YELLOW%  - R2_ACCOUNT_ID%NC%
    echo %YELLOW%  - R2_ACCESS_KEY%NC%
    echo %YELLOW%  - R2_SECRET_KEY%NC%
    echo.
    pause
)

REM Parse command
set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=up"

REM Execute based on action
if "%ACTION%"=="up" goto START
if "%ACTION%"=="start" goto START
if "%ACTION%"=="down" goto STOP
if "%ACTION%"=="stop" goto STOP
if "%ACTION%"=="restart" goto RESTART
if "%ACTION%"=="rebuild" goto REBUILD
if "%ACTION%"=="logs" goto LOGS
if "%ACTION%"=="status" goto STATUS
if "%ACTION%"=="clean" goto CLEAN
if "%ACTION%"=="help" goto HELP
goto UNKNOWN

:START
echo %BLUE%========================================%NC%
echo %BLUE%Starting all services...%NC%
echo %BLUE%========================================%NC%
echo.
echo %BLUE%This will start:%NC%
echo   - PostgreSQL (4 databases)
echo   - Kafka + Kafka UI
echo   - Identity Service (port 9000)
echo   - Back-Office Service (port 9100)
echo   - Product Storage Service (port 9200)
echo   - Ecommerce Service (port 9301)
echo.

docker-compose up -d --build

echo.
echo %GREEN%All services started!%NC%
echo.
echo %BLUE%========================================%NC%
echo %BLUE%Service URLs%NC%
echo %BLUE%========================================%NC%
echo Identity Service:        http://localhost:9000
echo Back-Office Service:     http://localhost:9100
echo Product Storage Service: http://localhost:9200
echo Ecommerce Service:       http://localhost:9301
echo Kafka UI:                http://localhost:9280
echo PostgreSQL:              localhost:5432
echo.
echo %BLUE%Useful Commands:%NC%
echo   View logs:          start.bat logs [service-name]
echo   Stop all services:  start.bat stop
echo   Restart services:   start.bat restart
echo   View status:        start.bat status
goto END

:STOP
echo %BLUE%Stopping all services...%NC%
docker-compose down
echo %GREEN%All services stopped%NC%
goto END

:RESTART
echo %BLUE%Restarting all services...%NC%
docker-compose restart
echo %GREEN%All services restarted%NC%
goto END

:REBUILD
echo %BLUE%Rebuilding and restarting all services...%NC%
docker-compose down
docker-compose up -d --build
echo %GREEN%All services rebuilt and started%NC%
goto END

:LOGS
set "SERVICE=%~2"
if "%SERVICE%"=="" (
    echo %BLUE%Showing logs for all services (Ctrl+C to exit)...%NC%
    docker-compose logs -f
) else (
    echo %BLUE%Showing logs for %SERVICE% (Ctrl+C to exit)...%NC%
    docker-compose logs -f %SERVICE%
)
goto END

:STATUS
echo %BLUE%Service Status%NC%
docker-compose ps
goto END

:CLEAN
echo %YELLOW%This will remove all containers, networks, and volumes!%NC%
set /p "CONFIRM=Are you sure? (y/N): "
if /i "%CONFIRM%"=="y" (
    echo %BLUE%Cleaning up...%NC%
    docker-compose down -v
    echo %GREEN%Cleanup complete%NC%
) else (
    echo %BLUE%Cleanup cancelled%NC%
)
goto END

:HELP
echo %BLUE%========================================%NC%
echo %BLUE%E-Commerce Platform - Help%NC%
echo %BLUE%========================================%NC%
echo.
echo Usage: start.bat [COMMAND] [OPTIONS]
echo.
echo Commands:
echo   up, start       Start all services (default)
echo   down, stop      Stop all services
echo   restart         Restart all services
echo   rebuild         Rebuild and restart all services
echo   logs [service]  View logs (optionally for specific service)
echo   status          Show status of all services
echo   clean           Remove all containers, networks, and volumes
echo   help            Show this help message
echo.
echo Examples:
echo   start.bat                    # Start all services
echo   start.bat logs               # View all logs
echo   start.bat logs kafka         # View Kafka logs only
echo   start.bat stop               # Stop all services
goto END

:UNKNOWN
echo %RED%Unknown command: %ACTION%%NC%
echo %BLUE%Run 'start.bat help' for usage information%NC%
goto END

:END
endlocal
