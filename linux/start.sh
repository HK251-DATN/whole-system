#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check if .env file exists
check_env_file() {
    if [ ! -f .env ]; then
        print_error ".env file not found!"
        print_info "Creating .env from .env.example..."
        cp .env.example .env
        print_warning "Please edit .env file and add your Cloudflare R2 credentials:"
        print_warning "  - R2_ACCOUNT_ID"
        print_warning "  - R2_ACCESS_KEY"
        print_warning "  - R2_SECRET_KEY"
        echo ""
        read -p "Press Enter after you've updated the .env file to continue..."
    else
        print_success ".env file found"
    fi
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

# Check if docker-compose is available
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

# Main script
main() {
    print_header "E-Commerce Platform Startup Script"

    # Pre-flight checks
    print_info "Running pre-flight checks..."
    check_docker
    check_docker_compose
    check_env_file

    # Parse command line arguments
    ACTION="${1:-up}"

    case "$ACTION" in
        up|start)
            print_header "Starting all services..."
            print_info "This will start:"
            print_info "  - PostgreSQL (4 databases)"
            print_info "  - Kafka + Kafka UI"
            print_info "  - Identity Service (port 9000)"
            print_info "  - Back-Office Service (port 9100)"
            print_info "  - Product Storage Service (port 9200)"
            print_info "  - Ecommerce Service (port 9301)"
            echo ""

            $DOCKER_COMPOSE_CMD up -d --build

            print_success "All services started!"
            echo ""
            print_header "Service URLs"
            print_info "Identity Service:        http://localhost:9000"
            print_info "Back-Office Service:     http://localhost:9100"
            print_info "Product Storage Service: http://localhost:9200"
            print_info "Ecommerce Service:       http://localhost:9301"
            print_info "Kafka UI:                http://localhost:9280"
            print_info "PostgreSQL:              localhost:5432"
            echo ""
            print_header "Useful Commands"
            print_info "View logs:          $DOCKER_COMPOSE_CMD logs -f [service-name]"
            print_info "Stop all services:  ./start.sh stop"
            print_info "Restart services:   ./start.sh restart"
            print_info "View status:        ./start.sh status"
            ;;

        down|stop)
            print_header "Stopping all services..."
            $DOCKER_COMPOSE_CMD down
            print_success "All services stopped"
            ;;

        restart)
            print_header "Restarting all services..."
            $DOCKER_COMPOSE_CMD restart
            print_success "All services restarted"
            ;;

        rebuild)
            print_header "Rebuilding and restarting all services..."
            $DOCKER_COMPOSE_CMD down
            $DOCKER_COMPOSE_CMD up -d --build
            print_success "All services rebuilt and started"
            ;;

        logs)
            SERVICE="${2:-}"
            if [ -z "$SERVICE" ]; then
                print_info "Showing logs for all services (Ctrl+C to exit)..."
                $DOCKER_COMPOSE_CMD logs -f
            else
                print_info "Showing logs for $SERVICE (Ctrl+C to exit)..."
                $DOCKER_COMPOSE_CMD logs -f "$SERVICE"
            fi
            ;;

        status)
            print_header "Service Status"
            $DOCKER_COMPOSE_CMD ps
            ;;

        clean)
            print_warning "This will remove all containers, networks, and volumes!"
            read -p "Are you sure? (y/N) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                print_info "Cleaning up..."
                $DOCKER_COMPOSE_CMD down -v
                print_success "Cleanup complete"
            else
                print_info "Cleanup cancelled"
            fi
            ;;

        help|--help|-h)
            print_header "E-Commerce Platform - Help"
            echo "Usage: ./start.sh [COMMAND] [OPTIONS]"
            echo ""
            echo "Commands:"
            echo "  up, start       Start all services (default)"
            echo "  down, stop      Stop all services"
            echo "  restart         Restart all services"
            echo "  rebuild         Rebuild and restart all services"
            echo "  logs [service]  View logs (optionally for specific service)"
            echo "  status          Show status of all services"
            echo "  clean           Remove all containers, networks, and volumes"
            echo "  help            Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./start.sh                    # Start all services"
            echo "  ./start.sh logs               # View all logs"
            echo "  ./start.sh logs kafka         # View Kafka logs only"
            echo "  ./start.sh stop               # Stop all services"
            ;;

        *)
            print_error "Unknown command: $ACTION"
            print_info "Run './start.sh help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
