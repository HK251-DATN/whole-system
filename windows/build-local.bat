@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Building JARs for all services
echo ========================================
echo.

set "services=identity-service back-office-service product_storage_service ecommerce-service"

for %%s in (%services%) do (
    echo Building %%s...
    cd services\%%s

    if exist mvnw.cmd (
        call mvnw.cmd clean package -DskipTests
        if errorlevel 1 (
            echo [ERROR] Failed to build %%s
            exit /b 1
        )
        echo [SUCCESS] %%s built successfully
    ) else (
        echo [ERROR] mvnw.cmd not found in services\%%s
        exit /b 1
    )

    cd ..\..
)

echo.
echo ========================================
echo All services built successfully!
echo ========================================
echo Now run: start.bat up
