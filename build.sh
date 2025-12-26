#!/bin/bash
set -e

echo "========================================="
echo "Zero-Copy JSON Validator - Build Script"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

echo -e "${BLUE}📦 Building Docker image...${NC}"
docker-compose build app

echo -e "${GREEN}✅ Build completed successfully!${NC}"
echo ""
echo "To run the application:"
echo "  ./run.sh"
echo ""
echo "To run tests:"
echo "  ./test.sh"
echo ""
echo "To start development environment:"
echo "  docker-compose run --rm dev"
