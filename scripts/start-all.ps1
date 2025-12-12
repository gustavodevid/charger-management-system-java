# Start all Docker containers for the Charge Management System
# Usage: .\scripts\start-all.ps1

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Starting Charge Management System" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

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
Write-Host "[1/4] Setting up Docker network..." -ForegroundColor Yellow
$networkExists = docker network ls --format "{{.Name}}" | Select-String -Pattern "^$NetworkName$"
if (-not $networkExists) {
    docker network create $NetworkName
    Write-Host "Network '$NetworkName' created."
} else {
    Write-Host "Network '$NetworkName' already exists."
}

# Start PostgreSQL
Write-Host ""
Write-Host "[2/4] Starting PostgreSQL database..." -ForegroundColor Yellow
$dbRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "^$DbContainer$"
$dbExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$DbContainer$"

if ($dbExists) {
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
$ready = $false
$attempts = 0
while (-not $ready -and $attempts -lt 30) {
    $result = docker exec $DbContainer pg_isready -U $DbUser -d $DbName 2>$null
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
    } else {
        Write-Host "  PostgreSQL is not ready yet, waiting..."
        Start-Sleep -Seconds 2
        $attempts++
    }
}
Write-Host "PostgreSQL is ready!" -ForegroundColor Green

# Start Charge Manager
Write-Host ""
Write-Host "[3/4] Starting Charge Manager..." -ForegroundColor Yellow
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
    -p 8081:8081 `
    charge-manager:latest

Write-Host "Charge Manager started on ports 8080 (HTTP) and 8081 (SOAP)."

# Start Charge Proxy
Write-Host ""
Write-Host "[4/4] Starting Charge Proxy..." -ForegroundColor Yellow
$proxyExists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$ProxyContainer$"
if ($proxyExists) {
    docker rm -f $ProxyContainer 2>$null
}

docker run -d `
    --name $ProxyContainer `
    --network $NetworkName `
    -p 8082:8082 `
    -p 8083:8081 `
    charge-proxy:latest

Write-Host "Charge Proxy started on ports 8082 (SOAP) and 8083 (Actuator)."

# Wait for services to be healthy
Write-Host ""
Write-Host "Waiting for services to be healthy..."
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "System started successfully!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
Write-Host "  - PostgreSQL:          localhost:5432"
Write-Host "  - Charge Manager SOAP: http://localhost:8081/ws/customer"
Write-Host "  - Charge Manager WSDL: http://localhost:8081/ws/customer?wsdl"
Write-Host "  - Charge Proxy SOAP:   http://localhost:8082/ws/charge"
Write-Host "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
Write-Host ""
Write-Host "Actuator:" -ForegroundColor Cyan
Write-Host "  - Manager Health: http://localhost:8080/actuator/health"
Write-Host "  - Proxy Health:   http://localhost:8083/actuator/health"
Write-Host ""
Write-Host "Container status:"
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}" | Select-String -Pattern "charge-|NAMES"
Write-Host ""
Write-Host "To stop the system, run: .\scripts\stop-all.ps1" -ForegroundColor Yellow
Write-Host "To view logs, run: docker logs <container-name>" -ForegroundColor Yellow

