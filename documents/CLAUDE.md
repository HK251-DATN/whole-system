# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

E-commerce platform built with microservices architecture, event-driven communication via Kafka, and PostgreSQL databases. The system handles user identity, product storage/inventory, back-office operations, and e-commerce transactions.

## Repository Structure

**Repository**: `ecommerce-microservices-platform`

```
ecommerce-microservices-platform/
├── services/               # 4 microservices
│   ├── identity-service/          # Port 9000 - User authentication & authorization
│   ├── back-office-service/       # Port 9100 - Product & category management
│   ├── product_storage_service/   # Port 9200 - Warehouse & inventory management
│   └── ecommerce-service/         # Port 9300 - Orders & sales (Clean Architecture)
├── infrastructure/         # Shared infrastructure
│   ├── database/          # PostgreSQL 17 multi-database setup
│   └── kafka/             # Kafka 4.2.0 + Kafka UI
└── frontend/              # React-based admin and customer UIs
    ├── ecommerce-ui/      # Customer-facing e-commerce app (React + Vite)
    ├── back-office-ui/    # Admin interface (React + Vite + MUI + Ant Design)
    └── provider-ui/       # Farmer/supplier portal (React + Vite + Ant Design)
```

## Quick Start

## First-Time Repository Setup

**IMPORTANT**: This is a monorepo that orchestrates 8 separate Git repositories.

Before using Docker Compose or manual setup, clone all service repositories:

```bash
# Clone all microservice, infrastructure, and frontend repositories
./setup-repos.sh    # Linux/Mac
setup-repos.bat     # Windows
```

This creates:
- `services/` - 4 microservice repositories
- `infrastructure/` - 2 infrastructure repositories  
- `frontend/` - 2 frontend repositories (ecommerce-ui, back-office-ui)
  - Note: `provider-ui` is part of the main repo and not cloned separately

**Repository URLs** are configured in `setup-repos.sh` (already filled in for HK251-DATN organization).

### Option 1: Docker Compose (Recommended)

**Start entire platform:**
```bash
# 1. Clone all repositories (first time only)
./setup-repos.sh

# 2. Copy environment template
cp .env.example .env
# Edit .env and add your Cloudflare R2 credentials

# 3. Build JARs locally (avoids Docker network issues)
./build-local.sh    # Linux/Mac
build-local.bat     # Windows

# 4. Start all services
./start.sh          # Linux/Mac
start.bat           # Windows
```

This starts:
- PostgreSQL (4 databases) + pgAdmin
- Kafka + Kafka UI
- All 4 microservices
- Frontend apps run locally (see Frontend Development Workflow)

**Start script subcommands:**
```bash
./start.sh          # Start all services
./start.sh logs     # View all logs
./start.sh logs identity-service  # View specific service logs
./start.sh rebuild  # Rebuild all services
./start.sh stop     # Stop all services
./start.sh clean    # Stop and remove containers/volumes
```

For individual service management, use `service.sh` instead (see Development Workflow section).

**See `DOCKER_SETUP.md` for complete Docker documentation.**

### Option 2: Manual Setup (Development)

**Infrastructure:**
```bash
# Start PostgreSQL (4 databases)
cd infrastructure/database
./setup.sh  # Linux/Mac OR setup.bat for Windows

# Start Kafka
cd infrastructure/kafka
docker-compose up -d
```

Creates: `identity_db`, `back_office_db`, `product_storage_db`, `ecommerce_db` (Port 5432)
Kafka topics: `user-events`, `order-events`, `order-item-events`, `payment-events`, etc.

**Each service:**

1. Copy `.env.example` to `.env` in service directory
2. Add credentials (R2, DB, Kafka)
3. Build and run:

```bash
cd services/{service-name}

# Build
./mvnw clean install

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run

# Run without tests
./mvnw clean install -DskipTests
```

**All services use Maven Wrapper (`./mvnw`)** - no global Maven needed.

### Environment File Locations

**Root `.env` file** (Docker Compose variables):
- Location: `/project-root/.env`
- Used by: Docker Compose to configure all services
- Required for: Docker-based deployment

**Service-specific `.env` files** (Local development):
- Location: `services/{service-name}/.env`
- Used by: Individual service when running locally with `./mvnw spring-boot:run`
- Copy from: `.env.example` in each service directory
- Required for: Manual/local development only

**Which to use:**
- Docker deployment: Only need root `.env`
- Local development: Need both root `.env` (for infrastructure) and service-specific `.env`

