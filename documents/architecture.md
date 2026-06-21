# E-Commerce Microservices Platform - Architecture Report

**HCMUT Capstone Project 2026**  
**Date:** May 2, 2026  
**Version:** 1.0 (Draft)

---

## Executive Summary

This document describes the architecture of an e-commerce platform built using microservices architecture, event-driven communication, and domain-driven design principles. The system supports fresh food and vegetable sales, connecting farmers/suppliers with customers through an online marketplace.

**Key Characteristics:**
- 4 independent microservices (Identity, Back-Office, Product Storage, Ecommerce)
- Event-driven communication via Apache Kafka
- Database-per-service pattern with PostgreSQL
- 3 frontend applications for different user types
- Containerized deployment with Docker Compose

---

## 1. System Overview

### 1.1 Purpose

The platform enables:
- **Customers**: Browse and purchase fresh food products online
- **Administrators**: Manage product catalog, categories, and inventory
- **Suppliers/Farmers**: Track demand, manage deliveries, view transactions

### 1.2 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Ecommerce UI │  │Back-Office UI│  │ Provider UI  │          │
│  │ (Customers)  │  │   (Admins)   │  │  (Farmers)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY LAYER                           │
│                    (Direct service access)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Identity   │      │ Back-Office  │      │   Product    │
│   Service    │◄────►│   Service    │─────►│   Storage    │
│   :9000      │      │   :9100      │      │   Service    │
└──────────────┘      └──────────────┘      │   :9200      │
        │                     │              └──────────────┘
        │                     │                     │
        │                     ▼                     ▼
        │              ┌────────────────────────────────┐
        │              │      Apache Kafka             │
        │              │   (Event Streaming Bus)       │
        │              └────────────────────────────────┘
        │                            │
        │                            ▼
        │                     ┌──────────────┐
        └────────────────────►│  Ecommerce   │
                              │   Service    │
                              │   :9300      │
                              └──────────────┘
                                     │
                              ┌──────────────┐
                              │ Goong Maps   │
                              │     API      │
                              └──────────────┘
```

---

## 2. Architecture Patterns

### 2.1 Microservices Architecture

The system is decomposed into 4 independent services, each with:
- **Single Responsibility**: Each service handles one business domain
- **Independent Deployment**: Services can be deployed and scaled separately
- **Technology Diversity**: Different Spring Boot versions (3.5.6 vs 4.0.x) and Java versions (21 vs 25)
- **Database Isolation**: Each service has its own PostgreSQL database

### 2.2 Event-Driven Architecture

Services communicate asynchronously through Apache Kafka:
- **Loose Coupling**: Services don't need to know about each other's internal implementation
- **Eventual Consistency**: Data synchronization happens through domain events
- **Scalability**: Event consumers can scale independently
- **Resilience**: Services continue operating even if others are temporarily unavailable

### 2.3 Clean Architecture (Ecommerce Service)

The ecommerce service implements Clean Architecture principles:
- **Domain Layer**: Pure business logic, no framework dependencies
- **Use Case Layer**: Application-specific business rules
- **Infrastructure Layer**: Framework concerns (Spring, Kafka, databases)
- **Presentation Layer**: REST API controllers and DTOs

---

## 3. Service Details

### 3.1 Identity Service (Port 9000)

**Responsibilities:**
- User authentication and authorization
- Session management
- OAuth2 integration
- User profile management

**Technology Stack:**
- Spring Boot 4.0.1
- Java 25
- Spring Security
- OAuth2

**Database:** `identity_db`

**External Dependencies:**
- Cloudflare R2 (user avatar storage)

**Communication:**
- Publishes: `user-events`
- REST endpoints for other services

---

### 3.2 Back-Office Service (Port 9100)

**Responsibilities:**
- Product catalog management
- 3-tier category hierarchy (Category → Subcategory → SubSubcategory)
- Product general information (name, description, unit type)
- Image management

**Technology Stack:**
- Spring Boot 4.0.2
- Java 25
- Spring Security

**Database:** `back_office_db`

**External Dependencies:**
- Cloudflare R2 (product images, user avatars)

**Communication:**
- Publishes: `category-events`, `subsubcategory-events`, `product-general-events`
- REST API for admin UI

---

### 3.3 Product Storage Service (Port 9200)

**Responsibilities:**
- Warehouse management
- Storage tool management (racks, fridges)
- Inventory tracking
- **Batch processing**: Converts bulk batches into sellable product details
- Unit conversion (mass: kg/g, volume: L/mL)

**Technology Stack:**
- Spring Boot 4.0.3
- Java 25

**Database:** `product_storage_db`

**Key Business Logic:**
```
ProductBatch (10kg) + PackageSize (500g) 
  → 20 ProductDetails (500g each)
  → Published to ecommerce service
