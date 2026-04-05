# PostgreSQL Multi-Database Setup

Creates a PostgreSQL 17 instance with 4 empty databases. Works on **both Windows and Ubuntu/Linux** Docker environments.

## 🗄️ Created Databases

This setup creates 4 **empty** databases (no tables, no schemas):

1. **identity_db** - Empty database
2. **back_office_db** - Empty database  
3. **product_storage_db** - Empty database
4. **ecommerce_db** - Empty database

Tables, relationships, and schemas will be handled by your team separately.

## 🚀 Quick Start

### Windows Users

**Option 1: Double-click**
```
Double-click setup.bat in File Explorer
```

**Option 2: Command Prompt**
```cmd
setup.bat
```

**Option 3: Git Bash / WSL**
```bash
chmod +x setup.sh
./setup.sh
```

### Ubuntu/Linux Users

```bash
chmod +x setup.sh
./setup.sh
```

### Manual Setup (All Platforms)

```bash
# Build and start
docker-compose up -d --build

# Verify databases were created
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"
```

## 📋 Prerequisites

- Docker installed
- Docker Compose installed

## 🔌 Connection Information

### Default Credentials
- **Host**: `localhost`
- **Port**: `5432`
- **User**: `khoidev`
- **Password**: `khoicktv`

### Connection Strings

```bash
# identity_db
postgresql://khoidev:khoicktv@localhost:5432/identity_db

# back_office_db
postgresql://khoidev:khoicktv@localhost:5432/back_office_db

# product_storage_db
postgresql://khoidev:khoicktv@localhost:5432/product_storage_db

# ecommerce_db
postgresql://khoidev:khoicktv@localhost:5432/ecommerce_db
```

## 🛠️ Common Commands

### Start/Stop Containers

```bash
# Start
docker-compose up -d

# Stop (keeps data)
docker-compose down

# Stop and remove all data
docker-compose down -v
```

### Access Database

```bash
# Connect to a specific database
docker exec -it multi_db_postgres psql -U khoidev -d identity_db

# List all databases
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"

# List tables in a database (will be empty initially)
docker exec -it multi_db_postgres psql -U khoidev -d identity_db -c "\dt"
```

### View Logs

```bash
docker-compose logs -f
```

### Rebuild

```bash
# After making changes to scripts
docker-compose down
docker-compose up -d --build
```

## 📁 Project Structure

```
.
├── Dockerfile              # PostgreSQL image configuration
├── docker-compose.yml      # Docker Compose setup
├── init-databases.sh       # Database creation script
├── setup.sh               # Linux/Mac setup script
├── setup.bat              # Windows setup script
└── README.md              # This file
```

## 🔧 Configuration

### Change Database Names

Edit `init-databases.sh`:

```bash
declare -a DATABASES=(
    "identity_db"
    "back_office_db"
    "product_storage_db"
    "ecommerce_db"
    "your_new_database"  # Add more here
)
```

Then rebuild:
```bash
docker-compose down -v
docker-compose up -d --build
```

### Change Credentials

Edit `docker-compose.yml`:

```yaml
environment:
  POSTGRES_USER: your_username      # Change this
  POSTGRES_PASSWORD: your_password  # Change this
  POSTGRES_DB: postgres
```

### Change Port

If port 5432 is already in use:

Edit `docker-compose.yml`:
```yaml
ports:
  - "5433:5432"  # Use 5433 instead
```

## 💾 Data Persistence

Data is stored in a Docker named volume called `postgres_data`. This means:

- ✅ Data survives container restarts
- ✅ Data persists when you run `docker-compose down`
- ❌ Data is deleted when you run `docker-compose down -v`

### Where is the data?

```bash
# Inspect the volume
docker volume inspect postgres-setup_postgres_data

# Backup the volume
docker run --rm -v postgres-setup_postgres_data:/data -v $(pwd):/backup ubuntu tar czf /backup/postgres_backup.tar.gz /data
```

## 🐛 Troubleshooting

### Windows: Line Ending Errors

The Dockerfile automatically handles Windows CRLF line endings using `dos2unix`.

If you still have issues:
1. Open `init-databases.sh` in VS Code
2. Click "CRLF" in the bottom-right corner
3. Select "LF"
4. Save and rebuild

