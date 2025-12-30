#!/bin/bash
set -e

echo "========================================="
echo "Zero-Copy JSON Validator - Run"
echo "========================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 Starting application...${NC}"
docker-compose up app

echo -e "${GREEN}✅ Application stopped${NC}"
