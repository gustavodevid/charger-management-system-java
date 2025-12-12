# Remove the Charge Management System stack from Docker Swarm
# Usage: .\scripts\swarm-remove.ps1 [-Leave]
#
# Parameters:
#   -Leave    Also leave the swarm after removing the stack

param(
    [switch]$Leave
)

$ErrorActionPreference = "Stop"

$StackName = "charge-system"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Removing Charge Management System from Swarm" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Check if stack exists
$stacks = docker stack ls 2>$null
if ($stacks -match $StackName) {
    Write-Host ""
    Write-Host "Removing stack '$StackName'..." -ForegroundColor Yellow
    docker stack rm $StackName
    
    Write-Host ""
    Write-Host "Waiting for services to be removed..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    Write-Host "Stack removed." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Stack '$StackName' is not deployed." -ForegroundColor Yellow
}

# Optionally leave the swarm
if ($Leave) {
    Write-Host ""
    Write-Host "Leaving Docker Swarm..." -ForegroundColor Yellow
    docker swarm leave --force
    Write-Host "Left the swarm." -ForegroundColor Green
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "Cleanup complete!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

if (-not $Leave) {
    Write-Host ""
    Write-Host "Note: The swarm is still active." -ForegroundColor Yellow
    Write-Host "To leave the swarm, run: docker swarm leave --force"
    Write-Host "Or run: .\scripts\swarm-remove.ps1 -Leave"
}