## Service Details

### identity-service (Port 9000)
- **Tech**: Spring Boot 4.0.1, Java 25, Spring Security, OAuth2
- **Database**: identity_db
- **Responsibilities**: User authentication, authorization, session management
- **Architecture**: Layered (controller → service → repository → dao)
- **Cloudflare R2**: Stores user avatars in `back-office-user-avts` bucket

### back-office-service (Port 9100)
- **Tech**: Spring Boot 4.0.2, Java 25, Spring Security
- **Database**: back_office_db
- **Responsibilities**: Product catalog management, category hierarchy (3-layer: category → subcategory → subsubcategory), product general information
- **Kafka Producers**: `category-events`, `subsubcategory-events`, `product-general-events`
- **Cloudflare R2**: 
  - User avatars: `back-office-user-avts`
  - Product images: `product-general-img`

### product_storage_service (Port 9200)
- **Tech**: Spring Boot 4.0.3, Java 25
- **Database**: product_storage_db
- **Responsibilities**: Warehouse management, storage tools (racks/fridges), product inventory, batch processing
- **Kafka Consumers**: `product-general-events`, `subsubcategory-events`, `order-item-events`
- **Kafka Producers**: `batch-detail-events`
- **Key Logic**: Batch processing with unit conversion (mass/volume) to create product details
- **See**: `services/product_storage_service/CLAUDE.md` for detailed architecture

### ecommerce-service (Port 9300)
- **Tech**: Spring Boot 3.5.6, Java 21, Clean Architecture pattern
- **Database**: ecommerce_db_2
- **Responsibilities**: Order processing, sales transactions, pricing
- **Authentication**: JWT-based (via custom filter) - different from identity-service's OAuth2
- **Kafka Consumers**: `batch-detail-events` (from product storage)
- **Architecture**: Clean Architecture (domain → use cases → infrastructure)
  - `domain/` - Pure business logic (entities, use cases, services)
  - `persistence/` - Database access (repositories, DTOs)
  - `infrastructure/` - Framework concerns (config, messaging)
  - `presentation/` - REST API (controllers, request/response)
- **Data Seeder**: Automatically populates database with Vietnamese fresh food sample data on first run:
  - 5 buyers with addresses
  - 18 product categories (hierarchical)
  - 15 fresh food products (fruits, vegetables, meat, seafood, dairy)
  - 15 batch details with pricing
  - 3 active sale events with discounts
  - Shopping carts and product reviews in Vietnamese
  - Seeder only runs when database is empty (checks `buyer` table)

## API Endpoints & Documentation

### Service Health Checks

All Spring Boot services expose actuator endpoints:

```bash
# Check service health
curl http://localhost:9000/actuator/health  # identity-service
curl http://localhost:9100/actuator/health  # back-office-service
curl http://localhost:9200/actuator/health  # product-storage-service
curl http://localhost:9300/actuator/health  # ecommerce-service
```

### Common API Patterns

All services follow RESTful conventions:

**Standard CRUD endpoints:**
- `GET /api/{resource}` - List all (paginated)
- `GET /api/{resource}/{id}` - Get by ID
- `POST /api/{resource}` - Create
- `PUT /api/{resource}/{id}` - Update
- `DELETE /api/{resource}/{id}` - Delete

**Pagination:**
- Query params: `?pageNum=0&pageSize=20`
- Page numbering: 0-based index
- Default page size: varies by service (typically 10-20)

**Example API calls:**

```bash
# Identity Service - Get user info
curl http://localhost:9000/api/users/1

# Back-Office Service - List products
curl http://localhost:9100/api/product-general?pageNum=0&pageSize=10

# Product Storage - Get warehouse inventory
curl http://localhost:9200/api/warehouses/1

# Ecommerce Service - Get cart
curl -H "Authorization: Bearer {jwt_token}" \
  http://localhost:9300/api/cart/buyer/1
```

**Authentication:**
- Identity/Back-Office/Product Storage: OAuth2 (check service-specific docs)
- Ecommerce Service: JWT Bearer token (custom filter)

## Frontend Applications

### back-office-ui
- **Tech**: React 19, Vite, Material-UI, Ant Design, Redux Toolkit, React Query
- **Purpose**: Admin interface for product/category management
- **Dev server**: `npm run dev` (typically port 5173)

### ecommerce-ui  
- **Tech**: React, Vite
- **Purpose**: Customer-facing e-commerce application
- **Dev server**: `npm run dev`

