# Show status of the Charge Management System in Docker Swarm
# Usage: .\scripts\swarm-status.ps1

$StackName = "charge-system"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Charge Management System - Swarm Status" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Check if swarm is active
$swarmState = docker info --format '{{.Swarm.LocalNodeState}}' 2>$null
if ($swarmState -ne "active") {
    Write-Host ""
    Write-Host "Docker Swarm is NOT initialized." -ForegroundColor Red
    Write-Host "Run: .\scripts\swarm-init.ps1"
    exit 0
}

Write-Host ""
Write-Host "Swarm Nodes:" -ForegroundColor Yellow
docker node ls

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan

# Check if stack is deployed
$stacks = docker stack ls 2>$null
if ($stacks -match $StackName) {
    Write-Host ""
    Write-Host "Stack Services:" -ForegroundColor Yellow
    docker stack services $StackName
    
    Write-Host ""
    Write-Host "==============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Service Details:" -ForegroundColor Yellow
    Write-Host ""
    
    $services = docker stack services $StackName --format "{{.Name}}"
    foreach ($service in $services) {
        Write-Host "--- $service ---" -ForegroundColor Cyan
        docker service ps $service --format "table {{.ID}}`t{{.Node}}`t{{.CurrentState}}`t{{.Error}}" | Select-Object -First 5
        Write-Host ""
    }
    
    Write-Host "==============================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Endpoints:" -ForegroundColor Yellow
    Write-Host "  - Charge Manager WSDL: http://localhost:8081/ws/customer?wsdl"
    Write-Host "  - Charge Proxy WSDL:   http://localhost:8082/ws/charge?wsdl"
} else {
    Write-Host ""
    Write-Host "Stack '$StackName' is NOT deployed." -ForegroundColor Red
    Write-Host "Run: .\scripts\swarm-deploy.ps1"
}

