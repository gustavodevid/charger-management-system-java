#!/bin/bash
# Build all Docker images for the Charge Management System
# Usage: ./scripts/build-all.sh

set -e

echo "=============================================="
echo "Building Charge Management System Docker Images"
echo "=============================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

echo ""
echo "[1/2] Building Charge Manager image..."
echo "----------------------------------------------"
docker build -t charge-manager:latest -f charge-manager/Dockerfile .

echo ""
echo "[2/2] Building Charge Proxy image..."
echo "----------------------------------------------"
docker build -t charge-proxy:latest -f charge-proxy/Dockerfile .

echo ""
echo "=============================================="
echo "Build completed successfully!"
echo "=============================================="
echo ""
echo "Images created:"
docker images | grep -E "charge-manager|charge-proxy|REPOSITORY"
echo ""
echo "To start the system, run: ./scripts/start-all.sh"