```

**Communication:**
- Consumes: `product-general-events`, `subsubcategory-events`, `order-item-events`
- Publishes: `batch-detail-events`
- REST endpoints for back-office UI

---

### 3.4 Ecommerce Service (Port 9300)

**Responsibilities:**
- Order processing
- Shopping cart management
- Sales transactions
- Pricing logic
- Sale events and promotions
- Customer reviews

**Technology Stack:**
- Spring Boot 3.5.6
- Java 21
- Clean Architecture pattern
- JWT authentication (custom filter)

**Database:** `ecommerce_db_2`

**External Dependencies:**
- Goong Maps API (Vietnamese address validation, delivery routes)
- Cloudflare R2 (product images from other services)

**Communication:**
- Consumes: `batch-detail-events`
- REST API for ecommerce UI

**Special Features:**
- Data seeder with Vietnamese sample data (fruits, vegetables, meat, seafood)
- Hierarchical product categories
- Sale event management

---

## 4. Data Architecture

### 4.1 Database Strategy

**Pattern:** Database-per-Service

Each microservice has an isolated PostgreSQL database:

| Service | Database | Purpose |
|---------|----------|---------|
| Identity | `identity_db` | User accounts, sessions, credentials |
| Back-Office | `back_office_db` | Product catalog, categories |
| Product Storage | `product_storage_db` | Warehouses, inventory, batches |
| Ecommerce | `ecommerce_db_2` | Orders, carts, sales, reviews |

**Benefits:**
- Service independence
- Technology flexibility
- Fault isolation
- Scalability

**Challenges:**
- No foreign keys across services
- Data consistency through events
- Distributed transactions avoided (eventual consistency)

### 4.2 Database Management

- **ORM:** JPA/Hibernate
- **Schema Management:** `hibernate.ddl-auto: update` (auto-migration)
- **Versioning:** PostgreSQL 17
- **Admin Tool:** pgAdmin (http://localhost:5480)

---

## 5. Communication Patterns

### 5.1 Event-Driven Communication (Kafka)

**Event Flow:**

```
┌─────────────────┐
│ Back-Office     │
│   Service       │
└────────┬────────┘
         │
         ├──► category-events ────────────┐
         ├──► subsubcategory-events ──────┤
         └──► product-general-events ─────┤
                                          │
                                          ▼
                                 ┌─────────────────┐
                                 │ Product Storage │
                                 │    Service      │
                                 └────────┬────────┘
                                          │
                                          ├──► batch-detail-events
                                          │
                                          ▼
                                 ┌─────────────────┐
                                 │   Ecommerce     │
                                 │    Service      │
                                 └────────┬────────┘
                                          │
                  order-item-events ◄─────┘
                         │
                         └──► Product Storage Service