### provider-ui
- **Tech**: React 19, Vite, TypeScript, Ant Design, TanStack Query, Zustand
- **Purpose**: Portal for farmers/suppliers to manage products, view transactions, and track demand
- **Dev server**: `npm run dev` (port 5273)
- **UI Language**: Vietnamese (target users: non-technical farmers/vendors)
- **See**: `frontend/provider-ui/CLAUDE.md` for detailed architecture

**Note**: ecommerce-ui and back-office-ui repos are cloned via `setup-repos.sh`. provider-ui is part of the main repo.

### Frontend Development Workflow

**Local Development (recommended):**
```bash
cd frontend/back-office-ui  # or frontend/ecommerce-ui or frontend/provider-ui
npm install
npm run dev
```

**Important**: Frontend services are **commented out** in `docker-compose.yml` by default. Run them locally for development.

**Access URLs (local dev):**
- Ecommerce UI: http://localhost:3000 (or Vite's default port)
- Back-Office UI: http://localhost:5173
- Provider UI: http://localhost:5273

**To enable Docker deployment** (optional):
Uncomment the frontend service sections in `docker-compose.yml`, then:
```bash
./service.sh rebuild ecommerce-ui      # Rebuild ecommerce UI
./service.sh rebuild back-office-ui    # Rebuild back-office UI
./service.sh logs ecommerce-ui         # View ecommerce UI logs
./service.sh logs back-office-ui       # View back-office UI logs
```

**Stack:**
- React 19, Vite, TypeScript (ecommerce-ui uses JavaScript)
- State: Redux Toolkit, React Query
- UI: Material-UI (back-office), Ant Design (back-office)
- API calls integrate with backend services on ports 9000-9300
- Production builds served via Nginx in Docker containers

### Frontend-Backend Integration

**API Base URLs:**

Development (local):
```javascript
// ecommerce-ui typically uses
const API_BASE_URL = 'http://localhost:9300/api'

// back-office-ui typically uses
const IDENTITY_API = 'http://localhost:9000/api'
const BACKOFFICE_API = 'http://localhost:9100/api'
const STORAGE_API = 'http://localhost:9200/api'
```

**CORS Configuration:**
All backend services are configured to allow frontend origins. Default CORS settings in `config/CorsConfig.java`:
- Allowed origins: `http://localhost:3000`, `http://localhost:5173`
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials: allowed

**Development Proxy (Vite):**
If needed, configure proxy in `vite.config.js`:
```javascript
export default {
  server: {
    proxy: {
      '/api': 'http://localhost:9300'
    }
  }
}
```

## Event-Driven Architecture

**Kafka Event Flow:**

```
Back-Office Service
  └─> category-events
  └─> subsubcategory-events ──┐
  └─> product-general-events ─┼──> Product Storage Service
                               │     └─> batch-detail-events ──> Ecommerce Service
  Order Item Events ───────────┘

Identity Service
  └─> user-events
```

**Event Processing Pattern:**
- Services consume events via `@KafkaListener`
- Events trigger domain logic in service layer
- Failures logged, no automatic retry (consumer must handle)
- All topics use 3 partitions, replication factor 1

### Kafka Event Payload Examples

**product-general-events** (Back-Office → Product Storage):
```json
{
  "eventType": "PRODUCT_CREATED",
  "productId": 123,
  "name": "Fresh Apple",
  "subSubCategoryId": 45,
  "unit": "MASS",
  "timestamp": "2026-04-30T10:00:00Z"
}
```

**batch-detail-events** (Product Storage → Ecommerce):
```json
{
  "eventType": "BATCH_DETAIL_CREATED",
  "batchDetailId": 789,
  "productGeneralId": 123,
  "quantity": 50,
  "unitPrice": 25000,
  "packageSize": 500,
  "packageUnit": "GRAM",
  "expirationDate": "2026-05-15",
  "warehouseId": 1
}
```

**order-item-events** (Ecommerce → Product Storage):
```json
{
  "eventType": "ORDER_PLACED",
  "orderId": 456,
  "items": [
    {
      "batchDetailId": 789,
      "quantity": 5
    }
  ]
}
```

**Event Monitoring:**
- Kafka UI: http://localhost:9280
- View messages, consumer lag, partition distribution
- Useful for debugging event flow issues

## Common Development Patterns

### Service Layer Architecture

