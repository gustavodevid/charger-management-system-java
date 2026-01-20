# Deploy the Charge Management System to Docker Swarm
# Usage: .\scripts\swarm-deploy.ps1

$ErrorActionPreference = "Stop"

$StackName = "charge-system"

Write-Host "=============================================="
Write-Host "Deploying Charge Management System to Swarm"
Write-Host "(Jakarta EE / Tomcat)"
Write-Host "=============================================="

# Get project root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir

Set-Location $ProjectRoot

# Check if swarm is initialized
$swarmState = docker info --format '{{.Swarm.LocalNodeState}}'
if ($swarmState -ne "active") {
    Write-Host "ERROR: Docker Swarm is not initialized!" -ForegroundColor Red
    Write-Host "Run: .\scripts\swarm-init.ps1"
    exit 1
}

# Check if images exist
Write-Host ""
Write-Host "[1/3] Checking Docker images..."
$managerImage = docker image inspect charge-manager:latest 2>$null
if (-not $managerImage) {
    Write-Host "ERROR: charge-manager:latest image not found!" -ForegroundColor Red
    Write-Host "Run: .\scripts\build-all.ps1"
    exit 1
}

$proxyImage = docker image inspect charge-proxy:latest 2>$null
if (-not $proxyImage) {
    Write-Host "ERROR: charge-proxy:latest image not found!" -ForegroundColor Red
    Write-Host "Run: .\scripts\build-all.ps1"
    exit 1
}
Write-Host "Images found."

# Deploy the stack
Write-Host ""
Write-Host "[2/3] Deploying stack '$StackName'..."
docker stack deploy -c docker-stack.yml $StackName

# Wait for services to start
Write-Host ""
Write-Host "[3/3] Waiting for services to start..."
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "=============================================="
Write-Host "Stack deployed successfully!"
Write-Host "=============================================="
Write-Host ""
Write-Host "Services:"
docker stack services $StackName
Write-Host ""
Write-Host "Endpoints:" -ForegroundColor Cyan
Write-Host "  - Charge Manager SOAP: http://localhost:8080/ws/customer"
Write-Host "  - Charge Manager WSDL: http://localhost:8080/ws/customer?wsdl"
Write-Host "  - Charge Proxy SOAP:   http://localhost:8082/ws/charge"
Write-Host "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
Write-Host ""
Write-Host "Health Checks:" -ForegroundColor Cyan
Write-Host "  - Manager Health: http://localhost:8080/health"
Write-Host "  - Proxy Health:   http://localhost:8082/health"
Write-Host ""
Write-Host "Commands:"
Write-Host "  - View services: docker stack services $StackName"
Write-Host "  - View logs:     docker service logs ${StackName}_charge-manager"
Write-Host "  - Remove stack:  .\scripts\swarm-remove.ps1"