```

**Kafka Configuration:**
- Broker: Apache Kafka 4.2.0
- Topics: 9 predefined topics (user-events, order-events, category-events, etc.)
- Partitions: 3 per topic
- Replication Factor: 1 (development setup)
- Monitoring: Kafka UI (http://localhost:9280)

**Event Processing:**
- Consumers: `@KafkaListener` annotations
- No automatic retry (services handle failures)
- Error logging for debugging

### 5.2 Synchronous REST Communication

Services also make direct HTTP calls when immediate consistency is required:

**Configuration:**
```bash
IDENTITY_HOST=identity-service
IDENTITY_PORT=9000
BACK_OFFICE_HOST=back-office-service
BACK_OFFICE_PORT=9100
PRODUCT_STORAGE_HOST=product-storage-service
PRODUCT_STORAGE_PORT=9200
ECOMMERCE_HOST=ecommerce-service
ECOMMERCE_PORT=9300
```

**Use Cases:**
- User authentication verification
- Real-time product availability checks
- Cross-service data validation

---

## 6. Frontend Applications

### 6.1 Ecommerce UI (Port 3000)

**Target Users:** Customers

**Technology:**
- React 19
- Vite
- JavaScript

**Features:**
- Product browsing and search
- Shopping cart
- Order placement
- User registration/login

---

### 6.2 Back-Office UI (Port 5173)

**Target Users:** Administrators

**Technology:**
- React 19
- Vite
- TypeScript
- Material-UI + Ant Design
- Redux Toolkit
- React Query

**Features:**
- Product catalog management
- Category hierarchy management
- Inventory monitoring
- User management

---

### 6.3 Provider UI (Port 5273)

**Target Users:** Farmers/Suppliers

**Technology:**
- React 19
- Vite
- TypeScript
- Ant Design (Vietnamese locale)
- TanStack Query
- Zustand (state management)

**Features:**
- Transaction history
- Demand tracking
- Profile management
- Dashboard analytics

**Design Principles:**
- All text in Vietnamese
- Large, readable fonts
- Responsive sizing (em/% units, not px)
- Simple navigation for non-technical users

---

## 7. External Integrations

### 7.1 Cloudflare R2 (Object Storage)

**Purpose:** S3-compatible object storage for files

**Buckets:**
- `back-office-user-avts`: User avatars
- `product-general-img`: Product images

**Access:**
- Account ID, Access Key, Secret Key
- Public bucket URLs for image serving

---

### 7.2 Goong Maps API

**Purpose:** Vietnamese mapping and geocoding service

**Use Cases:**
- Address validation
- Delivery route calculation
- Geolocation services

**Integration:** Ecommerce service only

---

## 8. Infrastructure

### 8.1 Containerization (Docker)

**Orchestration:** Docker Compose

**Services:**
- 4 microservices (identity, back-office, product-storage, ecommerce)
- PostgreSQL database
- Apache Kafka
- Kafka UI
- pgAdmin

**Networking:**
- Bridge network: `ecommerce-network`
- Service discovery via container names

**Volumes:**
- `postgres_data`: Database persistence
- `pgadmin_data`: pgAdmin configuration
- `kafka_data`: Kafka logs

### 8.2 Development Workflow

**Build Process:**
```bash
# 1. Build JARs locally (recommended)
./build-local.sh

