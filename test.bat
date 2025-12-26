@echo off
REM =========================================
REM Zero-Copy JSON Validator - Test Suite (Windows)
REM =========================================

echo =========================================
echo Zero-Copy JSON Validator - Test Suite
echo =========================================

REM Build test image
echo [INFO] Building test environment...
docker build -t zero-copy-test -f Dockerfile --target java-builder .

REM Run Rust tests
echo [INFO] Running Rust tests...
docker run --rm -v "%CD%\rust-ffi:/build/rust-ffi" rust:1.75-slim sh -c "cd /build/rust-ffi && cargo test --release"

if %errorlevel% neq 0 (
    echo [ERROR] Rust tests failed
    exit /b 1
)

echo [SUCCESS] Rust tests passed

REM Run Java tests
echo [INFO] Running Java tests...
docker run --rm zero-copy-test mvn test

if %errorlevel% neq 0 (
    echo [ERROR] Java tests failed
    exit /b 1
)

echo [SUCCESS] Java tests passed
echo.
echo [SUCCESS] All tests passed!
