@echo off
REM =============================================================================
REM Repository Setup Script (Windows)
REM This script creates the folder structure and clones all microservice repositories
REM =============================================================================

REM FILL IN THESE REPOSITORY URLS
REM Services (4 repositories)
set IDENTITY_SERVICE_REPO=git@github.com:HK251-DATN/identity-service.git
set BACK_OFFICE_SERVICE_REPO=git@github.com:HK251-DATN/back-office-service.git
set PRODUCT_STORAGE_SERVICE_REPO=git@github.com:HK251-DATN/product_storage_service.git
set ECOMMERCE_SERVICE_REPO=git@github.com:HK251-DATN/ecommerce-temp.git

REM Infrastructure (2 repositories)
set DATABASE_INFRA_REPO=git@github.com:HK251-DATN/database_schema.git
set KAFKA_INFRA_REPO=git@github.com:HK251-DATN/kafka-share.git

REM Frontend (2 repositories)
set FRONTEND_REPO_1=git@github.com:HK251-DATN/FE-prototype.git
set FRONTEND_REPO_2=git@github.com:HK251-DATN/back-office-ui.git
set FRONTEND_REPO_3=git@github.com:HK251-DATN/provider-ui.git

REM =============================================================================

echo ==========================================
echo E-Commerce Microservices Setup
echo ==========================================
echo.

REM Create directory structure
echo Creating directory structure...
if not exist "services" mkdir services
if not exist "infrastructure" mkdir infrastructure
if not exist "frontend" mkdir frontend
echo [OK] Directories created
echo.

REM Clone services
echo ==========================================
echo Cloning Service Repositories (4/8)
echo ==========================================
call :clone_repo "%IDENTITY_SERVICE_REPO%" "services\identity-service" "identity-service"
call :clone_repo "%BACK_OFFICE_SERVICE_REPO%" "services\back-office-service" "back-office-service"
call :clone_repo "%PRODUCT_STORAGE_SERVICE_REPO%" "services\product_storage_service" "product_storage_service"
call :clone_repo "%ECOMMERCE_SERVICE_REPO%" "services\ecommerce-service" "ecommerce-service"

REM Clone infrastructure
echo ==========================================
echo Cloning Infrastructure Repositories (2/8)
echo ==========================================
call :clone_repo "%DATABASE_INFRA_REPO%" "infrastructure\database" "database-infrastructure"
call :clone_repo "%KAFKA_INFRA_REPO%" "infrastructure\kafka" "kafka-infrastructure"

REM Clone frontend
echo ==========================================
echo Cloning Frontend Repositories (2/8)
echo ==========================================
call :clone_repo "%FRONTEND_REPO_1%" "frontend\ecommerce-ui" "ecommerce-ui"
call :clone_repo "%FRONTEND_REPO_2%" "frontend\back-office-ui" "back-office-ui"
call :clone_repo "%FRONTEND_REPO_3%" "frontend\provider-ui" "provider-ui"

echo ==========================================
echo Setup Complete!
echo ==========================================
echo.
echo Next steps:
echo 1. Fill in repository URLs in setup-repos.bat if you haven't already
echo 2. Configure .env files
echo 3. Run start.bat to start all services with Docker Compose
echo.
pause
exit /b

:clone_repo
setlocal
set repo_url=%~1
set target_dir=%~2
set repo_name=%~3

REM Check if URL is still a placeholder
echo %repo_url% | findstr /C:"YOUR_ORG" >nul
if %errorlevel%==0 (
    echo [SKIP] %repo_name% - URL not configured
    echo.
    endlocal
    exit /b
)

echo Cloning %repo_name%...
git clone "%repo_url%" "%target_dir%" 2>nul
if %errorlevel%==0 (
    echo [OK] Successfully cloned %repo_name%
) else (
    if exist "%target_dir%" (
        echo [WARN] %repo_name% already exists, skipping
    ) else (
        echo [ERROR] Failed to clone %repo_name%
    )
)
echo.
endlocal
exit /b
