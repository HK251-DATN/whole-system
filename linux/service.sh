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
    ["ecommerce-ui"]="ecommerce-ui"
    ["back-office-ui"]="back-office-ui"
    ["backoffice-ui"]="back-office-ui"
    ["search-chat"]="search-chat-service"
    ["search"]="search-chat-service"
    ["chat"]="search-chat-service"
)

# Service directory mapping (for Maven builds)
declare -A SERVICE_DIR=(
    ["identity-service"]="services/identity-service"
    ["back-office-service"]="services/back-office-service"
    ["product-storage-service"]="services/product_storage_service"
    ["ecommerce-service"]="services/ecommerce-service"
)

# Service directory mapping (for Python/Docker-only builds)
declare -A PYTHON_SERVICE_DIR=(
    ["search-chat-service"]="services/search-chat-service"
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
    echo "  reset       - Wipe a service's data volume and start it fresh (DESTRUCTIVE)"
    echo "  logs        - View logs for a service (follow mode)"
    echo "  status      - Show status of a service"
    echo "  exec        - Execute bash inside a service container"
    echo ""
    echo -e "${BLUE}Service names:${NC}"
    echo "  identity         - Identity Service (port 9000)"
    echo "  back-office      - Back Office Service (port 9100)"
    echo "  product-storage  - Product Storage Service (port 9200)"
    echo "  ecommerce        - Ecommerce Service (port 9301)"
    echo "  ecommerce-ui     - Ecommerce UI (port 3000)"
    echo "  back-office-ui   - Back Office UI (port 5173)"
    echo "  search-chat      - Search & Chat Service (port 9400)"
    echo "  postgres / db    - PostgreSQL Database"
    echo "  kafka            - Kafka Broker"
    echo "  kafka-ui         - Kafka UI"
    echo "  infra            - postgres + kafka together (reset only)"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo "  ./service.sh rebuild identity          # Rebuild identity service after code changes"
    echo "  ./service.sh restart product-storage   # Restart product storage service"
    echo "  ./service.sh logs ecommerce            # View ecommerce service logs"
    echo "  ./service.sh stop back-office          # Stop back office service"
    echo "  ./service.sh exec identity             # Open bash in identity service container"
    echo "  ./service.sh reset postgres            # Wipe Postgres data and start clean"
    echo "  ./service.sh reset infra               # Wipe Postgres + Kafka and start clean"
    echo "  ./service.sh reset infra -y            # Same, skip the confirmation prompt"
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

# Check if docker-compose is available and detect which command to use
check_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker compose"
    else
        echo -e "${RED}Error: docker-compose is not installed${NC}"
        exit 1
    fi

    # Check if docker compose is running
    if ! $DOCKER_COMPOSE_CMD ps >/dev/null 2>&1; then
        echo -e "${RED}Error: Docker Compose is not running or docker-compose.yml not found${NC}"
        echo "Run './start.sh' first to start all services"
        exit 1
    fi
}

