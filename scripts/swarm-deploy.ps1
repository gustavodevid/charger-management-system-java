# Deploy the Charge Management System to Docker Swarm
# Usage: .\scripts\swarm-deploy.ps1

$ErrorActionPreference = "Stop"

$StackName = "charge-system"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Deploying Charge Management System to Swarm" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Get the project root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir

Set-Location $ProjectRoot

# Check if swarm is initialized
$swarmState = docker info --format '{{.Swarm.LocalNodeState}}' 2>$null
if ($swarmState -ne "active") {
    Write-Host "ERROR: Docker Swarm is not initialized!" -ForegroundColor Red
    Write-Host "Run: .\scripts\swarm-init.ps1"
    exit 1
}

# Check if images exist
Write-Host ""
Write-Host "[1/3] Checking Docker images..." -ForegroundColor Yellow
$managerImage = docker image inspect charge-manager:latest 2>$null
if (-not $?) {
    Write-Host "ERROR: charge-manager:latest image not found!" -ForegroundColor Red
    Write-Host "Run: .\scripts\build-all.ps1"
    exit 1
}

$proxyImage = docker image inspect charge-proxy:latest 2>$null
if (-not $?) {
    Write-Host "ERROR: charge-proxy:latest image not found!" -ForegroundColor Red
    Write-Host "Run: .\scripts\build-all.ps1"
    exit 1
}
Write-Host "Images found."

# Deploy the stack
Write-Host ""
Write-Host "[2/3] Deploying stack '$StackName'..." -ForegroundColor Yellow
docker stack deploy -c docker-stack.yml $StackName

# Wait for services to start
Write-Host ""
Write-Host "[3/3] Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "Stack deployed successfully!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services:" -ForegroundColor Yellow
docker stack services $StackName
Write-Host ""
Write-Host "Endpoints:" -ForegroundColor Yellow
Write-Host "  - Charge Manager WSDL: http://localhost:8081/ws/customer?wsdl"
Write-Host "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
Write-Host "  - Manager Actuator:    http://localhost:8080/actuator/health"
Write-Host "  - Proxy Actuator:      http://localhost:8083/actuator/health"
Write-Host ""
Write-Host "Commands:" -ForegroundColor Yellow
Write-Host "  - View services: docker stack services $StackName"
Write-Host "  - View logs:     docker service logs ${StackName}_charge-manager"
Write-Host "  - Remove stack:  .\scripts\swarm-remove.ps1"