**Most services** (identity, back-office, product-storage):
```
edu.hcmut.datn.{service}/
├── common/           # Enums, constants
├── config/           # Spring configuration, CORS, security
├── controller/       # REST endpoints
├── dao/              # JPA entities (database models)
├── dto/              # Request/Response DTOs
├── exception/        # Custom exceptions
├── messaging/        # Kafka producers/consumers
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic
│   └── impl/         # Service implementations
└── util/             # Utilities
```

**Ecommerce service** (Clean Architecture):
```
microservice.base_source/
├── domain/           # Pure business logic
│   ├── entity/       # Domain entities (NOT JPA)
│   ├── use_case/     # Use case interfaces
│   └── service/      # Domain services
├── persistence/      # Data access
│   ├── repository/   # JPA repositories
│   └── dto/          # Database DTOs
├── infrastructure/   # Framework
│   ├── configuration/
│   ├── messaging/    # Kafka
│   └── security/
└── presentation/     # API layer
    ├── rest/         # Controllers
    ├── request/
    ├── response/
    └── mapping/      # Request/response mappers
```

### Standard CRUD Service Pattern

All services implement similar CRUD interfaces:
- `create(T entity)` - Create new entity
- `read(Long id)` - Read by ID (throws `*NotFoundException` if not found)
- `readAll(Integer pageNum, Integer pageSize)` - Paginated results (0-based index)
- `update(Long id, T entity)` - Partial update (null fields ignored)
- `delete(Long id)` - Delete entity

Service implementations use constructor injection via `@AllArgsConstructor` (Lombok).

### Entity Conventions

- Most entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment IDs
- Exception: ProductGeneral uses manually assigned IDs from back-office service
- All entities auto-manage `created_at` and `updated_at` via `@PrePersist` and `@PreUpdate`
- Selective `@Getter`/`@Setter` on fields (not class-level) to control access

### Exception Handling

Custom exceptions follow naming: `{Entity}NotFoundException`, `{Entity}AlreadyExistsException`
- Thrown from service layer (not repositories)
- Caught in global exception handlers

## Database Schema Locations

Each service has database schema documentation:
- `services/identity-service/db_schema/README.md`
- `services/back-office-service/db_scheme/README.md`
- `services/product_storage_service/db_scheme/README.md`
- `services/ecommerce-service/db_schema/README.md`

Schemas are managed via JPA with `hibernate.ddl-auto: update`.

## Service-Specific Documentation

Each service has detailed CLAUDE.md files with service-specific architecture and patterns:
- `services/identity-service/CLAUDE.md` - Detailed identity service architecture
- `services/back-office-service/CLAUDE.md` - Back-office service specifics
- `services/product_storage_service/CLAUDE.md` - Storage hierarchy and batch processing details
- `services/ecommerce-service/CLAUDE.md` - Clean architecture patterns and use cases

## Testing

**Run all tests in a service:**
```bash
cd services/{service-name}
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=ClassNameTest
```

**Run specific test method:**
```bash
./mvnw test -Dtest=ClassNameTest#methodName
```

**Run with coverage:**
```bash
./mvnw clean verify
```

**Skip tests during build:**
```bash
./mvnw clean install -DskipTests
```

Tests use Spring Boot Test framework with JPA and WebMVC testing support.

### Integration Testing

**Test Kafka consumers/producers:**
```bash
# Use embedded Kafka for tests
# Most services use @EmbeddedKafka annotation in test classes
./mvnw test -Dtest=*KafkaTest
```

**Test with running Docker services:**
```bash
# Start infrastructure first
./start.sh up

# Run integration tests against live services
cd services/identity-service
./mvnw verify -P integration-test
```

**Test API endpoints:**
```bash
# Use Spring MockMvc for controller tests
./mvnw test -Dtest=*ControllerTest

# Or test against running service
curl -X POST http://localhost:9000/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

## Development Workflow - Individual Service Management

When working on a single service, use `service.sh` to quickly rebuild/restart:

```bash
# Rebuild and restart after code changes (recommended workflow)
./service.sh rebuild identity           # Apply changes to identity service
./service.sh rebuild back-office        # Apply changes to back-office
./service.sh rebuild product-storage    # Apply changes to product storage
./service.sh rebuild ecommerce          # Apply changes to ecommerce