# 2. Docker copies pre-built JARs
# (Avoids Maven network issues in containers)
```

**Individual Service Management:**
```bash
./service.sh rebuild identity    # Rebuild after code changes
./service.sh logs identity        # View logs
./service.sh restart identity     # Restart without rebuild
```

**Health Checks:**
- All services expose `/actuator/health` endpoints
- Docker Compose health checks ensure proper startup order

---

## 9. Key Architectural Decisions

### 9.1 Why Microservices?

**Decision:** Split system into 4 independent services

**Rationale:**
- **Scalability**: Scale services independently based on load
- **Team Autonomy**: Different teams can work on different services
- **Technology Flexibility**: Use different Spring Boot/Java versions per service
- **Fault Isolation**: Failure in one service doesn't crash entire system

**Trade-offs:**
- Increased complexity in deployment and monitoring
- Distributed data consistency challenges
- Inter-service communication overhead

---

### 9.2 Why Clean Architecture in Ecommerce Service Only?

**Decision:** Use Clean Architecture (hexagonal) in ecommerce service, layered architecture in others

**Rationale:**
- Ecommerce service has most complex business logic (pricing, promotions, order processing)
- Domain logic needs to be testable without Spring dependencies
- Easier to swap infrastructure (e.g., change database or message broker)

**Trade-offs:**
- More boilerplate code
- Steeper learning curve
- Other services kept simpler with traditional layered architecture

---

### 9.3 Why Different Spring Boot Versions?

**Current State:**
- Ecommerce: Spring Boot 3.5.6 (Java 21)
- Others: Spring Boot 4.0.x (Java 25)

**Possible Reasons:**
- Migration in progress
- Ecommerce service developed first, others upgraded later
- Compatibility with external libraries
- Team preferences

**Impact:**
- Need multiple JDK versions for local development
- Different dependency management
- Potential API incompatibilities

---

### 9.4 Why Event-Driven for Product Catalog?

**Decision:** Use Kafka events to sync product data across services

**Rationale:**
- **Loose Coupling**: Back-office service doesn't need to know about product-storage or ecommerce services
- **Resilience**: If a consumer is down, events are queued and processed later
- **Audit Trail**: Event log provides history of all catalog changes
- **Scalability**: Multiple consumers can process events in parallel

**Trade-offs:**
- Eventual consistency (not immediate)
- Debugging is harder (need to trace events across services)
- Requires Kafka infrastructure

---

### 9.5 Why 3-Tier Category Hierarchy?

**Decision:** Category → Subcategory → SubSubcategory

**Rationale:**
- Matches fresh food domain (e.g., Food → Vegetables → Leafy Greens)
- Flexible enough for diverse product types
- Not too deep (avoids UI complexity)

**Trade-offs:**
- Fixed depth (can't add more levels without code changes)
- All products must fit this hierarchy

---

## 10. Security Considerations

### 10.1 Authentication & Authorization

**Identity Service:**
- OAuth2 for token-based authentication
- Spring Security for endpoint protection

**Ecommerce Service:**
- Custom JWT filter (different from OAuth2)
- Bearer token validation

**Frontend Applications:**
- JWT stored in localStorage
- Axios interceptors auto-attach tokens
- 401 responses trigger logout and redirect

### 10.2 Data Security

**In Transit:**
- CORS configured for allowed frontend origins
- HTTPS recommended for production

**At Rest:**
- Database credentials in environment variables
- Secrets should use Docker secrets or external secret managers in production

**File Storage:**
- Cloudflare R2 with access key/secret key authentication
- Public bucket URLs for read-only image access

---

## 11. Scalability Considerations

### 11.1 Horizontal Scaling

Each service can scale independently:

```
          ┌─────────────┐
          │   Nginx     │
          │ (Load Bal)  │
          └──────┬──────┘
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
    ┌────┐   ┌────┐   ┌────┐
    │ S1 │   │ S2 │   │ S3 │  (Service instances)
    └────┘   └────┘   └────┘