# Rebuild and restart a service
rebuild_service() {
    local service=$1
    local service_dir="${SERVICE_DIR[$service]}"
    local python_service_dir="${PYTHON_SERVICE_DIR[$service]}"

    # Python service (no Maven) — rebuild via Docker only
    if [ -n "$python_service_dir" ]; then
        echo -e "${BLUE}========================================${NC}"
        echo -e "${BLUE}Rebuilding $service (Python)${NC}"
        echo -e "${BLUE}========================================${NC}"

        echo -e "${YELLOW}→ [1/4] Stopping container...${NC}"
        $DOCKER_COMPOSE_CMD stop "$service" 2>/dev/null
        echo -e "${GREEN}✓ Container stopped${NC}"

        echo -e "${YELLOW}→ [2/4] Removing container...${NC}"
        $DOCKER_COMPOSE_CMD rm -f "$service" 2>/dev/null
        echo -e "${GREEN}✓ Container removed${NC}"

        echo -e "${YELLOW}→ [3/4] Building Docker image...${NC}"
        $DOCKER_COMPOSE_CMD build "$service"
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗ Docker build failed${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ Docker image built${NC}"

        echo -e "${YELLOW}→ [4/4] Starting container...${NC}"
        $DOCKER_COMPOSE_CMD up -d "$service"
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗ Failed to start container${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ Container started${NC}"

        echo ""
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}✓ $service rebuilt successfully${NC}"
        echo -e "${GREEN}========================================${NC}"
        echo ""
        echo "View logs with: ./service.sh logs $service"
        return
    fi

    # Only rebuild microservices (not infrastructure)
    if [ -z "$service_dir" ]; then
        echo -e "${YELLOW}⚠ $service is not a microservice. Use 'restart' instead.${NC}"
        restart_service "$service"
        return
    fi

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Rebuilding $service${NC}"
    echo -e "${BLUE}========================================${NC}"

    # Step 1: Stop the container
    echo -e "${YELLOW}→ [1/6] Stopping container...${NC}"
    $DOCKER_COMPOSE_CMD stop "$service" 2>/dev/null
    echo -e "${GREEN}✓ Container stopped${NC}"

    # Step 2: Remove the container
    echo -e "${YELLOW}→ [2/6] Removing container...${NC}"
    $DOCKER_COMPOSE_CMD rm -f "$service" 2>/dev/null
    echo -e "${GREEN}✓ Container removed${NC}"

    # Step 3: Remove the Docker image
    echo -e "${YELLOW}→ [3/6] Removing Docker image...${NC}"
    local image_name="whole-system-${service}"
    docker rmi "$image_name" 2>/dev/null || true
    echo -e "${GREEN}✓ Docker image removed${NC}"

    # Step 4: Build JAR locally
    echo -e "${YELLOW}→ [4/6] Building JAR with Maven...${NC}"
    cd "$service_dir"
    if [ -f "./mvnw" ]; then
        ./mvnw clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗ Maven build failed${NC}"
            cd - > /dev/null
            exit 1
        fi
    else
        echo -e "${RED}✗ mvnw not found in $service_dir${NC}"
        cd - > /dev/null
        exit 1
    fi
    cd - > /dev/null
    echo -e "${GREEN}✓ JAR built successfully${NC}"

    # Step 5: Build Docker image using Dockerfile.local
    echo -e "${YELLOW}→ [5/6] Building Docker image...${NC}"
    $DOCKER_COMPOSE_CMD build "$service"
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Docker build failed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker image built${NC}"

    # Step 6: Start the container
    echo -e "${YELLOW}→ [6/6] Starting container...${NC}"
    $DOCKER_COMPOSE_CMD up -d "$service"
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Failed to start container${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Container started${NC}"

    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ $service rebuilt successfully${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "View logs with: ./service.sh logs $service"
}

# Restart a service without rebuilding
restart_service() {
    local service=$1
    echo -e "${YELLOW}→ Restarting $service...${NC}"
    $DOCKER_COMPOSE_CMD restart "$service"

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
    $DOCKER_COMPOSE_CMD stop "$service"

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
    $DOCKER_COMPOSE_CMD up -d "$service"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Service started${NC}"
    else
        echo -e "${RED}✗ Failed to start service${NC}"
        exit 1
    fi
}

# Ask for confirmation before a destructive action, unless -y/--yes was passed
confirm_reset() {
    local prompt=$1
    if [ "$SKIP_CONFIRM" == "1" ]; then
        return 0
    fi

    echo -e "${RED}⚠ $prompt${NC}"
    read -r -p "Type 'yes' to continue: " answer
    if [ "$answer" != "yes" ]; then
        echo -e "${YELLOW}Aborted, nothing was touched.${NC}"
        exit 1
    fi
}

# Wipe a single infra service's data volume and start it fresh
reset_one_service() {
    local service=$1

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Resetting $service${NC}"
    echo -e "${BLUE}========================================${NC}"

    # `down <service> -v` scopes both the container removal and the volume wipe
    # to just this service (verified: other running services/volumes are untouched).
    # Requires a Compose version new enough to accept a service filter on `down`.
    echo -e "${YELLOW}→ [1/2] Removing container and its data volume(s)...${NC}"
    $DOCKER_COMPOSE_CMD down "$service" -v
    echo -e "${GREEN}✓ $service and its volume(s) removed${NC}"

    echo -e "${YELLOW}→ [2/2] Starting $service fresh...${NC}"
    $DOCKER_COMPOSE_CMD up -d "$service"
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Failed to start $service${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ $service started${NC}"
}

# Reset one or more infra services (postgres, kafka, or the "infra" shortcut for both)
reset_service() {
    local service=$1

    case "$service" in
        infra)
            confirm_reset "This wipes ALL data in Postgres (every service's DB) and Kafka (all topics/messages)."
            reset_one_service "postgres"
            reset_one_service "kafka"
            echo -e "${YELLOW}→ Recreating Kafka topics (waits for Kafka to report healthy)...${NC}"
            $DOCKER_COMPOSE_CMD up -d kafka-init
            ;;
        postgres)
            confirm_reset "This wipes ALL data in Postgres, including every microservice's database."
            reset_one_service "postgres"
            ;;
        kafka)
            confirm_reset "This wipes all Kafka topics and messages (consumer offsets included)."
            reset_one_service "kafka"
            echo -e "${YELLOW}→ Recreating Kafka topics (waits for Kafka to report healthy)...${NC}"
            $DOCKER_COMPOSE_CMD up -d kafka-init
            ;;
        *)
            confirm_reset "This wipes $service's data volume, if it has one."
            reset_one_service "$service"
            ;;
    esac

    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ Reset complete${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${YELLOW}Note: microservices connected to what you just reset (DB connections, Kafka${NC}"
    echo -e "${YELLOW}consumer groups) may need a restart to pick up a clean state:${NC}"
    echo "  ./service.sh restart <service>"
}

# View logs
view_logs() {
    local service=$1
    echo -e "${BLUE}→ Showing logs for $service (Ctrl+C to exit)...${NC}"
    $DOCKER_COMPOSE_CMD logs -f --tail=100 "$service"
}

# Show status
show_status() {
    local service=$1
    echo -e "${BLUE}→ Status for $service:${NC}"
    $DOCKER_COMPOSE_CMD ps "$service"
}

# Execute bash in container
exec_bash() {
    local service=$1
    echo -e "${BLUE}→ Opening bash in $service container...${NC}"
    $DOCKER_COMPOSE_CMD exec "$service" /bin/bash
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

SKIP_CONFIRM=0
for arg in "${@:3}"; do
    if [ "$arg" == "-y" ] || [ "$arg" == "--yes" ]; then
        SKIP_CONFIRM=1
    fi
done

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
    reset)
        reset_service "$SERVICE"
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
