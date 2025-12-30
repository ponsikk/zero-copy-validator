@echo off
REM =========================================
REM Zero-Copy JSON Validator - Build Script (Windows)
REM =========================================

echo =========================================
echo Zero-Copy JSON Validator - Build Script
echo =========================================

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop and try again.
    exit /b 1
)

echo [INFO] Building Docker image...
docker-compose build app

if %errorlevel% neq 0 (
    echo [ERROR] Build failed!
    exit /b 1
)

echo.
echo [SUCCESS] Build completed successfully!
echo.
echo To run the application:
echo   run.bat
echo.
echo To run tests:
echo   test.bat
echo.
echo To start development environment:
echo   dev.bat
