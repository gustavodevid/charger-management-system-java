#!/bin/bash
# Show status of the Charge Management System in Docker Swarm
# Usage: ./scripts/swarm-status.sh

STACK_NAME="charge-system"

echo "=============================================="
echo "Charge Management System - Swarm Status"
echo "=============================================="

# Check if swarm is active
if ! docker info --format '{{.Swarm.LocalNodeState}}' | grep -q "active"; then
    echo ""
    echo "Docker Swarm is NOT initialized."
    echo "Run: ./scripts/swarm-init.sh"
    exit 0
fi

echo ""
echo "Swarm Nodes:"
docker node ls

echo ""
echo "=============================================="

# Check if stack is deployed
if docker stack ls | grep -q "$STACK_NAME"; then
    echo ""
    echo "Stack Services:"
    docker stack services $STACK_NAME
    
    echo ""
    echo "=============================================="
    echo ""
    echo "Service Details:"
    echo ""
    
    for service in $(docker stack services $STACK_NAME --format "{{.Name}}"); do
        echo "--- $service ---"
        docker service ps $service --format "table {{.ID}}\t{{.Node}}\t{{.CurrentState}}\t{{.Error}}" | head -5
        echo ""
    done
    
    echo "=============================================="
    echo ""
    echo "Endpoints:"
    echo "  - Charge Manager WSDL: http://localhost:8081/ws/customer?wsdl"
    echo "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
else
    echo ""
    echo "Stack '$STACK_NAME' is NOT deployed."
    echo "Run: ./scripts/swarm-deploy.sh"
fi

