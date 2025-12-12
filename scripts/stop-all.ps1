# Stop all Docker containers for the Charge Management System
# Usage: .\scripts\stop-all.ps1 [-Clean]
#
# Parameters:
#   -Clean    Also remove containers, network, and volumes

param(
    [switch]$Clean
)

$ErrorActionPreference = "Stop"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Stopping Charge Management System" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

if ($Clean) {
    Write-Host "Clean mode enabled - will remove containers, network, and volumes" -ForegroundColor Yellow
}

# Container names
$DbContainer = "charge-db"
$ManagerContainer = "charge-manager"
$ProxyContainer = "charge-proxy"
$NetworkName = "charge-network"

Write-Host ""
Write-Host "Stopping containers..."

# Stop Charge Proxy
$proxyRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "^$ProxyContainer$"
if ($proxyRunning) {
    docker stop $ProxyContainer
    Write-Host "  - Charge Proxy stopped"
} else {
    Write-Host "  - Charge Proxy was not running"
}

# Stop Charge Manager
$managerRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "^$ManagerContainer$"
if ($managerRunning) {
    docker stop $ManagerContainer
    Write-Host "  - Charge Manager stopped"
} else {
    Write-Host "  - Charge Manager was not running"
}

# Stop PostgreSQL
$dbRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "^$DbContainer$"
if ($dbRunning) {
    docker stop $DbContainer
    Write-Host "  - PostgreSQL stopped"
} else {
    Write-Host "  - PostgreSQL was not running"
}

if ($Clean) {
    Write-Host ""
    Write-Host "Removing containers..."
    
    foreach ($container in @($ProxyContainer, $ManagerContainer, $DbContainer)) {
        $exists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$container$"
        if ($exists) {
            docker rm $container
            Write-Host "  - $container removed"
        }
    }

    Write-Host ""
    Write-Host "Removing network..."
    $networkExists = docker network ls --format "{{.Name}}" | Select-String -Pattern "^$NetworkName$"
    if ($networkExists) {
        docker network rm $NetworkName
        Write-Host "  - Network '$NetworkName' removed"
    } else {
        Write-Host "  - Network '$NetworkName' does not exist"
    }

    Write-Host ""
    $response = Read-Host "Do you want to remove the database volume? (y/N)"
    if ($response -eq 'y' -or $response -eq 'Y') {
        $volumeExists = docker volume ls --format "{{.Name}}" | Select-String -Pattern "^charge-db-data$"
        if ($volumeExists) {
            docker volume rm charge-db-data
            Write-Host "  - Volume 'charge-db-data' removed"
        } else {
            Write-Host "  - Volume 'charge-db-data' does not exist"
        }
    } else {
        Write-Host "  - Volume 'charge-db-data' preserved"
    }
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "System stopped successfully!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

