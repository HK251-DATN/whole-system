#!/bin/bash

# =============================================================================
# Service Management Script
# Manage individual microservices during development
# =============================================================================

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service name mapping
declare -A SERVICE_MAP=(
    ["identity"]="identity-service"
    ["back-office"]="back-office-service"
    ["backoffice"]="back-office-service"
    ["product-storage"]="product-storage-service"
    ["product"]="product-storage-service"
    ["ecommerce"]="ecommerce-service"
    ["postgres"]="postgres"
    ["db"]="postgres"
    ["kafka"]="kafka"
    ["kafka-ui"]="kafka-ui"
)

show_usage() {
    echo -e "${BLUE}Usage:${NC}"
    echo "  ./service.sh <command> <service-name>"
    echo ""
    echo -e "${BLUE}Commands:${NC}"
    echo "  rebuild     - Rebuild and restart a service (apply code changes)"
    echo "  restart     - Restart a service without rebuilding"
    echo "  stop        - Stop a service"
    echo "  start       - Start a service"
    echo "  logs        - View logs for a service (follow mode)"
    echo "  status      - Show status of a service"
    echo "  exec        - Execute bash inside a service container"
    echo ""
    echo -e "${BLUE}Service names:${NC}"
    echo "  identity         - Identity Service (port 9000)"
    echo "  back-office      - Back Office Service (port 9100)"
    echo "  product-storage  - Product Storage Service (port 9200)"
    echo "  ecommerce        - Ecommerce Service (port 9301)"
    echo "  postgres / db    - PostgreSQL Database"
    echo "  kafka            - Kafka Broker"
    echo "  kafka-ui         - Kafka UI"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo "  ./service.sh rebuild identity          # Rebuild identity service after code changes"
    echo "  ./service.sh restart product-storage   # Restart product storage service"
    echo "  ./service.sh logs ecommerce            # View ecommerce service logs"
    echo "  ./service.sh stop back-office          # Stop back office service"
    echo "  ./service.sh exec identity             # Open bash in identity service container"
}

# Get actual service name from alias
get_service_name() {
    local input=$1
    if [[ -n "${SERVICE_MAP[$input]}" ]]; then
        echo "${SERVICE_MAP[$input]}"
    else
        echo "$input"
    fi
}

# Check if docker-compose is running
check_docker_compose() {
    if ! docker-compose ps >/dev/null 2>&1; then
        echo -e "${RED}Error: Docker Compose is not running or docker-compose.yml not found${NC}"
        echo "Run './start.sh' first to start all services"
        exit 1
    fi
}

# Rebuild and restart a service
rebuild_service() {
    local service=$1
    echo -e "${YELLOW}→ Building $service...${NC}"
    docker-compose build --no-cache "$service"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Build successful${NC}"
        echo -e "${YELLOW}→ Restarting $service...${NC}"
        docker-compose up -d "$service"

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Service restarted successfully${NC}"
            echo ""
            echo "View logs with: ./service.sh logs $service"
        else
            echo -e "${RED}✗ Failed to restart service${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Build failed${NC}"
        exit 1
    fi
}

# Restart a service without rebuilding
restart_service() {
    local service=$1
    echo -e "${YELLOW}→ Restarting $service...${NC}"
    docker-compose restart "$service"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Service restarted successfully${NC}"
    else
        echo -e "${RED}✗ Failed to restart service${NC}"
        exit 1
    fi
}

# Stop a service
stop_service() {
    local service=$1
    echo -e "${YELLOW}→ Stopping $service...${NC}"
    docker-compose stop "$service"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Service stopped${NC}"
    else
        echo -e "${RED}✗ Failed to stop service${NC}"
        exit 1
    fi
}

# Start a service
start_service() {
    local service=$1
    echo -e "${YELLOW}→ Starting $service...${NC}"
    docker-compose up -d "$service"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Service started${NC}"
    else
        echo -e "${RED}✗ Failed to start service${NC}"
        exit 1
    fi
}

# View logs
view_logs() {
    local service=$1
    echo -e "${BLUE}→ Showing logs for $service (Ctrl+C to exit)...${NC}"
    docker-compose logs -f --tail=100 "$service"
}

# Show status
show_status() {
    local service=$1
    echo -e "${BLUE}→ Status for $service:${NC}"
    docker-compose ps "$service"
}

# Execute bash in container
exec_bash() {
    local service=$1
    echo -e "${BLUE}→ Opening bash in $service container...${NC}"
    docker-compose exec "$service" /bin/bash
}

# Main script
if [ $# -lt 1 ]; then
    show_usage
    exit 1
fi

COMMAND=$1

if [ "$COMMAND" == "help" ] || [ "$COMMAND" == "-h" ] || [ "$COMMAND" == "--help" ]; then
    show_usage
    exit 0
fi

if [ $# -lt 2 ]; then
    echo -e "${RED}Error: Service name required${NC}"
    echo ""
    show_usage
    exit 1
fi

SERVICE_INPUT=$2
SERVICE=$(get_service_name "$SERVICE_INPUT")

check_docker_compose

case $COMMAND in
    rebuild)
        rebuild_service "$SERVICE"
        ;;
    restart)
        restart_service "$SERVICE"
        ;;
    stop)
        stop_service "$SERVICE"
        ;;
    start)
        start_service "$SERVICE"
        ;;
    logs)
        view_logs "$SERVICE"
        ;;
    status)
        show_status "$SERVICE"
        ;;
    exec)
        exec_bash "$SERVICE"
        ;;
    *)
        echo -e "${RED}Error: Unknown command '$COMMAND'${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac
