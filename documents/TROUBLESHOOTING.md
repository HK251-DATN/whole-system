# Troubleshooting Guide

## Maven Build Errors in Docker

### Problem: "Connection refused" to Maven repositories during Docker build

**Error message:**
```
Could not transfer artifact from/to central (https://repo.maven.apache.org/maven2): 
Connection refused
```

This happens because Docker build context cannot access external networks to download Maven dependencies.

### Solution 1: Build JARs Locally (Recommended)

Build the JAR files on your host machine first, then Docker just copies them:

**Linux/Mac:**
```bash
# 1. Build all JARs locally
./build-local.sh

# 2. Start Docker services
./start.sh up
```

**Windows:**
```bash
# 1. Build all JARs locally
build-local.bat

# 2. Start Docker services
start.bat up
```

This approach:
- ✅ Faster builds (uses local Maven cache)
- ✅ Works behind corporate proxies
- ✅ No network issues
- ✅ Better for development (can test locally first)

### Solution 2: Fix Docker Network Access

If you want to build inside Docker, configure network settings:

**Option A: Use host network for build**

Edit each service's Dockerfile to add network mode:

```dockerfile
# syntax=docker/dockerfile:1
FROM maven:4.0.0-rc-5-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies with retry
RUN --network=host mvn dependency:go-offline -B || true
COPY src ./src
RUN --network=host mvn -B -DskipTests package
```

**Option B: Configure Docker daemon to use host DNS**

Edit `/etc/docker/daemon.json`:
```json
{
  "dns": ["8.8.8.8", "8.8.4.4"],
  "dns-opts": ["ndots:0"]
}
```

Restart Docker:
```bash
sudo systemctl restart docker
```

**Option C: Use Maven mirror**

Create `.m2/settings.xml` in each service directory:
```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun-maven</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

Update Dockerfile to use it:
```dockerfile
FROM maven:4.0.0-rc-5-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY .m2/settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
```

### Solution 3: Build Each Service Individually

```bash
# Build JARs locally one by one
cd services/identity-service
./mvnw clean package -DskipTests
cd ../..

cd services/back-office-service
./mvnw clean package -DskipTests
cd ../..

cd services/product_storage_service
./mvnw clean package -DskipTests
cd ../..

cd services/ecommerce-service
./mvnw clean package -DskipTests
cd ../..

# Then start Docker
./start.sh up
```

---

## Port Conflicts

### Problem: Port already in use

**Error:**
```
Error starting userland proxy: listen tcp4 0.0.0.0:5432: bind: address already in use
```

**Check what's using the port:**

Linux/Mac:
```bash
sudo lsof -i :5432
sudo lsof -i :9092
sudo lsof -i :9000
```

Windows:
```bash
netstat -ano | findstr :5432
netstat -ano | findstr :9092
```

**Solutions:**

1. **Stop the conflicting service:**
```bash
# PostgreSQL
sudo systemctl stop postgresql

# Or kill specific process
kill -9 <PID>
```

2. **Change ports in docker-compose.yml:**
```yaml
services:
  postgres:
    ports:
      - "5433:5432"  # Use 5433 on host instead
```

---

## Database Connection Issues

### Problem: Services can't connect to PostgreSQL

**Check PostgreSQL is running:**
```bash
docker-compose ps postgres
```

**Check logs:**
```bash
docker-compose logs postgres
```

**Verify databases exist:**
```bash
docker-compose exec postgres psql -U khoidev -d postgres -c "\l"
```

Should show: `identity_db`, `back_office_db`, `product_storage_db`, `ecommerce_db`

**Connect manually to test:**
```bash
docker-compose exec postgres psql -U khoidev -d identity_db
```

**If databases not created:**
```bash
# Stop and remove volumes
docker-compose down -v

