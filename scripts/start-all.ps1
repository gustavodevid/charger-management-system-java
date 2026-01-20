# Start all Docker containers for the Charge Management System
# Usage: .\scripts\start-all.ps1

$ErrorActionPreference = "Stop"

Write-Host "=============================================="
Write-Host "Starting Charge Management System (Jakarta EE)"
Write-Host "=============================================="

# Configuration
$NetworkName = "charge-network"
$DbContainer = "charge-db"
$ManagerContainer = "charge-manager"
$ProxyContainer = "charge-proxy"

# Database configuration
$DbName = "chargedb"
$DbUser = "chargeuser"
$DbPassword = "chargepass"

# Create network if it doesn't exist
Write-Host ""
Write-Host "[1/4] Setting up Docker network..."
$networkExists = docker network ls --format "{{.Name}}" | Select-String -Pattern "^$NetworkName$"
if (-not $networkExists) {
    docker network create $NetworkName
    Write-Host "Network '$NetworkName' created."
} else {
    Write-Host "Network '$NetworkName' already exists."
}

# Start PostgreSQL
Write-Host ""
Write-Host "[2/4] Starting PostgreSQL database..."
$dbExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$DbContainer$"
if ($dbExists) {
    $dbRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "^$DbContainer$"
    if ($dbRunning) {
        Write-Host "PostgreSQL container is already running."
    } else {
        docker start $DbContainer
        Write-Host "PostgreSQL container started."
    }
} else {
    docker run -d `
        --name $DbContainer `
        --network $NetworkName `
        -e POSTGRES_DB=$DbName `
        -e POSTGRES_USER=$DbUser `
        -e POSTGRES_PASSWORD=$DbPassword `
        -p 5432:5432 `
        -v charge-db-data:/var/lib/postgresql/data `
        postgres:15-alpine
    Write-Host "PostgreSQL container created and started."
}

# Wait for PostgreSQL to be ready
Write-Host "Waiting for PostgreSQL to be ready..."
Start-Sleep -Seconds 5
do {
    $pgReady = docker exec $DbContainer pg_isready -U $DbUser -d $DbName 2>$null
    if (-not $pgReady) {
        Write-Host "  PostgreSQL is not ready yet, waiting..."
        Start-Sleep -Seconds 2
    }
} while (-not $pgReady)
Write-Host "PostgreSQL is ready!"

# Start Charge Manager (Tomcat)
Write-Host ""
Write-Host "[3/4] Starting Charge Manager (Tomcat)..."
$managerExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$ManagerContainer$"
if ($managerExists) {
    docker rm -f $ManagerContainer 2>$null
}

docker run -d `
    --name $ManagerContainer `
    --network $NetworkName `
    -e DB_HOST=$DbContainer `
    -e DB_PORT=5432 `
    -e DB_NAME=$DbName `
    -e DB_USER=$DbUser `
    -e DB_PASSWORD=$DbPassword `
    -p 8080:8080 `
    charge-manager:latest

Write-Host "Charge Manager started on port 8080 (Tomcat)."

# Start Charge Proxy (Tomcat)
Write-Host ""
Write-Host "[4/4] Starting Charge Proxy (Tomcat)..."
$proxyExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$ProxyContainer$"
if ($proxyExists) {
    docker rm -f $ProxyContainer 2>$null
}

docker run -d `
    --name $ProxyContainer `
    --network $NetworkName `
    -e ASAAS_API_URL=https://sandbox.asaas.com/api/v3 `
    -e ASAAS_ACCESS_TOKEN="" `
    -p 8082:8080 `
    charge-proxy:latest

Write-Host "Charge Proxy started on port 8082 (Tomcat)."

# Wait for services to be healthy
Write-Host ""
Write-Host "Waiting for services to be healthy..."
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "=============================================="
Write-Host "System started successfully!"
Write-Host "=============================================="
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
Write-Host "  - PostgreSQL:          localhost:5432"
Write-Host "  - Charge Manager SOAP: http://localhost:8080/ws/customer"
Write-Host "  - Charge Manager WSDL: http://localhost:8080/ws/customer?wsdl"
Write-Host "  - Charge Proxy SOAP:   http://localhost:8082/ws/charge"
Write-Host "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
Write-Host ""
Write-Host "Health Checks:" -ForegroundColor Cyan
Write-Host "  - Manager Health: http://localhost:8080/health"
Write-Host "  - Proxy Health:   http://localhost:8082/health"
Write-Host ""
Write-Host "Container status:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "charge-|NAMES"
Write-Host ""
Write-Host "To stop the system, run: .\scripts\stop-all.ps1"
Write-Host "To view logs, run: docker logs <container-name>"
