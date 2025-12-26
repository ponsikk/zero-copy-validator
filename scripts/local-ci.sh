#!/bin/bash
# ============================================
# Local CI - Запуск CI pipeline локально
# ============================================
# Эмулирует GitHub Actions pipeline

set -e

# Цвета
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

FAILED=0

echo -e "${BLUE}╔══════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Local CI Pipeline - Zero-Copy Validator   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════╝${NC}"
echo ""

# ============================================
# Функция для отчёта
# ============================================
run_step() {
    local name=$1
    local command=$2

    echo -e "${BLUE}▶ ${name}${NC}"

    if eval "$command"; then
        echo -e "${GREEN}  ✅ Passed${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}  ❌ Failed${NC}"
        echo ""
        FAILED=1
        return 1
    fi
}

# ============================================
# Job 1: Rust Build & Test
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Job 1: Rust Build & Test${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

run_step "Rust Format Check" "cd rust-ffi && cargo fmt -- --check"
run_step "Rust Clippy" "cd rust-ffi && cargo clippy -- -D warnings"
run_step "Rust Build (Debug)" "cd rust-ffi && cargo build --verbose"
run_step "Rust Tests" "cd rust-ffi && cargo test --verbose"
run_step "Rust Build (Release)" "cd rust-ffi && cargo build --release --verbose"

# ============================================
# Job 2: Java Build & Test
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Job 2: Java Build & Test${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Копируем native lib для тестов
mkdir -p src/main/resources/native/
cp rust-ffi/target/release/libjson_validator_ffi.* src/main/resources/native/ 2>/dev/null || true

run_step "Maven Compile" "mvn clean compile --batch-mode"
run_step "Maven Test" "mvn test --batch-mode"
run_step "Maven Package" "mvn package --batch-mode"

# ============================================
# Job 3: Docker Build & Test
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Job 3: Docker Build & Test${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

run_step "Docker Build" "docker-compose build app"
run_step "Docker Test" "docker run --rm zero-copy-app | grep -q 'Hello, World!'"

# ============================================
# Job 4: Security Audit
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Job 4: Security Audit${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Установка cargo-audit если нужно
if ! command -v cargo-audit &> /dev/null; then
    echo -e "${YELLOW}⚠️  Installing cargo-audit...${NC}"
    cargo install cargo-audit
fi

run_step "Rust Security Audit" "cd rust-ffi && cargo audit"

# ============================================
# Summary
# ============================================
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}CI Summary${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  🎉 All CI checks passed successfully!      ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
    exit 0
else
    echo -e "${RED}╔══════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  ❌ Some CI checks failed!                   ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════╝${NC}"
    exit 1
fi
