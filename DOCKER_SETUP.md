# Docker Setup Guide

Complete guide for running the entire e-commerce platform using Docker Compose.

## Quick Start

### 1. Prerequisites

- **Docker Desktop** (Windows/Mac) or **Docker Engine** (Linux)
- **Docker Compose** (usually included with Docker Desktop)
- **Git** (to clone the repository)

Verify installation:
```bash
docker --version
docker-compose --version  # or: docker compose version
```

### 2. Configure Environment Variables

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` and add your **Cloudflare R2 credentials**:
```bash
R2_ACCOUNT_ID=your_actual_account_id
R2_ACCESS_KEY=your_actual_access_key
R2_SECRET_KEY=your_actual_secret_key
```

**How to get R2 credentials:**
1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Navigate to R2 Object Storage
3. Go to "Manage R2 API Tokens"
4. Create a new API token with read/write permissions
5. Copy the Account ID, Access Key, and Secret Key

### 3. Build JARs Locally

Build the Java applications on your host machine (avoids Docker network issues):

**Linux/Mac:**
```bash
./build-local.sh
```

**Windows:**
```bash
build-local.bat
```

This builds all 4 services and creates JAR files in each service's `target/` directory.

### 4. Start All Services

**Linux/Mac:**
```bash
./start.sh
```

**Windows:**
```bash
start.bat
```

**Or use Docker Compose directly:**
```bash
docker-compose up -d --build
```

### 5. Verify Services Are Running

Check service status:
```bash
./start.sh status
# OR
docker-compose ps
```

All services should show "Up" or "running" status.

## Service URLs

Once all services are running:

| Service | URL | Description |
|---------|-----|-------------|
| Identity Service | http://localhost:9000 | User authentication & authorization |
| Back-Office Service | http://localhost:9100 | Product & category management |
| Product Storage Service | http://localhost:9200 | Warehouse & inventory |
| Ecommerce Service | http://localhost:9301 | Orders & transactions |
| Kafka UI | http://localhost:9280 | Kafka topic monitoring |
| PostgreSQL | localhost:5432 | Database (user: khoidev, pass: khoicktv) |

## Common Commands

### Using the start script (Linux/Mac)

```bash
# Start all services
./start.sh up

# Stop all services
./start.sh stop

# Restart all services
./start.sh restart

# Rebuild and restart (after code changes)
./start.sh rebuild

# View logs for all services
./start.sh logs

# View logs for specific service
./start.sh logs identity-service
./start.sh logs kafka
./start.sh logs postgres

# Check service status
./start.sh status

# Clean up everything (removes volumes!)
./start.sh clean

# Show help
./start.sh help
```

### Using the start script (Windows)

```bash
start.bat up
start.bat stop
start.bat logs
start.bat logs identity-service
start.bat status
start.bat clean
```

### Using Docker Compose directly

```bash
# Start all services
docker-compose up -d

# Start with rebuild
docker-compose up -d --build

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes data!)
docker-compose down -v

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f identity-service

# Restart specific service
docker-compose restart identity-service

# Rebuild specific service
docker-compose up -d --build identity-service

# Check status
docker-compose ps

# Execute command in container
docker-compose exec postgres psql -U khoidev -d identity_db
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose Network                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────┐                     │
│  │  PostgreSQL  │      │    Kafka     │                     │
│  │   (Port      │      │   (Port      │                     │
│  │    5432)     │      │    9092)     │                     │
│  └──────┬───────┘      └──────┬───────┘                     │
│         │                     │                              │
│         │  ┌──────────────────┴────────────────┐            │
│         │  │                                    │            │
│  ┌──────▼──▼──────┐  ┌────────▼─────────┐  ┌──▼──────────┐ │
│  │   Identity      │  │   Back-Office    │  │   Product    │ │
│  │   Service       │  │    Service       │  │   Storage    │ │
│  │  (Port 9000)    │  │  (Port 9100)     │  │  (Port 9200) │ │
│  └─────────────────┘  └──────────────────┘  └──────┬───────┘ │
│                                                     │         │
│                       ┌─────────────────────────────┘         │
│                       │                                       │
│                ┌──────▼──────────┐                           │
│                │   Ecommerce     │                           │
│                │    Service      │                           │
│                │  (Port 9301)    │                           │
│                └─────────────────┘                           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Service Dependencies

Services start in the following order to respect dependencies:

1. **PostgreSQL** - Creates 4 databases: `identity_db`, `back_office_db`, `product_storage_db`, `ecommerce_db`
2. **Kafka** - Message broker for event-driven communication
3. **Kafka Init** - Creates required topics
4. **Microservices** - Start after infrastructure is healthy
   - Identity Service (no Kafka dependencies)
   - Back-Office Service (produces events)
   - Product Storage Service (consumes back-office events, produces batch events)
   - Ecommerce Service (consumes batch events)

## Environment Variables

### Database Configuration
- `DB_USERNAME` - PostgreSQL username (default: khoidev)
- `DB_PASSWORD` - PostgreSQL password (default: khoicktv)
- `DB_PORT` - PostgreSQL port (default: 5432)

### Kafka Configuration
- `KAFKA_HOST` - Kafka broker host (default: kafka)
- `KAFKA_PORT` - Kafka broker port (default: 9092)

