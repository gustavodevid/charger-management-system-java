#!/bin/bash
# Remove the Charge Management System stack from Docker Swarm
# Usage: ./scripts/swarm-remove.sh [--leave]
#
# Options:
#   --leave    Also leave the swarm after removing the stack

set -e

STACK_NAME="charge-system"
LEAVE_SWARM=false

if [ "$1" == "--leave" ]; then
    LEAVE_SWARM=true
fi

echo "=============================================="
echo "Removing Charge Management System from Swarm"
echo "=============================================="

# Check if stack exists
if docker stack ls | grep -q "$STACK_NAME"; then
    echo ""
    echo "Removing stack '$STACK_NAME'..."
    docker stack rm $STACK_NAME
    
    echo ""
    echo "Waiting for services to be removed..."
    sleep 10
    
    echo "Stack removed."
else
    echo "Stack '$STACK_NAME' is not deployed."
fi

# Optionally leave the swarm
if [ "$LEAVE_SWARM" == true ]; then
    echo ""
    echo "Leaving Docker Swarm..."
    docker swarm leave --force
    echo "Left the swarm."
fi

echo ""
echo "=============================================="
echo "Cleanup complete!"
echo "=============================================="

if [ "$LEAVE_SWARM" == false ]; then
    echo ""
    echo "Note: The swarm is still active."
    echo "To leave the swarm, run: docker swarm leave --force"
    echo "Or run: ./scripts/swarm-remove.sh --leave"
fi

