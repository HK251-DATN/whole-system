# E-Commerce Microservices Platform

[![Java](https://img.shields.io/badge/Java-21%20%7C%2025-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6%20%7C%204.0.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-4.2.0-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

A complete e-commerce platform built with microservices architecture, featuring user management, product catalog, inventory management, and order processing. Built with Spring Boot, Kafka, PostgreSQL, and Docker.

**HCMUT Capstone Project** - Scalable event-driven e-commerce backend with clean architecture patterns.

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- (Optional) Java 21 & 25 for local development
- (Optional) Maven for local builds

### First Time Setup

```bash
# 1. Clone this project root repository
git clone https://github.com/HK251-DATN/project-root.git
cd project-root

# 2. Run setup script to clone all service repositories
./setup-repos.sh    # Linux/Mac
setup-repos.bat     # Windows

# This will create and populate:
#   - services/ (4 microservice repos)
#   - infrastructure/ (2 infrastructure repos)
#   - frontend/ (2 frontend repos)
```

### Start Everything with Docker

```bash
# 1. Configure environment
cp .env.example .env
# Edit .env and add your Cloudflare R2 credentials

# 2. Build JARs locally (avoids Docker network issues)
./build-local.sh    # Linux/Mac
build-local.bat     # Windows

# 3. Start all services
./start.sh          # Linux/Mac
start.bat           # Windows
```

That's it! All services will be available at:
- **Identity Service**: http://localhost:9000
- **Back-Office Service**: http://localhost:9100
- **Product Storage Service**: http://localhost:9200
- **Ecommerce Service**: http://localhost:9301
- **Kafka UI**: http://localhost:9280

📖 **Full Docker guide:** See [DOCKER_SETUP.md](DOCKER_SETUP.md)

## 📋 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Microservices Platform                  │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Identity Service (9000)          Back-Office (9100)    │
│  ├─ Authentication                ├─ Product Catalog     │
│  ├─ Authorization                 ├─ Category Mgmt       │
│  └─ User Management               └─ Kafka Producer      │
│                                                           │
│  Product Storage (9200)           Ecommerce (9301)      │
│  ├─ Warehouse Mgmt                ├─ Order Processing    │
│  ├─ Inventory                     ├─ Sales Transactions  │
│  ├─ Batch Processing              └─ Clean Architecture  │
│  └─ Kafka Consumer/Producer                              │
│                                                           │
├─────────────────────────────────────────────────────────┤
│              Event-Driven (Apache Kafka)                 │
├─────────────────────────────────────────────────────────┤
│              PostgreSQL (4 Databases)                    │
└─────────────────────────────────────────────────────────┘
```

### Event Flow

```
Back-Office Service
  └─> category-events
  └─> subsubcategory-events ──┐
  └─> product-general-events ─┼──> Product Storage Service
                               │     └─> batch-detail-events ──> Ecommerce Service
  Order Item Events ───────────┘
```

## 🏗️ Project Structure

```
ecommerce-microservices-platform/
├── services/                     # Created by setup-repos.sh
│   ├── identity-service/         # Port 9000, Spring Boot 4.0.1, Java 25
│   ├── back-office-service/      # Port 9100, Spring Boot 4.0.2, Java 25
│   ├── product_storage_service/  # Port 9200, Spring Boot 4.0.3, Java 25
│   └── ecommerce-service/        # Port 9301, Spring Boot 3.5.6, Java 21
├── infrastructure/               # Created by setup-repos.sh
│   ├── database/                 # PostgreSQL multi-db setup
│   └── kafka/                    # Kafka + Kafka UI
├── frontend/                     # Created by setup-repos.sh
│   ├── ecommerce-ui/             # Customer-facing e-commerce app
│   └── back-office-ui/           # Admin/back-office interface
├── docker-compose.yml            # Main orchestration
├── setup-repos.sh/.bat           # First-time repository setup
├── service.sh/.bat               # Individual service management
├── start.sh / start.bat          # Startup scripts
└── .env.example                  # Environment template
```

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Backend** | Spring Boot 3.5.6 - 4.0.3 |
| **Language** | Java 21 & 25 |
| **Database** | PostgreSQL 17 |
| **Messaging** | Apache Kafka 4.2.0 |
| **ORM** | JPA/Hibernate |
| **Security** | Spring Security, OAuth2 |
| **Build Tool** | Maven (with wrapper) |
| **Containerization** | Docker & Docker Compose |
| **Storage** | Cloudflare R2 (S3-compatible) |

## 📦 Services

### 1. Identity Service (Port 9000)
- User authentication and authorization
- OAuth2 integration
- Session management
- User profile with avatar storage (Cloudflare R2)

**Tech**: Spring Boot 4.0.1, Java 25, Spring Security

### 2. Back-Office Service (Port 9100)
- Product catalog management
- 3-layer category hierarchy (Category → Subcategory → SubSubcategory)
- Product general information management
- Kafka event producer for catalog changes
- Image storage (Cloudflare R2)

**Tech**: Spring Boot 4.0.2, Java 25

### 3. Product Storage Service (Port 9200)
- Warehouse management
- Storage tools (Racks, Fridges)
- Inventory tracking
- Batch processing with unit conversion (kg/g, L/mL)
- Kafka consumer and producer

**Tech**: Spring Boot 4.0.3, Java 25

**Key Feature**: Converts bulk batches into sellable product details
- Example: 10kg batch + 500g packaging = 20 sellable products

### 4. Ecommerce Service (Port 9301)
- Order processing
- Sales transactions
- Pricing logic
- Clean Architecture implementation

**Tech**: Spring Boot 3.5.6, Java 21

## 🔧 Development

### Local Development (without Docker)

**Prerequisites:**
- Java 21 & 25 installed
- PostgreSQL running
- Kafka running

**Start infrastructure:**
```bash
# PostgreSQL
cd infrastructure/database
./setup.sh

# Kafka
cd infrastructure/kafka
docker-compose up -d
```

**Run a service:**
```bash
cd services/identity-service

# Configure environment
cp .env.example .env
# Edit .env with your credentials

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

### With Docker (Recommended)

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for complete guide.

**Common commands:**
```bash
# Start all
./start.sh up

# View logs
./start.sh logs identity-service

# Restart after code changes
./start.sh rebuild

# Stop all
./start.sh stop

# Clean everything
./start.sh clean
```

### Development Workflow - Individual Service Management

After editing code in a service, use the `service.sh` script to quickly rebuild and restart just that service:

```bash
# Rebuild and restart after code changes (recommended)
./service.sh rebuild identity           # Apply changes to identity service
./service.sh rebuild back-office        # Apply changes to back-office service
./service.sh rebuild product-storage    # Apply changes to product storage
./service.sh rebuild ecommerce          # Apply changes to ecommerce service

# Other useful commands
./service.sh logs identity              # View logs (follow mode)
./service.sh restart identity           # Restart without rebuilding
./service.sh stop identity              # Stop a service
./service.sh start identity             # Start a service
./service.sh status identity            # Show service status
./service.sh exec identity              # Open bash inside container
```

**Common development workflow:**
1. Edit code in `services/identity-service/`
2. Run `./service.sh rebuild identity` to apply changes
3. Run `./service.sh logs identity` to verify the service started correctly
4. Test your changes

**Available service aliases:**
- `identity` → identity-service
- `back-office` / `backoffice` → back-office-service
- `product-storage` / `product` → product-storage-service
- `ecommerce` → ecommerce-service
- `postgres` / `db` → PostgreSQL database
- `kafka` → Kafka broker
- `kafka-ui` → Kafka UI

## 📝 Configuration

### Environment Variables

Create `.env` file in project root:

```bash
# Database
DB_USERNAME=khoidev
DB_PASSWORD=khoicktv
DB_PORT=5432

# Kafka
KAFKA_HOST=kafka
KAFKA_PORT=9092

# Cloudflare R2 (Required)
R2_ACCOUNT_ID=your_account_id
R2_ACCESS_KEY=your_access_key
R2_SECRET_KEY=your_secret_key
```

**Get R2 Credentials:**
1. Cloudflare Dashboard → R2 → Manage R2 API Tokens
2. Create token with read/write permissions
3. Copy Account ID, Access Key, Secret Key

### Database Configuration

PostgreSQL creates 4 databases automatically:
- `identity_db` - User authentication data
- `back_office_db` - Product catalog data
- `product_storage_db` - Inventory data
- `ecommerce_db` - Order data

**Connection:**
- Host: localhost
- Port: 5432
- User: khoidev
- Password: khoicktv

### Kafka Topics

Auto-created on startup:
- `user-events`
- `order-events`
- `order-item-events`
- `payment-events`
- `shipping-notifications`
- `category-events`
- `subsubcategory-events`
- `product-general-events`
- `batch-detail-events`

**Monitor:** http://localhost:9280 (Kafka UI)

## 🧪 Testing

```bash
# Test specific service
cd services/identity-service
./mvnw test

# Test in Docker
docker-compose exec identity-service ./mvnw test

# Test with coverage
./mvnw clean verify
```

## 📚 Documentation

- **[CLAUDE.md](CLAUDE.md)** - Detailed architecture and development guide for Claude Code
- **[DOCKER_SETUP.md](DOCKER_SETUP.md)** - Complete Docker setup and troubleshooting
- **Service-specific docs:**
  - [Product Storage Service](services/product_storage_service/CLAUDE.md)
  - [Database Schema](infrastructure/database/README.md)

## 🐛 Troubleshooting

### Services won't start

```bash
# Check Docker is running
docker info

# View logs
./start.sh logs

# Check specific service
./start.sh logs identity-service
```

### Port conflicts

Default ports: 5432 (Postgres), 9092 (Kafka), 9000-9301 (Services), 9280 (Kafka UI)

```bash
# Check what's using a port
sudo lsof -i :5432  # Linux/Mac
netstat -ano | findstr :5432  # Windows
```

### Database connection failed

```bash
# Verify databases exist
docker-compose exec postgres psql -U khoidev -d postgres -c "\l"

# Connect manually
docker-compose exec postgres psql -U khoidev -d identity_db
```

### Kafka not receiving events

1. Check Kafka UI: http://localhost:9280
2. Verify topics exist
3. Check producer/consumer logs
4. Verify network connectivity between services

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for more troubleshooting.

## 🚦 Service Startup Order

For proper operation, services should start in this order:

1. **PostgreSQL** (creates databases)
2. **Kafka** (message broker)
3. **Kafka Init** (creates topics)
4. **Services** (can start in any order, but recommended):
   - Identity Service
   - Back-Office Service
   - Product Storage Service
   - Ecommerce Service

Docker Compose handles this automatically via health checks and dependencies.

## 🔒 Security Notes

**For Development:**
- Default credentials are included
- .env file contains secrets

**For Production:**
- Change all default passwords
- Use secrets management (Docker secrets, Kubernetes secrets)
- Enable SSL/TLS for all connections
- Use managed services (RDS, MSK, etc.)
- Implement API authentication
- Set up proper firewall rules

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Run tests: `./mvnw test`
4. Test with Docker: `./start.sh rebuild`
5. Submit pull request

## 🌟 Features

- ✅ **Microservices Architecture** - 4 independent services with clear separation of concerns
- ✅ **Event-Driven Communication** - Apache Kafka for asynchronous messaging
- ✅ **Clean Architecture** - Domain-driven design in ecommerce service
- ✅ **Multi-Database Setup** - Isolated PostgreSQL databases per service
- ✅ **Docker Ready** - One-command deployment with Docker Compose
- ✅ **Cloudflare R2 Integration** - S3-compatible object storage for files
- ✅ **Spring Security** - OAuth2 authentication and authorization
- ✅ **Batch Processing** - Smart inventory management with unit conversion
- ✅ **API Documentation** - RESTful APIs for all services

## 🎯 Use Cases

- User authentication and profile management
- Product catalog with 3-tier category hierarchy
- Warehouse and inventory tracking
- Order processing and fulfillment
- Real-time event synchronization across services

## 📈 Scalability

- **Horizontal scaling**: Each service can scale independently
- **Event-driven**: Kafka enables loose coupling and async processing
- **Database isolation**: No single point of failure
- **Containerized**: Easy deployment to Kubernetes or cloud platforms

## 📄 License

This project is part of HCMUT Capstone Project. All rights reserved.

## 👥 Team

**Ho Chi Minh City University of Technology (HCMUT)**  
Capstone Project Team - 2026

## 📞 Support

For issues and questions:
- 📖 Check [DOCKER_SETUP.md](DOCKER_SETUP.md) for setup help
- 🐛 Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues
- 📋 Review service logs: `./start.sh logs [service-name]`
- 🎛️ Monitor Kafka: http://localhost:9280
- 💬 Open an issue on [GitHub](https://github.com/yourusername/ecommerce-microservices-platform/issues)

---

⭐ **Star this repository if you find it helpful!**
