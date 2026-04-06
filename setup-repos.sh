#!/bin/bash

# =============================================================================
# Repository Setup Script
# This script creates the folder structure and clones all microservice repositories
# =============================================================================

# FILL IN THESE REPOSITORY URLS
# Services (4 repositories)
IDENTITY_SERVICE_REPO="git@github.com:HK251-DATN/identity-service.git"
BACK_OFFICE_SERVICE_REPO="git@github.com:HK251-DATN/back-office-service.git"
PRODUCT_STORAGE_SERVICE_REPO="git@github.com:HK251-DATN/product_storage_service.git"
ECOMMERCE_SERVICE_REPO="git@github.com:HK251-DATN/ecommerce-temp.git"

# Infrastructure (2 repositories)
DATABASE_INFRA_REPO="git@github.com:HK251-DATN/database_schema.git"
KAFKA_INFRA_REPO="git@github.com:HK251-DATN/kafka-share.git"

# Frontend (2 repositories)
FRONTEND_REPO_1="git@github.com:HK251-DATN/FE-prototype.git"
FRONTEND_REPO_2="git@github.com:HK251-DATN/back-office-ui.git"

# =============================================================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=========================================="
echo "E-Commerce Microservices Setup"
echo "=========================================="
echo ""

# Function to check if a URL is still a placeholder
check_placeholder() {
    if [[ $1 == *"YOUR_ORG"* ]]; then
        return 0  # Is placeholder
    fi
    return 1  # Is not placeholder
}

# Function to clone a repository
clone_repo() {
    local repo_url=$1
    local target_dir=$2
    local repo_name=$3

    if check_placeholder "$repo_url"; then
        echo -e "${YELLOW}⚠ Skipping $repo_name - URL not configured${NC}"
        return
    fi

    echo -e "${GREEN}→ Cloning $repo_name...${NC}"
    if git clone "$repo_url" "$target_dir" 2>/dev/null; then
        echo -e "${GREEN}✓ Successfully cloned $repo_name${NC}"
    else
        if [ -d "$target_dir" ]; then
            echo -e "${YELLOW}⚠ $repo_name already exists, skipping${NC}"
        else
            echo -e "${RED}✗ Failed to clone $repo_name${NC}"
        fi
    fi
    echo ""
}

# Create directory structure
echo "Creating directory structure..."
mkdir -p services
mkdir -p infrastructure
mkdir -p frontend
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

# Clone services
echo "=========================================="
echo "Cloning Service Repositories (4/8)"
echo "=========================================="
clone_repo "$IDENTITY_SERVICE_REPO" "services/identity-service" "identity-service"
clone_repo "$BACK_OFFICE_SERVICE_REPO" "services/back-office-service" "back-office-service"
clone_repo "$PRODUCT_STORAGE_SERVICE_REPO" "services/product_storage_service" "product_storage_service"
clone_repo "$ECOMMERCE_SERVICE_REPO" "services/ecommerce-service" "ecommerce-service"

# Clone infrastructure
echo "=========================================="
echo "Cloning Infrastructure Repositories (2/8)"
echo "=========================================="
clone_repo "$DATABASE_INFRA_REPO" "infrastructure/database" "database-infrastructure"
clone_repo "$KAFKA_INFRA_REPO" "infrastructure/kafka" "kafka-infrastructure"

# Clone frontend
echo "=========================================="
echo "Cloning Frontend Repositories (2/8)"
echo "=========================================="
clone_repo "$FRONTEND_REPO_1" "frontend/ecommerce-ui" "ecommerce-ui"
clone_repo "$FRONTEND_REPO_2" "frontend/back-office-ui" "back-office-ui"

echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Fill in repository URLs in setup-repos.sh if you haven't already"
echo "2. Configure .env files"
echo "3. Run ./start.sh to start all services with Docker Compose"
echo ""
