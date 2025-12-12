#!/bin/bash
# View logs for Charge Management System containers
# Usage: ./scripts/logs.sh [service] [-f]
#
# Services: manager, proxy, db, all
# Options:
#   -f    Follow log output

set -e

SERVICE="${1:-all}"
FOLLOW=""

# Check for -f flag in arguments
for arg in "$@"; do
    if [ "$arg" == "-f" ]; then
        FOLLOW="-f"
    fi
done

case $SERVICE in
    manager)
        echo "Showing logs for Charge Manager..."
        docker logs $FOLLOW charge-manager
        ;;
    proxy)
        echo "Showing logs for Charge Proxy..."
        docker logs $FOLLOW charge-proxy
        ;;
    db)
        echo "Showing logs for PostgreSQL..."
        docker logs $FOLLOW charge-db
        ;;
    all)
        echo "=============================================="
        echo "PostgreSQL Logs"
        echo "=============================================="
        docker logs --tail 20 charge-db 2>/dev/null || echo "Container not running"
        
        echo ""
        echo "=============================================="
        echo "Charge Manager Logs"
        echo "=============================================="
        docker logs --tail 50 charge-manager 2>/dev/null || echo "Container not running"
        
        echo ""
        echo "=============================================="
        echo "Charge Proxy Logs"
        echo "=============================================="
        docker logs --tail 50 charge-proxy 2>/dev/null || echo "Container not running"
        ;;
    *)
        echo "Usage: ./scripts/logs.sh [service] [-f]"
        echo ""
        echo "Services:"
        echo "  manager  - Charge Manager logs"
        echo "  proxy    - Charge Proxy logs"
        echo "  db       - PostgreSQL logs"
        echo "  all      - All services (default)"
        echo ""
        echo "Options:"
        echo "  -f       - Follow log output (tail -f)"
        exit 1
        ;;
esac