### Cloudflare R2 Configuration (Required)
- `R2_ACCOUNT_ID` - Your Cloudflare R2 account ID
- `R2_ACCESS_KEY` - R2 API access key
- `R2_SECRET_KEY` - R2 API secret key
- `USER_AVATAR_PUBLIC_BUCKET_URL` - Public URL for avatar bucket (optional)

## Troubleshooting

### Services won't start

**Check Docker is running:**
```bash
docker info
```

**Check for port conflicts:**
```bash
# Linux/Mac
sudo lsof -i :5432
sudo lsof -i :9092
sudo lsof -i :9000
sudo lsof -i :9100
sudo lsof -i :9200
sudo lsof -i :9301

# Windows
netstat -ano | findstr :5432
netstat -ano | findstr :9092
```

**Solution:** Stop conflicting services or change ports in `docker-compose.yml`

### Database connection errors

**View PostgreSQL logs:**
```bash
docker-compose logs postgres
```

**Check database was created:**
```bash
docker-compose exec postgres psql -U khoidev -d postgres -c "\l"
```

You should see: `identity_db`, `back_office_db`, `product_storage_db`, `ecommerce_db`

**Connect to database manually:**
```bash
docker-compose exec postgres psql -U khoidev -d identity_db
```

### Kafka connection errors

**View Kafka logs:**
```bash
docker-compose logs kafka
docker-compose logs kafka-init
```

**Check topics were created:**
```bash
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

Expected topics:
- user-events
- order-events
- order-item-events
- payment-events
- shipping-notifications
- category-events
- subsubcategory-events
- product-general-events
- batch-detail-events

**Or use Kafka UI:** http://localhost:9280

### Service build errors

**View build logs:**
```bash
docker-compose up --build identity-service
```

**Clear Docker cache and rebuild:**
```bash
docker-compose down
docker system prune -a
docker-compose up -d --build
```

### R2 credentials errors

**Verify .env file exists:**
```bash
cat .env
```

**Check environment variables are loaded:**
```bash
docker-compose config | grep R2
```

### Out of disk space

**Check Docker disk usage:**
```bash
docker system df
```

**Clean up unused images/containers:**
```bash
docker system prune -a
docker volume prune
```

### View service logs in real-time

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f identity-service

# Last 100 lines
docker-compose logs --tail=100 identity-service

# Since specific time
docker-compose logs --since 30m identity-service
```

## Data Persistence

**Persistent data is stored in Docker named volumes:**
- `postgres_data` - All database data (critical - do not delete!)
- `kafka_data` - Kafka logs and topics (managed by Docker)

Both volumes are managed by Docker and stored in Docker's volume directory (not in your project folder).

**View volumes:**
```bash
# List volumes
docker volume ls | grep ecommerce

# Inspect volume location
docker volume inspect ecommerce-microservices-platform_postgres_data
docker volume inspect ecommerce-microservices-platform_kafka_data
```

**To completely reset (WARNING: deletes all data):**
```bash
./start.sh clean
# OR
docker-compose down -v
```

**Backup database:**
```bash
# Using pg_dumpall
docker-compose exec postgres pg_dumpall -U khoidev > backup.sql

# Or backup the entire volume
docker run --rm -v ecommerce-microservices-platform_postgres_data:/data \
  -v $(pwd):/backup ubuntu tar czf /backup/postgres_backup.tar.gz /data
```

**Restore database:**
```bash
# From SQL dump
docker-compose exec -T postgres psql -U khoidev < backup.sql

# Or restore volume
docker run --rm -v ecommerce-microservices-platform_postgres_data:/data \
  -v $(pwd):/backup ubuntu tar xzf /backup/postgres_backup.tar.gz -C /data --strip-components=1
```

## Development Workflow

### 1. Making code changes

After changing code in any service:
```bash
# Rebuild and restart specific service
docker-compose up -d --build identity-service

# Or rebuild all services
./start.sh rebuild
```

### 2. Testing changes

```bash
# View logs to check for errors
docker-compose logs -f identity-service

# Execute tests inside container
docker-compose exec identity-service /bin/bash
# Inside container:
./mvnw test
```

### 3. Debugging

```bash
# Shell into a running container
docker-compose exec identity-service /bin/bash

# Check environment variables
docker-compose exec identity-service env

# Check network connectivity
docker-compose exec identity-service ping postgres
docker-compose exec identity-service ping kafka
```

## Production Considerations

**This setup is for DEVELOPMENT only.** For production:

1. **Use secrets management** (Docker secrets, Kubernetes secrets, AWS Secrets Manager)
2. **Change default passwords** in `.env`
3. **Enable SSL/TLS** for PostgreSQL and Kafka
4. **Use external databases** (AWS RDS, Cloud SQL) instead of containerized PostgreSQL
5. **Use managed Kafka** (AWS MSK, Confluent Cloud)
6. **Add health checks** and monitoring (Prometheus, Grafana)
7. **Configure resource limits** (CPU, memory)
8. **Use production-grade image registry** (Docker Hub, AWS ECR, GCR)
9. **Enable authentication** on all services
10. **Set up proper logging** and log aggregation

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
