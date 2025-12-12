# Initialize Docker Swarm
# Usage: .\scripts\swarm-init.ps1

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Initializing Docker Swarm" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Check if already in swarm mode
$swarmState = docker info --format '{{.Swarm.LocalNodeState}}' 2>$null

if ($swarmState -eq "active") {
    Write-Host ""
    Write-Host "Docker Swarm is already initialized." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Current node info:"
    docker node ls
} else {
    Write-Host ""
    Write-Host "Initializing Docker Swarm..." -ForegroundColor Yellow
    docker swarm init
    Write-Host ""
    Write-Host "Docker Swarm initialized successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "This node is now a Swarm manager."
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "Swarm is ready!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Build images: .\scripts\build-all.ps1"
Write-Host "  2. Deploy stack: .\scripts\swarm-deploy.ps1"

