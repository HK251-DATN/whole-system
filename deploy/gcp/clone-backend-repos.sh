#!/bin/bash
set -euo pipefail

# =============================================================================
# Trimmed version of linux/setup-repos.sh for a backend-only VM deployment:
# clones what postgres + kafka + the 4 core services need (identity,
# back-office, product-storage, ecommerce) plus tools/scenario-seeder for
# seeding demo data. Skips frontend/* and search-chat-service — that's
# ~2.2GB of the repo's 2.7GB local footprint, not needed here.
#
# Requires SSH access to the HK251-DATN GitHub org from this VM (see
# deploy/gcp/README.md for setting up a deploy key or personal key).
# Run from the whole-system repo root.
# =============================================================================

IDENTITY_SERVICE_REPO="git@github.com:HK251-DATN/identity-service.git"
BACK_OFFICE_SERVICE_REPO="git@github.com:HK251-DATN/back-office-service.git"
PRODUCT_STORAGE_SERVICE_REPO="git@github.com:HK251-DATN/product_storage_service.git"
ECOMMERCE_SERVICE_REPO="git@github.com:HK251-DATN/e-commerce.git"

DATABASE_INFRA_REPO="git@github.com:HK251-DATN/database_schema.git"
KAFKA_INFRA_REPO="git@github.com:HK251-DATN/kafka-share.git"
PGADMIN_INFRA_REPO="git@github.com:HK251-DATN/pgadmin.git"

SCENARIO_SEEDER_TOOL_REPO="git@github.com:HK251-DATN/tools-scenario-seeder.git"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

clone_repo() {
  local repo_url=$1 target_dir=$2 repo_name=$3
  if [ -d "$target_dir/.git" ]; then
    echo -e "${YELLOW}⚠ $repo_name already exists, skipping${NC}"
    return
  fi
  echo -e "${GREEN}→ Cloning $repo_name...${NC}"
  git clone "$repo_url" "$target_dir"
}

mkdir -p services infrastructure tools

clone_repo "$IDENTITY_SERVICE_REPO" "services/identity-service" "identity-service"
clone_repo "$BACK_OFFICE_SERVICE_REPO" "services/back-office-service" "back-office-service"
clone_repo "$PRODUCT_STORAGE_SERVICE_REPO" "services/product_storage_service" "product_storage_service"
clone_repo "$ECOMMERCE_SERVICE_REPO" "services/ecommerce-service" "ecommerce-service"

clone_repo "$DATABASE_INFRA_REPO" "infrastructure/database" "database-infrastructure"
clone_repo "$KAFKA_INFRA_REPO" "infrastructure/kafka" "kafka-infrastructure"
clone_repo "$PGADMIN_INFRA_REPO" "infrastructure/pgadmin" "pgadmin-infrastructure"

clone_repo "$SCENARIO_SEEDER_TOOL_REPO" "tools/scenario-seeder" "scenario-seeder"

echo -e "${GREEN}Done.${NC} Next: create .env (see deploy/gcp/README.md), then:"
echo "  docker compose -f docker-compose.yml -f deploy/gcp/compose.prod.yml up -d --build"
echo "  docker compose --profile seed run --rm scenario-seeder all   # optional, seeds demo data"
