#!/bin/bash
# Start all Docker containers for the Charge Management System
# Usage: ./scripts/start-all.sh

set -e

echo "=============================================="
echo "Starting Charge Management System"
echo "=============================================="

# Configuration
NETWORK_NAME="charge-network"
DB_CONTAINER="charge-db"
MANAGER_CONTAINER="charge-manager"
PROXY_CONTAINER="charge-proxy"

# Database configuration
DB_NAME="chargedb"
DB_USER="chargeuser"
DB_PASSWORD="chargepass"

# Create network if it doesn't exist
echo ""
echo "[1/4] Setting up Docker network..."
if ! docker network inspect $NETWORK_NAME >/dev/null 2>&1; then
    docker network create $NETWORK_NAME
    echo "Network '$NETWORK_NAME' created."
else
    echo "Network '$NETWORK_NAME' already exists."
fi

# Start PostgreSQL
echo ""
echo "[2/4] Starting PostgreSQL database..."
if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
        echo "PostgreSQL container is already running."
    else
        docker start $DB_CONTAINER
        echo "PostgreSQL container started."
    fi
else
    docker run -d \
        --name $DB_CONTAINER \
        --network $NETWORK_NAME \
        -e POSTGRES_DB=$DB_NAME \
        -e POSTGRES_USER=$DB_USER \
        -e POSTGRES_PASSWORD=$DB_PASSWORD \
        -p 5432:5432 \
        -v charge-db-data:/var/lib/postgresql/data \
        postgres:15-alpine
    echo "PostgreSQL container created and started."
fi

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5
until docker exec $DB_CONTAINER pg_isready -U $DB_USER -d $DB_NAME >/dev/null 2>&1; do
    echo "  PostgreSQL is not ready yet, waiting..."
    sleep 2
done
echo "PostgreSQL is ready!"

# Start Charge Manager
echo ""
echo "[3/4] Starting Charge Manager..."
if docker ps -a --format '{{.Names}}' | grep -q "^${MANAGER_CONTAINER}$"; then
    docker rm -f $MANAGER_CONTAINER >/dev/null 2>&1
fi

docker run -d \
    --name $MANAGER_CONTAINER \
    --network $NETWORK_NAME \
    -e DB_HOST=$DB_CONTAINER \
    -e DB_PORT=5432 \
    -e DB_NAME=$DB_NAME \
    -e DB_USER=$DB_USER \
    -e DB_PASSWORD=$DB_PASSWORD \
    -p 8080:8080 \
    -p 8081:8081 \
    charge-manager:latest

echo "Charge Manager started on ports 8080 (HTTP) and 8081 (SOAP)."

# Start Charge Proxy
echo ""
echo "[4/4] Starting Charge Proxy..."
if docker ps -a --format '{{.Names}}' | grep -q "^${PROXY_CONTAINER}$"; then
    docker rm -f $PROXY_CONTAINER >/dev/null 2>&1
fi

docker run -d \
    --name $PROXY_CONTAINER \
    --network $NETWORK_NAME \
    -p 8081:8081 \
    -p 8082:8082 \
    charge-proxy:latest

echo "Charge Proxy started on ports 8081 (REST) and 8082 (SOAP)."

# Wait for services to be healthy
echo ""
echo "Waiting for services to be healthy..."
sleep 10

echo ""
echo "=============================================="
echo "System started successfully!"
echo "=============================================="
echo ""
echo "Services:"
echo "  - PostgreSQL:          localhost:5432"
echo "  - Charge Manager SOAP: http://localhost:8081/ws/customer"
echo "  - Charge Manager WSDL: http://localhost:8081/ws/customer?wsdl"
echo "  - Charge Proxy SOAP:   http://localhost:8082/ws/charge"
echo "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
echo ""
echo "Actuator:"
echo "  - Manager Health: http://localhost:8080/actuator/health"
echo "  - Proxy Health:   http://localhost:8083/actuator/health"
echo ""
echo "Container status:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "charge-|NAMES"
echo ""
echo "To stop the system, run: ./scripts/stop-all.sh"
echo "To view logs, run: docker logs <container-name>"