### Linux: Permission Denied

```bash
chmod +x init-databases.sh setup.sh
```

### Port Already in Use

```bash
# Check what's using port 5432
sudo lsof -i :5432  # Linux/Mac
netstat -ano | findstr :5432  # Windows

# Option 1: Stop existing PostgreSQL
sudo systemctl stop postgresql  # Linux
# or
net stop postgresql-x64-14  # Windows (adjust version)

# Option 2: Change port in docker-compose.yml to 5433:5432
```

### Database Not Created

1. Check logs:
   ```bash
   docker-compose logs postgres
   ```

2. Verify script exists in container:
   ```bash
   docker exec -it multi_db_postgres ls -la /docker-entrypoint-initdb.d/
   ```

3. Rebuild completely:
   ```bash
   docker-compose down -v
   docker-compose up -d --build
   ```

### Container Won't Start

```bash
# Check if Docker is running
docker ps

# Check container logs
docker-compose logs

# Ensure no other containers are using the same name
docker rm multi_db_postgres
```

## 🔄 Reset Everything

To completely reset and start fresh:

```bash
# Remove containers and volumes
docker-compose down -v

# Rebuild and start
docker-compose up -d --build

# Verify databases
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"
```

## 📚 Usage Examples

### Node.js

```javascript
const { Pool } = require('pg');

const pool = new Pool({
  connectionString: 'postgresql://khoidev:khoicktv@localhost:5432/identity_db'
});

// or

const pool = new Pool({
  user: 'khoidev',
  host: 'localhost',
  database: 'identity_db',
  password: 'khoicktv',
  port: 5432,
});
```

### Python

```python
import psycopg2

# Connection string
conn = psycopg2.connect(
    "postgresql://khoidev:khoicktv@localhost:5432/ecommerce_db"
)

# Or with parameters
conn = psycopg2.connect(
    dbname="ecommerce_db",
    user="khoidev",
    password="khoicktv",
    host="localhost",
    port="5432"
)
```

### Java (JDBC)

```java
String url = "jdbc:postgresql://localhost:5432/back_office_db";
String user = "khoidev";
String password = "khoicktv";

Connection conn = DriverManager.getConnection(url, user, password);
```

### .NET (Npgsql)

```csharp
var connString = "Host=localhost;Port=5432;Database=product_storage_db;Username=khoidev;Password=khoicktv";

await using var conn = new NpgsqlConnection(connString);
await conn.OpenAsync();
```

### Using pgAdmin or DBeaver

1. Create a new connection
2. Enter connection details:
   - Host: `localhost`
   - Port: `5432`
   - Database: Choose one (identity_db, back_office_db, etc.)
   - Username: `khoidev`
   - Password: `khoicktv`

## ✅ Verify Setup

```bash
# List all databases
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"

# Expected output should show:
# - identity_db
# - back_office_db
# - product_storage_db
# - ecommerce_db

# Connect to a database and verify it's empty
docker exec -it multi_db_postgres psql -U khoidev -d identity_db -c "\dt"
# Should show: "Did not find any relations."
```

## 🎯 Next Steps

After setup, your team can:

1. **Create schemas/migrations** using tools like:
   - Flyway
   - Liquibase
   - Alembic (Python)
   - Knex.js (Node.js)
   - Entity Framework (C#)

2. **Run SQL scripts directly**:
   ```bash
   docker exec -i multi_db_postgres psql -U khoidev -d identity_db < schema.sql
   ```

3. **Use ORM migrations**:
   - TypeORM
   - Sequelize
   - Hibernate
   - SQLAlchemy

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. View logs: `docker-compose logs -f`
3. Verify Docker is running: `docker ps`
4. Ensure no port conflicts: `netstat -ano | findstr :5432`

## 📝 Notes

- Initialization script only runs on **first startup** or when volumes are empty
- All databases are created **empty** with no tables
- Data persists in the `postgres_data` volume
- Works identically on Windows and Ubuntu/Linux Docker

## 🔒 Security Notes

**For Production:**

1. Change default credentials
2. Use environment variables or secrets
3. Enable SSL/TLS
4. Configure firewall rules
5. Use strong passwords
6. Limit network exposure

This setup is intended for **development** use.
