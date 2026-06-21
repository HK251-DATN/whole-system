#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_header "Building JARs for all services"

# Array of services
services=("identity-service" "back-office-service" "product_storage_service" "ecommerce-service")

# Build each service
for service in "${services[@]}"; do
    print_info "Building $service..."
    cd "services/$service"

    if [ -f "./mvnw" ]; then
        ./mvnw clean package -DskipTests
        if [ $? -eq 0 ]; then
            print_success "$service built successfully"
        else
            print_error "Failed to build $service"
            exit 1
        fi
    else
        print_error "mvnw not found in services/$service"
        exit 1
    fi

    cd ../..
done

print_header "All services built successfully!"
print_info "Now run: ./start.sh up"
