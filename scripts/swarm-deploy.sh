#!/bin/bash
# Deploy the Charge Management System to Docker Swarm
# Usage: ./scripts/swarm-deploy.sh

set -e

STACK_NAME="charge-system"

echo "=============================================="
echo "Deploying Charge Management System to Swarm"
echo "(Jakarta EE / Tomcat)"
echo "=============================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# Check if swarm is initialized
if ! docker info --format '{{.Swarm.LocalNodeState}}' | grep -q "active"; then
    echo "ERROR: Docker Swarm is not initialized!"
    echo "Run: ./scripts/swarm-init.sh"
    exit 1
fi

# Check if images exist
echo ""
echo "[1/3] Checking Docker images..."
if ! docker image inspect charge-manager:latest >/dev/null 2>&1; then
    echo "ERROR: charge-manager:latest image not found!"
    echo "Run: ./scripts/build-all.sh"
    exit 1
fi

if ! docker image inspect charge-proxy:latest >/dev/null 2>&1; then
    echo "ERROR: charge-proxy:latest image not found!"
    echo "Run: ./scripts/build-all.sh"
    exit 1
fi
echo "Images found."

# Deploy the stack
echo ""
echo "[2/3] Deploying stack '$STACK_NAME'..."
docker stack deploy -c docker-stack.yml $STACK_NAME

# Wait for services to start
echo ""
echo "[3/3] Waiting for services to start..."
sleep 15

echo ""
echo "=============================================="
echo "Stack deployed successfully!"
echo "=============================================="
echo ""
echo "Services:"
docker stack services $STACK_NAME
echo ""
echo "Endpoints:"
echo "  - Charge Manager SOAP: http://localhost:8080/ws/customer"
echo "  - Charge Manager WSDL: http://localhost:8080/ws/customer?wsdl"
echo "  - Charge Proxy SOAP:   http://localhost:8082/ws/charge"
echo "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
echo ""
echo "Health Checks:"
echo "  - Manager Health: http://localhost:8080/health"
echo "  - Proxy Health:   http://localhost:8082/health"
echo ""
echo "Commands:"
echo "  - View services: docker stack services $STACK_NAME"
echo "  - View logs:     docker service logs ${STACK_NAME}_charge-manager"
echo "  - Remove stack:  ./scripts/swarm-remove.sh"
