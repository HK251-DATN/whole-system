# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

E-commerce platform built with microservices architecture, event-driven communication via Kafka, and PostgreSQL databases. The system handles user identity, product storage/inventory, back-office operations, and e-commerce transactions.

## Repository Structure

```
project-root/
├── services/               # 4 microservices
│   ├── identity-service/          # Port 9000 - User authentication & authorization
│   ├── back-office-service/       # Port 9100 - Product & category management
│   ├── product_storage_service/   # Port 9200 - Warehouse & inventory management
│   └── ecommerce-service/         # Port 9301 - Orders & sales (Clean Architecture)
├── infrastructure/         # Shared infrastructure
│   ├── database/          # PostgreSQL 17 multi-database setup
│   └── kafka/             # Kafka 4.2.0 + Kafka UI
└── frontend/              # (Currently empty)
```

## Quick Start

### Option 1: Docker Compose (Recommended)

**Start entire platform with one command:**
```bash
# 1. Copy environment template
cp .env.example .env

# 2. Edit .env and add your Cloudflare R2 credentials

# 3. Start all services
./start.sh          # Linux/Mac
start.bat           # Windows

# OR use Docker Compose directly
docker-compose up -d --build
```

This starts:
- PostgreSQL (4 databases)
- Kafka + Kafka UI
- All 4 microservices

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

### ecommerce-service (Port 9301)
- **Tech**: Spring Boot 3.5.6, Java 21, Clean Architecture pattern
- **Database**: ecommerce_db_2
- **Responsibilities**: Order processing, sales transactions, pricing
- **Kafka Consumers**: `batch-detail-events` (from product storage)
- **Architecture**: Clean Architecture (domain → use cases → infrastructure)
  - `domain/` - Pure business logic (entities, use cases, services)
  - `persistence/` - Database access (repositories, DTOs)
  - `infrastructure/` - Framework concerns (config, messaging)
  - `presentation/` - REST API (controllers, request/response)

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

## Testing

**Run all tests in a service:**
```bash
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=ClassNameTest
```

**Run with coverage:**
```bash
./mvnw clean verify
```

Tests use Spring Boot Test framework with JPA and WebMVC testing support.

## External Dependencies

### Cloudflare R2 (Object Storage)
All services integrate with Cloudflare R2 for file storage:
- Endpoint: `https://{account-id}.r2.cloudflarestorage.com`
- Required env vars: `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`
- Public bucket URLs configured per service

### Database Connection
PostgreSQL connection via environment variables:
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- Each service connects to its own database

### Kafka Connection
- `KAFKA_HOST`, `KAFKA_PORT`
- Bootstrap servers: `{KAFKA_HOST}:{KAFKA_PORT}`

## Multi-Service Development

When working across services:

1. **Start infrastructure first**: PostgreSQL + Kafka
2. **Order matters**: Start services in dependency order:
   - identity-service (no dependencies)
   - back-office-service (no dependencies)
   - product_storage_service (depends on back-office events)
   - ecommerce-service (depends on product storage events)
3. **Check Kafka UI** (localhost:9280) to verify event flow
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
- 9000, 9100, 9200, 9301: Microservices

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