# Other useful commands
./service.sh logs identity              # View logs (follow mode)
./service.sh restart identity           # Restart without rebuilding
./service.sh stop identity              # Stop a service
./service.sh start identity             # Start a service
./service.sh status identity            # Show service status
./service.sh exec identity              # Open bash inside container
```

**Service aliases supported:**
- `identity`, `back-office`/`backoffice`, `product-storage`/`product`, `ecommerce`
- `ecommerce-ui`, `back-office-ui`/`backoffice-ui`, `provider-ui` (if enabled in docker-compose)
- `postgres`/`db`, `pgadmin`, `kafka`, `kafka-ui`

**Common workflow:**
1. Edit code in `services/{service-name}/`
2. Run `./service.sh rebuild {service-name}`
3. Run `./service.sh logs {service-name}` to verify startup

### When to Rebuild vs Restart

**Rebuild required** (code changes):
```bash
./service.sh rebuild {service-name}
```

**Restart sufficient** (config/.env changes):
```bash
./service.sh restart {service-name}
```

**Just view logs** (no changes):
```bash
./service.sh logs {service-name}
```

### Hot Reload / Development Mode

**Spring Boot DevTools** (automatic restart on code changes):

Most services include DevTools dependency. To enable:

```bash
# Run service in dev mode
cd services/identity-service
./mvnw spring-boot:run

# Edit code - service auto-restarts when you save
```

**Faster rebuild cycle:**
```bash
# Skip tests for faster builds during development
./mvnw clean package -DskipTests spring-boot:run
```

**Frontend hot reload:**
```bash
# Vite automatically hot-reloads on file changes
cd frontend/ecommerce-ui
npm run dev
# Edit .jsx files - browser auto-updates
```

**Docker hot reload:**
Docker doesn't support hot reload by default. For faster iteration:
1. Use local development (`./mvnw spring-boot:run`) for code changes
2. Use Docker only for integration testing
3. When ready, rebuild with `./service.sh rebuild {service-name}`

## External Dependencies

### Cloudflare R2 (Object Storage)
All services integrate with Cloudflare R2 for file storage:
- Endpoint: `https://{account-id}.r2.cloudflarestorage.com`
- Required env vars: `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`
- Public bucket URLs configured per service

### Goong Maps API
Vietnamese mapping and geocoding service used by ecommerce-service:
- Required env var: `GOONG_API_KEY`
- Get API key from: https://goong.io/
- Used for: Address validation, delivery route calculation

### Database Connection
PostgreSQL connection via environment variables:
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- Each service connects to its own database
- **pgAdmin**: Web UI for database administration at http://localhost:5480
  - Default email: `admin@ecommerce.local`
  - Default password: `admin`

### Kafka Connection
- `KAFKA_HOST`, `KAFKA_PORT`
- Bootstrap servers: `{KAFKA_HOST}:{KAFKA_PORT}`

## Inter-Service Communication

Services communicate through **two patterns**:

### 1. Event-Driven (Kafka)
Asynchronous messaging for domain events (see Event-Driven Architecture section).

### 2. REST API Calls (Synchronous)
Some services make direct HTTP calls to other services. Configured via environment variables:

```bash
# In docker-compose.yml or service .env files
IDENTITY_HOST=identity-service      # or localhost for local dev
IDENTITY_PORT=9000
BACK_OFFICE_HOST=back-office-service
BACK_OFFICE_PORT=9100
PRODUCT_STORAGE_HOST=product-storage-service
PRODUCT_STORAGE_PORT=9200
ECOMMERCE_HOST=ecommerce-service
ECOMMERCE_PORT=9300
```

**Key points:**
- When running in Docker, use service names as hostnames (e.g., `identity-service`)
- For local development outside Docker, use `localhost`
- Services construct URLs like: `http://${IDENTITY_HOST}:${IDENTITY_PORT}/api/...`

## Multi-Service Development

When working across services:

1. **Start infrastructure first**: PostgreSQL + Kafka
2. **Order matters**: Start services in dependency order:
   - identity-service (no dependencies)
   - back-office-service (no dependencies)
   - product_storage_service (depends on back-office events)
   - ecommerce-service (depends on product storage events)
