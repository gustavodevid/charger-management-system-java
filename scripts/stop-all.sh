#!/bin/bash
# Stop all Docker containers for the Charge Management System
# Usage: ./scripts/stop-all.sh [--clean]
#
# Options:
#   --clean    Also remove containers, network, and volumes

set -e

echo "=============================================="
echo "Stopping Charge Management System"
echo "=============================================="

CLEAN_MODE=false
if [ "$1" == "--clean" ]; then
    CLEAN_MODE=true
    echo "Clean mode enabled - will remove containers, network, and volumes"
fi

# Container names
DB_CONTAINER="charge-db"
MANAGER_CONTAINER="charge-manager"
PROXY_CONTAINER="charge-proxy"
NETWORK_NAME="charge-network"

echo ""
echo "Stopping containers..."

# Stop Charge Proxy
if docker ps --format '{{.Names}}' | grep -q "^${PROXY_CONTAINER}$"; then
    docker stop $PROXY_CONTAINER
    echo "  - Charge Proxy stopped"
else
    echo "  - Charge Proxy was not running"
fi

# Stop Charge Manager
if docker ps --format '{{.Names}}' | grep -q "^${MANAGER_CONTAINER}$"; then
    docker stop $MANAGER_CONTAINER
    echo "  - Charge Manager stopped"
else
    echo "  - Charge Manager was not running"
fi

# Stop PostgreSQL
if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    docker stop $DB_CONTAINER
    echo "  - PostgreSQL stopped"
else
    echo "  - PostgreSQL was not running"
fi

if [ "$CLEAN_MODE" == true ]; then
    echo ""
    echo "Removing containers..."
    
    for container in $PROXY_CONTAINER $MANAGER_CONTAINER $DB_CONTAINER; do
        if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
            docker rm $container
            echo "  - $container removed"
        fi
    done

    echo ""
    echo "Removing network..."
    if docker network inspect $NETWORK_NAME >/dev/null 2>&1; then
        docker network rm $NETWORK_NAME
        echo "  - Network '$NETWORK_NAME' removed"
    else
        echo "  - Network '$NETWORK_NAME' does not exist"
    fi

    echo ""
    read -p "Do you want to remove the database volume? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if docker volume inspect charge-db-data >/dev/null 2>&1; then
            docker volume rm charge-db-data
            echo "  - Volume 'charge-db-data' removed"
        else
            echo "  - Volume 'charge-db-data' does not exist"
        fi
    else
        echo "  - Volume 'charge-db-data' preserved"
    fi
fi

echo ""
echo "=============================================="
echo "System stopped successfully!"
echo "=============================================="

