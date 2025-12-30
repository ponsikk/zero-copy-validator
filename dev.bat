@echo off
REM =========================================
REM Zero-Copy JSON Validator - Dev Environment (Windows)
REM =========================================

echo =========================================
echo Zero-Copy JSON Validator - Dev Environment
echo =========================================
echo.
echo This container includes:
echo   - Rust toolchain (stable)
echo   - Java 22 (Temurin)
echo   - Maven 3.9+
echo   - Git
echo.
echo Quick commands:
echo   cd rust-ffi ^&^& cargo build --release  # Build Rust library
echo   mvn clean package                      # Build Java project
echo   mvn test                               # Run tests
echo.

docker-compose run --rm dev
