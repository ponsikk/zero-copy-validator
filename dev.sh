#!/bin/bash
set -e

echo "========================================="
echo "Zero-Copy JSON Validator - Dev Environment"
echo "========================================="

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${BLUE}🛠️  Starting development environment...${NC}"
echo ""
echo "This container includes:"
echo "  - Rust toolchain (stable)"
echo "  - Java 22 (Temurin)"
echo "  - Maven 3.9+"
echo "  - Git"
echo ""
echo -e "${GREEN}💡 Quick commands:${NC}"
echo "  cd rust-ffi && cargo build --release  # Build Rust library"
echo "  mvn clean package                     # Build Java project"
echo "  mvn test                              # Run tests"
echo ""

docker-compose run --rm dev