```

**Stateless Services:**
- All microservices are stateless
- Session state stored in databases or tokens
- Easy to add/remove instances

### 11.2 Database Scaling

**Current:** Single PostgreSQL instance with multiple databases

**Future Options:**
- Read replicas for each database
- Connection pooling
- Database sharding for high-volume tables

### 11.3 Kafka Scaling

**Current:** 1 broker, 3 partitions per topic, replication factor 1

**Production Recommendations:**
- 3+ Kafka brokers for high availability
- Increase partitions for high-throughput topics
- Replication factor 3 for data durability

---

## 12. Monitoring & Observability

### 12.1 Current Tools

| Tool | Purpose | URL |
|------|---------|-----|
| Kafka UI | Monitor event flow, consumer lag | http://localhost:9280 |
| pgAdmin | Database administration | http://localhost:5480 |
| Spring Actuator | Health checks, metrics | `/actuator/health` on each service |

### 12.2 Recommended Additions

**Logging:**
- Centralized logging (ELK stack or similar)
- Distributed tracing (Zipkin, Jaeger)
- Correlation IDs across services

**Metrics:**
- Prometheus for metric collection
- Grafana for visualization
- Service-level metrics (requests/sec, latency, error rate)

**Alerting:**
- Alert on service health check failures
- Alert on high Kafka consumer lag
- Alert on database connection pool exhaustion

---

## 13. Deployment Architecture

### 13.1 Current (Development)

```
┌─────────────────────────────────────────┐
│         Developer Machine               │
│  ┌───────────────────────────────────┐  │
│  │     Docker Compose                │  │
│  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐     │  │
│  │  │ID  │ │BO  │ │PS  │ │EC  │     │  │
│  │  └────┘ └────┘ └────┘ └────┘     │  │
│  │  ┌────┐ ┌────┐                    │  │
│  │  │ PG │ │ KF │                    │  │
│  │  └────┘ └────┘                    │  │
│  └───────────────────────────────────┘  │
│                                          │
│  Frontend Apps (npm run dev)            │
│  ┌────┐ ┌────┐ ┌────┐                  │
│  │EC  │ │BO  │ │PR  │                  │
│  │UI  │ │UI  │ │UI  │                  │
│  └────┘ └────┘ └────┘                  │
└─────────────────────────────────────────┘
```

### 13.2 Recommended (Production)

```
┌──────────────────────────────────────────────────────┐
│              Cloud Platform (AWS/GCP/Azure)          │
│                                                       │
│  ┌────────────────────────────────────────────┐     │
│  │         Kubernetes Cluster                 │     │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │     │
│  │  │ ID   │ │ BO   │ │ PS   │ │ EC   │      │     │
│  │  │ Pod  │ │ Pod  │ │ Pod  │ │ Pod  │      │     │
│  │  └──────┘ └──────┘ └──────┘ └──────┘      │     │
│  │  (Auto-scaling based on CPU/memory)        │     │
│  └────────────────────────────────────────────┘     │
│                                                       │
│  ┌─────────────────┐  ┌─────────────────┐           │
│  │ Managed Kafka   │  │  Managed RDS    │           │
│  │  (MSK/Confluent)│  │  (PostgreSQL)   │           │
│  └─────────────────┘  └─────────────────┘           │
│                                                       │
│  ┌─────────────────┐  ┌─────────────────┐           │
│  │   S3/R2         │  │  Load Balancer  │           │
│  │ (File Storage)  │  │   + API Gateway │           │
│  └─────────────────┘  └─────────────────┘           │
└──────────────────────────────────────────────────────┘
```

---

## 14. Testing Strategy

### 14.1 Unit Tests

**Target:** Business logic in service layer

```bash
cd services/identity-service
./mvnw test
```

**Coverage:** Service implementations, domain logic

---

### 14.2 Integration Tests

**Target:** Database interactions, Kafka consumers

```bash
./mvnw verify -P integration-test
```

**Tools:**
- Spring Boot Test
- `@EmbeddedKafka` for Kafka tests
- H2 or Testcontainers for database tests

---

### 14.3 API Tests

**Target:** REST endpoints

```bash
./mvnw test -Dtest=*ControllerTest
```

**Tools:**
- Spring MockMvc
- Manual curl/Postman tests against running services

---

### 14.4 End-to-End Tests

**Target:** Full user workflows

**Recommended:**
- Selenium/Cypress for frontend UI tests
- Test against fully deployed Docker Compose environment

---

## 15. Known Limitations & Future Improvements

### 15.1 Current Limitations

1. **No API Gateway**
   - Frontend apps connect directly to services
   - CORS configuration duplicated across services

2. **Single Kafka Broker**
   - No high availability
   - Replication factor 1 (data loss risk)

3. **No Service Discovery**
   - Service URLs hardcoded in environment variables
   - Manual configuration required

4. **Limited Monitoring**
   - No centralized logging
   - No distributed tracing
   - No alerting system

5. **Authentication Inconsistency**
   - Identity service uses OAuth2
   - Ecommerce service uses custom JWT
   - No unified auth strategy

6. **Frontend in Docker Commented Out**
   - Frontends run separately (not containerized)
   - Manual deployment required

---

### 15.2 Recommended Improvements

**Short-term:**
1. Add API Gateway (Spring Cloud Gateway or Kong)
2. Implement distributed tracing (Zipkin)
3. Add centralized logging (ELK stack)
4. Standardize authentication across services
5. Add frontend Docker builds

**Medium-term:**
1. Migrate to Kubernetes
2. Implement circuit breakers (Resilience4j)
3. Add caching layer (Redis)
4. Implement rate limiting
5. Add comprehensive monitoring (Prometheus + Grafana)

**Long-term:**
1. Multi-region deployment
2. Event sourcing for audit trails
3. CQRS for read-heavy operations
4. GraphQL API for flexible frontend queries
5. Machine learning for demand forecasting

---

## 16. Technology Stack Summary

| Layer | Technology | Version |
|-------|------------|---------|
| **Backend Framework** | Spring Boot | 3.5.6 / 4.0.1-4.0.3 |
| **Programming Language** | Java | 21 / 25 |
| **Database** | PostgreSQL | 17 |
| **Message Broker** | Apache Kafka | 4.2.0 |
| **ORM** | JPA/Hibernate | (included in Spring Boot) |
| **Security** | Spring Security | (included in Spring Boot) |
| **Build Tool** | Maven | (wrapper included) |
| **Containerization** | Docker | - |
| **Orchestration** | Docker Compose | 3.9 |
| **Frontend Framework** | React | 19 |
| **Build Tool (Frontend)** | Vite | 8 |
| **State Management** | Redux Toolkit, Zustand | - |
| **HTTP Client** | Axios | - |
| **UI Libraries** | Material-UI, Ant Design | 5 |
| **Object Storage** | Cloudflare R2 | - |
| **Maps API** | Goong Maps | - |

---

## 17. Conclusion

This e-commerce platform demonstrates a modern microservices architecture with event-driven communication. The system successfully separates concerns across four independent services, enabling scalability and maintainability.

**Strengths:**
- Clear service boundaries
- Event-driven architecture for loose coupling
- Database isolation per service
- Multiple frontend applications for different user types
- Containerized for easy deployment

**Areas for Improvement:**
- Add API Gateway for centralized routing
- Standardize authentication across services
- Implement comprehensive monitoring and logging
- Increase Kafka resilience for production
- Add automated testing pipeline

**Overall Assessment:**
The architecture is well-suited for a capstone project and demonstrates understanding of modern distributed systems principles. With the recommended improvements, it could be production-ready.

---

## Appendix A: Glossary

- **Microservices**: Architectural style where application is composed of small, independent services
- **Event-Driven Architecture**: Pattern where services communicate through events (messages)
- **Clean Architecture**: Architectural pattern separating business logic from infrastructure
- **Kafka**: Distributed streaming platform for building event-driven applications
- **PostgreSQL**: Open-source relational database
- **OAuth2**: Authorization framework for token-based authentication
- **JWT**: JSON Web Token for stateless authentication
- **CORS**: Cross-Origin Resource Sharing for secure API access from browsers

---

## Appendix B: References

- Service Documentation: `services/*/CLAUDE.md`
- Database Schemas: `services/*/db_schema/README.md`
- Docker Setup: `DOCKER_SETUP.md`
- Troubleshooting: `TROUBLESHOOTING.md`
- Codebase Guide: `CLAUDE.md`

---

**Document Status:** Draft  
**Last Updated:** May 2, 2026  
**Maintained By:** Development Team