# Restart
docker-compose up -d
```

---

## Kafka Connection Issues

### Problem: Services can't connect to Kafka

**Check Kafka is running:**
```bash
docker-compose ps kafka
```

**Check Kafka logs:**
```bash
docker-compose logs kafka
docker-compose logs kafka-init
```

**Verify topics exist:**
```bash
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --list --bootstrap-server localhost:9092
```

**Use Kafka UI:**
Open http://localhost:9280 to visually inspect topics and messages.

**If topics not created:**
```bash
# Restart kafka-init
docker-compose up -d kafka-init
```

---

## Service Health Issues

### Problem: Service keeps restarting

**Check service logs:**
```bash
./start.sh logs identity-service
```

**Common causes:**

1. **Missing environment variables**
   - Check `.env` file exists
   - Verify all required variables are set

2. **Database not ready**
   - Wait for PostgreSQL health check to pass
   - Check `docker-compose ps` - postgres should be "healthy"

3. **Application errors**
   - Check application.yaml configuration
   - Verify R2 credentials are correct

**Debug inside container:**
```bash
docker-compose exec identity-service /bin/bash
# Check environment
env | grep DB_
env | grep KAFKA_
env | grep R2_
```

---

## R2 / File Upload Issues

### Problem: File upload fails

**Check R2 credentials:**
```bash
# Verify .env file
cat .env | grep R2
```

**Test credentials:**
Create a test script to verify R2 connection outside of the app.

**Common issues:**
- Wrong Account ID
- Expired or invalid Access Key
- Incorrect Secret Key
- Bucket doesn't exist
- Wrong region

---

## Build Issues

### Problem: ./mvnw: Permission denied

**Fix:**
```bash
chmod +x services/*/mvnw
```

### Problem: Java version mismatch

**Check Java version:**
```bash
java -version
```

Required:
- Java 25 for: identity-service, back-office-service, product_storage_service
- Java 21 for: ecommerce-service

**Install multiple Java versions:**

Linux (using SDKMAN):
```bash
curl -s "https://get.sdkman.io" | bash
sdk install java 25-tem
sdk install java 21-tem
sdk use java 25-tem  # Switch version
```

---

## Docker Issues

### Problem: Docker daemon not running

**Start Docker:**

Linux:
```bash
sudo systemctl start docker
```

Mac/Windows:
Start Docker Desktop application

### Problem: Out of disk space

**Check Docker disk usage:**
```bash
docker system df
```

**Clean up:**
```bash
# Remove unused containers, images, networks
docker system prune -a

# Remove unused volumes (WARNING: deletes data)
docker volume prune
```

### Problem: Build cache issues

**Clear build cache:**
```bash
docker builder prune -a
```

**Rebuild without cache:**
```bash
docker-compose build --no-cache
docker-compose up -d
```

---

## Environment Variable Issues

### Problem: Variables not loading

**Check .env file location:**
Must be in project root (same directory as docker-compose.yml)

**Check syntax:**
```bash
# Correct
DB_USERNAME=khoidev

# Wrong (no spaces around =)
DB_USERNAME = khoidev
```

**Verify variables are loaded:**
```bash
docker-compose config | grep DB_USERNAME
```

**Check inside container:**
```bash
docker-compose exec identity-service env | grep DB_
```

---

## Startup Order Issues

### Problem: Service starts before dependencies

Docker Compose handles this with `depends_on` and health checks, but sometimes needs help.

**Manual startup order:**
```bash
# Start infrastructure first
docker-compose up -d postgres kafka

# Wait for health checks
docker-compose ps

# Start services
docker-compose up -d identity-service back-office-service
docker-compose up -d product-storage-service ecommerce-service
```

---

## Network Issues

### Problem: Services can't communicate

**Check network exists:**
```bash
docker network ls | grep ecommerce
```

**Inspect network:**
```bash
docker network inspect project-root_ecommerce-network
```

**Test connectivity:**
```bash
# From identity-service to postgres
docker-compose exec identity-service ping postgres

# From identity-service to kafka
docker-compose exec identity-service ping kafka
```

---

## Logs Not Showing

### Problem: Can't see application logs

**Increase log verbosity in application.yaml:**
```yaml
logging:
  level:
    root: INFO
    org.springframework: DEBUG
```

**View logs with timestamp:**
```bash
docker-compose logs -f --timestamps identity-service
```

**Save logs to file:**
```bash
docker-compose logs identity-service > identity-service.log
```

---

## Complete Reset

When nothing works, reset everything:

```bash
# Stop all containers
docker-compose down

# Remove volumes (WARNING: deletes all data)
docker-compose down -v

# Remove all project containers
docker ps -a | grep ecommerce | awk '{print $1}' | xargs docker rm -f

# Remove images
docker images | grep identity-service | awk '{print $3}' | xargs docker rmi -f
docker images | grep back-office-service | awk '{print $3}' | xargs docker rmi -f
docker images | grep product-storage-service | awk '{print $3}' | xargs docker rmi -f
docker images | grep ecommerce-service | awk '{print $3}' | xargs docker rmi -f

# Rebuild everything
./build-local.sh
./start.sh up
```

---

## Getting Help

If issues persist:

1. Check service logs: `./start.sh logs [service-name]`
2. Verify `.env` file is correctly configured
3. Ensure Docker has internet access (for initial image pulls)
4. Check Docker daemon logs: `journalctl -u docker`
5. Review service-specific documentation in each service directory
