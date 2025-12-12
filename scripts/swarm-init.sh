#!/bin/bash
# Initialize Docker Swarm
# Usage: ./scripts/swarm-init.sh

set -e

echo "=============================================="
echo "Initializing Docker Swarm"
echo "=============================================="

# Check if already in swarm mode
if docker info --format '{{.Swarm.LocalNodeState}}' | grep -q "active"; then
    echo "Docker Swarm is already initialized."
    echo ""
    echo "Current node info:"
    docker node ls
else
    echo "Initializing Docker Swarm..."
    docker swarm init
    echo ""
    echo "Docker Swarm initialized successfully!"
    echo ""
    echo "This node is now a Swarm manager."
fi

echo ""
echo "=============================================="
echo "Swarm is ready!"
echo "=============================================="
echo ""
echo "Next steps:"
echo "  1. Build images: ./scripts/build-all.sh"
echo "  2. Deploy stack: ./scripts/swarm-deploy.sh"

