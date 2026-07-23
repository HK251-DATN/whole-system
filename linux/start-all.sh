#!/bin/bash

set -e

# =============================================================================
# One-shot startup: build service JARs, bring up all Docker services, seed
# scenario data, and (optionally) start the frontend UIs as containers.
#
# Equivalent to running, in order:
#   ./linux/build-local.sh
#   ./linux/start.sh up
#   docker compose --profile seed run --rm scenario-seeder all
#   [docker compose --profile frontend up -d --build   # if --enable-front-end]
# =============================================================================

cd "$(dirname "$0")/.."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}
print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ $1${NC}"; }

ENABLE_FRONTEND=0
SKIP_BUILD=0
SKIP_SEED=0
WITH_SETUP_REPOS=0
FRESH=0
WIPE_DATA=0
SKIP_CONFIRM=0

usage() {
    echo "Usage: ./linux/start-all.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --enable-front-end   Also build and start the 3 frontend UIs as Docker"
    echo "                       containers (ecommerce-ui:3000, back-office-ui:5173,"
    echo "                       provider-ui:5273). Experimental — see note printed"
    echo "                       at startup about backend CORS allow-lists."
    echo "  --with-setup-repos   Run linux/setup-repos.sh first (clone missing repos)"
    echo "  --skip-build         Skip the Maven JAR build (linux/build-local.sh)"
    echo "  --skip-seed          Skip running the scenario-seeder after startup"
    echo "  --fresh              Tear down any already-running containers first"
    echo "                       (docker compose down --remove-orphans) before"
    echo "                       starting. Keeps data volumes."
    echo "  --wipe-data          Like --fresh, but also deletes data volumes"
    echo "                       (Postgres, Kafka, pgAdmin, Elasticsearch). Destructive!"
    echo "  -y, --yes            Skip the confirmation prompt for --wipe-data"
    echo "  -h, --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./linux/start-all.sh"
    echo "  ./linux/start-all.sh --enable-front-end"
    echo "  ./linux/start-all.sh --skip-build --skip-seed"
    echo "  ./linux/start-all.sh --fresh          # replace an already-running system"
    echo "  ./linux/start-all.sh --wipe-data      # replace it AND wipe all data"
}

for arg in "$@"; do
    case "$arg" in
        --enable-front-end|--enable-frontend) ENABLE_FRONTEND=1 ;;
        --with-setup-repos) WITH_SETUP_REPOS=1 ;;
        --skip-build) SKIP_BUILD=1 ;;
        --skip-seed) SKIP_SEED=1 ;;
        --fresh) FRESH=1 ;;
        --wipe-data) WIPE_DATA=1 ;;
        -y|--yes) SKIP_CONFIRM=1 ;;
        -h|--help) usage; exit 0 ;;
        *)
            print_error "Unknown option: $arg"
            usage
            exit 1
            ;;
    esac
done

check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

check_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker compose"
    else
        print_error "docker-compose is not installed"
        exit 1
    fi
    print_success "Using: $DOCKER_COMPOSE_CMD"
}

check_env_file() {
    if [ ! -f .env ]; then
        print_error ".env file not found!"
        print_info "Creating .env from .env.example..."
        cp .env.example .env
        print_warning "Edit .env and add your Cloudflare R2 credentials, then re-run this script."
        exit 1
    fi
    print_success ".env file found"
}

print_header "Whole-System Startup"

if [ "$WITH_SETUP_REPOS" == "1" ]; then
    print_header "Step 0: Cloning repositories"
    ./linux/setup-repos.sh
fi

print_info "Running pre-flight checks..."
check_docker
check_docker_compose
check_env_file

if [ "$WIPE_DATA" == "1" ]; then
    FRESH=1
fi

if [ "$FRESH" == "1" ]; then
    print_header "Tearing down any already-running system"
    # COMPOSE_PROFILES ensures containers started under the "seed"/"frontend"
    # profiles are recognized too, so a prior --enable-front-end run gets torn
    # down cleanly. --remove-orphans also catches containers left over from an
    # older version of this compose file.
    if [ "$WIPE_DATA" == "1" ]; then
        print_warning "This will WIPE all data volumes (Postgres, Kafka, pgAdmin, Elasticsearch)!"
        if [ "$SKIP_CONFIRM" != "1" ]; then
            read -r -p "Type 'yes' to continue: " answer
            if [ "$answer" != "yes" ]; then
                print_warning "Aborted, nothing was touched."
                exit 1
            fi
        fi
        COMPOSE_PROFILES=seed,frontend $DOCKER_COMPOSE_CMD down --remove-orphans -v
        print_success "Old containers, networks, and volumes removed"
    else
        COMPOSE_PROFILES=seed,frontend $DOCKER_COMPOSE_CMD down --remove-orphans
        print_success "Old containers and networks removed"
    fi
fi

if [ "$SKIP_BUILD" == "1" ]; then
    print_warning "Skipping JAR build (--skip-build)"
else
    print_header "Step 1: Building service JARs"
    ./linux/build-local.sh
fi

print_header "Step 2: Starting core services (Docker Compose)"
$DOCKER_COMPOSE_CMD up -d --build
print_success "Core services are up"

if [ "$SKIP_SEED" == "1" ]; then
    print_warning "Skipping scenario seeding (--skip-seed)"
else
    print_header "Step 3: Seeding scenario data"
    $DOCKER_COMPOSE_CMD --profile seed run --rm scenario-seeder all
    print_success "Scenario data seeded"
fi

if [ "$ENABLE_FRONTEND" == "1" ]; then
    print_header "Step 4: Building and starting frontend containers"
    print_warning "Frontend containers are experimental: each backend service only"
    print_warning "allows CORS requests from an allow-listed set of origins (see"
    print_warning "services/*/.../WebConfig.java). If a UI can't reach the backend,"
    print_warning "check that http://localhost:<ui-port> is in that service's list."
    $DOCKER_COMPOSE_CMD --profile frontend up -d --build ecommerce-ui back-office-ui provider-ui
    print_success "Frontend containers started"
else
    print_info "Frontend containers not started (pass --enable-front-end to include them)."
    print_info "Default dev workflow instead: cd frontend/<app> && npm run dev"
fi

print_header "Service URLs"
print_info "Identity Service:        http://localhost:9000"
print_info "Back-Office Service:     http://localhost:9100"
print_info "Product Storage Service: http://localhost:9200"
print_info "Ecommerce Service:       http://localhost:9300"
print_info "Search & Chat Service:   http://localhost:9400"
print_info "Kafka UI:                http://localhost:9280"
print_info "pgAdmin:                 http://localhost:5480"
if [ "$ENABLE_FRONTEND" == "1" ]; then
    print_info "Ecommerce UI:            http://localhost:3000"
    print_info "Back-Office UI:          http://localhost:5173"
    print_info "Provider UI:             http://localhost:5273"
fi
echo ""
print_header "Useful Commands"
print_info "View logs:          ./linux/start.sh logs [service-name]"
print_info "Stop all services:  ./linux/start.sh stop"
print_info "Rebuild a service:  ./linux/service.sh rebuild <service-name>"
print_info "Re-seed scenarios:  docker compose --profile seed run --rm scenario-seeder all"
echo ""
print_success "Done."