3. **Check Kafka UI** (http://localhost:9280) to verify event flow
4. **Different Spring Boot versions**: Be aware that ecommerce-service uses 3.5.6 (Java 21) while others use 4.x (Java 25)

## Key Architectural Decisions

### Why Clean Architecture in Ecommerce Service?
The ecommerce service uses Clean Architecture (hexagonal) while others use layered architecture. This isolates domain logic from framework concerns, making business rules testable without Spring dependencies.

### Why Different Spring Boot Versions?
- Ecommerce service: Spring Boot 3.5.6 (stable, Java 21)
- Other services: Spring Boot 4.x (newer, Java 25)
- Reason: Likely migration in progress or different team preferences

### Category Hierarchy
3-layer category system managed by back-office service:
- Category → Subcategory → SubSubcategory
- SubSubcategory is the finest granularity linked to products

### Batch Processing Logic
Product storage service performs critical batch-to-detail conversion:
- ProductBatch (bulk quantity) → Multiple ProductDetails (sellable units)
- Unit conversion supports MASS (kg/g) and VOLUME (L/mL)
- Example: 10kg batch + 500g package = 20 sellable products

## Troubleshooting

**Port conflicts:**
- 5432: PostgreSQL
- 9092: Kafka
- 9280: Kafka UI
- 5480: pgAdmin (PostgreSQL admin UI)
- 9000, 9100, 9200, 9300: Microservices
- 3000: Ecommerce UI
- 5173: Back-Office UI

**Kafka consumer not receiving events:**
1. Check Kafka UI (localhost:9280) - verify topic exists
2. Verify producer successfully published (check logs)
3. Ensure consumer group ID is unique or correct
4. Check for serialization/deserialization errors

**Database connection failures:**
1. Verify PostgreSQL is running: `docker ps | grep postgres`
2. Check `.env` file has correct credentials
3. Verify database exists: `docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"`

**Build failures:**
- Ensure Java 25 installed for identity/back-office/product-storage services
- Ensure Java 21 installed for ecommerce service
- Clear Maven cache: `./mvnw clean`

## Common Pitfalls & Quick Fixes

### 1. Maven Build Fails in Docker
**Symptom:** "Connection refused" to Maven repositories during `docker-compose up`

**Solution:** Build JARs locally first
```bash
./build-local.sh    # Linux/Mac
build-local.bat     # Windows
```
This is the **recommended workflow** - Docker then just copies pre-built JARs.

### 2. Service Starts Before Dependencies
**Symptom:** Service crashes with "Connection refused" to database or Kafka

**Solution:** Wait for health checks
```bash
# Check all services are healthy
docker-compose ps

# Restart the failed service after dependencies are ready
./service.sh restart {service-name}
```

### 3. Kafka Events Not Flowing
**Symptom:** Product created in back-office but not appearing in product-storage

**Debug steps:**
1. Check Kafka UI (http://localhost:9280) - verify topic exists
2. Check producer logs: `./service.sh logs back-office`
3. Check consumer logs: `./service.sh logs product-storage`
4. Verify event was published: Kafka UI → Topics → Messages tab

**Common cause:** Consumer started before topic was created. Restart consumer.

### 4. Frontend Can't Connect to Backend
**Symptom:** CORS errors or network errors in browser console

**Check:**
1. Backend service is running: `curl http://localhost:9300/actuator/health`
2. CORS config allows frontend origin (check `CorsConfig.java`)
3. Frontend API URL is correct (check `.env` or config files)

### 5. R2 File Upload Fails
**Symptom:** 403 Forbidden or connection errors

**Check `.env` file:**
```bash
# Verify credentials are set
cat .env | grep R2_

# Test with minimal credentials
R2_ACCOUNT_ID=your_id
R2_ACCESS_KEY=your_key
R2_SECRET_KEY=your_secret
```

**Common mistake:** Forgetting to copy `.env.example` to `.env`

### 6. Database Schema Mismatch
**Symptom:** SQL errors about missing columns or tables

**Solution:** Let Hibernate update schema
```bash
# Check application.yaml has:
# spring.jpa.hibernate.ddl-auto: update

# Or reset database (WARNING: deletes data)
docker-compose down -v
docker-compose up -d postgres
```

### 7. Service Dependency Order Issues
**Remember the flow:**
1. Infrastructure: PostgreSQL, Kafka
2. Independent services: identity-service, back-office-service
3. Dependent services: product_storage_service (needs back-office events)
4. Final service: ecommerce-service (needs product-storage events)

**If services start out of order:**
```bash
# Restart in correct order
./service.sh restart back-office
sleep 5
./service.sh restart product-storage
sleep 5
./service.sh restart ecommerce
```

### 8. Port Already in Use
**Quick check:**
```bash
# Linux/Mac
sudo lsof -i :5432

# Windows
netstat -ano | findstr :5432
```

**Quick fix:** Stop conflicting service or change port in `docker-compose.yml`

---

**For more detailed troubleshooting, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)**
