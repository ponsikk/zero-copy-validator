#!/bin/bash
set -e

echo "========================================="
echo "Zero-Copy JSON Validator - Test Suite"
echo "========================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Build test image
echo -e "${BLUE}📦 Building test environment...${NC}"
docker build -t zero-copy-test -f Dockerfile --target java-builder .

# Run Rust tests
echo -e "${BLUE}🦀 Running Rust tests...${NC}"
docker run --rm \
    -v "$(pwd)/rust-ffi:/build/rust-ffi" \
    rust:1.75-slim \
    sh -c "cd /build/rust-ffi && cargo test --release"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Rust tests passed${NC}"
else
    echo -e "${RED}❌ Rust tests failed${NC}"
    exit 1
fi

# Run Java tests
echo -e "${BLUE}☕ Running Java tests...${NC}"
docker run --rm \
    zero-copy-test \
    mvn test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Java tests passed${NC}"
else
    echo -e "${RED}❌ Java tests failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 All tests passed!${NC}"
