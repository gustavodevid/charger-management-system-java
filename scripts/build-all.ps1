# Build all Docker images for the Charge Management System
# Usage: .\scripts\build-all.ps1

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Building Charge Management System Docker Images" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Get the project root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir

Set-Location $ProjectRoot

Write-Host ""
Write-Host "[1/2] Building Charge Manager image..." -ForegroundColor Yellow
Write-Host "----------------------------------------------"
docker build -t charge-manager:latest -f charge-manager/Dockerfile .

Write-Host ""
Write-Host "[2/2] Building Charge Proxy image..." -ForegroundColor Yellow
Write-Host "----------------------------------------------"
docker build -t charge-proxy:latest -f charge-proxy/Dockerfile .

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Images created:"
docker images | Select-String -Pattern "charge-manager|charge-proxy|REPOSITORY"
Write-Host ""
Write-Host "To start the system, run: .\scripts\start-all.ps1" -ForegroundColor Yellow

