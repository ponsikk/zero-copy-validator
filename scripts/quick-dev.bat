@echo off
REM ============================================
REM Quick Dev - Windows version
REM ============================================

echo Quick Development Mode (Windows)
echo.

:build
echo [BUILD] Building Rust...
cd rust-ffi
cargo build
if %errorlevel% neq 0 (
    echo [ERROR] Rust build failed!
    pause
    goto end
)
cd ..

echo [BUILD] Building Java...
call mvn compile -q
if %errorlevel% neq 0 (
    echo [ERROR] Java build failed!
    pause
    goto end
)

echo [TEST] Running tests...
cd rust-ffi
cargo test --lib -q
if %errorlevel% neq 0 (
    echo [ERROR] Tests failed!
    pause
    goto end
)
cd ..

echo.
echo [SUCCESS] Build successful!
echo.

REM Простой watch режим через polling
timeout /t 3 /nobreak >nul
goto build

:end
