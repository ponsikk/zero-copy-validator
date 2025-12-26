#!/bin/bash
# ============================================
# Quick Dev - Быстрый цикл разработки
# ============================================
# Использование: ./scripts/quick-dev.sh

set -e

# Цвета
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}⚡ Quick Development Mode${NC}"
echo ""

# ============================================
# Функция для сборки и тестирования
# ============================================
build_and_test() {
    echo -e "${YELLOW}📦 Building...${NC}"

    # Rust build (incremental)
    (cd rust-ffi && cargo build) || return 1

    # Java compile (incremental)
    mvn compile -q || return 1

    # Быстрые тесты
    echo -e "${YELLOW}🧪 Running tests...${NC}"
    (cd rust-ffi && cargo test --lib -q) || return 1

    echo -e "${GREEN}✅ Build successful!${NC}"
    echo ""
}

# ============================================
# Watch режим
# ============================================
echo "Watching for changes..."
echo "Press Ctrl+C to stop"
echo ""

# Первая сборка
build_and_test

# Установка inotify-tools если нужно
if ! command -v inotifywait &> /dev/null; then
    echo -e "${YELLOW}⚠️  inotifywait not found. Install with:${NC}"
    echo "  Ubuntu/Debian: sudo apt-get install inotify-tools"
    echo "  macOS: brew install fswatch"
    echo ""
    echo "Falling back to polling mode..."

    # Polling режим
    while true; do
        sleep 2
        if git status --short | grep -E '\.(rs|java)$' > /dev/null; then
            echo -e "${BLUE}🔄 Changes detected, rebuilding...${NC}"
            build_and_test || true
        fi
    done
else
    # inotifywait режим
    while inotifywait -r -e modify rust-ffi/src src/main/java 2>/dev/null; do
        echo -e "${BLUE}🔄 Changes detected, rebuilding...${NC}"
        build_and_test || true
    done
fi
