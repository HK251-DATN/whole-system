# 🚀 QUICK START

## For Windows Users

### Method 1: Double-Click (Easiest)
1. Double-click `setup.bat` in File Explorer
2. Wait for completion
3. Done! ✅

### Method 2: Command Prompt
```cmd
setup.bat
```

### Method 3: Git Bash / WSL
```bash
chmod +x setup.sh
./setup.sh
```

---

## For Ubuntu/Linux/Mac Users

```bash
chmod +x setup.sh
./setup.sh
```

---

## Manual Setup (All Platforms)

```bash
docker-compose up -d --build
```

Wait 10 seconds, then verify:

```bash
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"
```

---

## ✅ What You Get

**4 Empty PostgreSQL Databases:**
- `identity_db`
- `back_office_db`
- `product_storage_db`
- `ecommerce_db`

**No tables, no schemas** - completely empty and ready for your team to populate.

---

## 🔌 Connect to Databases

### Connection String Format
```
postgresql://khoidev:khoicktv@localhost:5432/[database_name]
```

### Examples
```
postgresql://khoidev:khoicktv@localhost:5432/identity_db
postgresql://khoidev:khoicktv@localhost:5432/back_office_db
postgresql://khoidev:khoicktv@localhost:5432/product_storage_db
postgresql://khoidev:khoicktv@localhost:5432/ecommerce_db
```

### Using psql (Command Line)
```bash
docker exec -it multi_db_postgres psql -U khoidev -d identity_db
```

### Using GUI Tools (pgAdmin, DBeaver, etc.)
- **Host**: `localhost`
- **Port**: `5432`
- **Username**: `khoidev`
- **Password**: `khoicktv`
- **Database**: Choose one (identity_db, back_office_db, etc.)

---

## 📝 Verify Setup

```bash
# Run verification script
chmod +x verify.sh
./verify.sh
```

Or manually:

```bash
# List all databases
docker exec -it multi_db_postgres psql -U khoidev -d postgres -c "\l"

# Check a database is empty
docker exec -it multi_db_postgres psql -U khoidev -d identity_db -c "\dt"
# Should show: "Did not find any relations."
```

---

## 🛠️ Common Commands

```bash
# View logs
docker-compose logs -f

# Stop (keeps data)
docker-compose down

# Stop and delete all data
docker-compose down -v

# Restart
docker-compose restart

# Rebuild from scratch
docker-compose down -v && docker-compose up -d --build
```

---

## 🐛 Troubleshooting

### Windows: Line Ending Errors
- Already handled automatically by the Dockerfile ✅
- If issues persist, open `init-databases.sh` in VS Code
- Click "CRLF" at bottom-right → Select "LF" → Save

### Linux: Permission Denied
```bash
chmod +x *.sh
```

### Port 5432 Already in Use
Edit `docker-compose.yml`, change:
```yaml
ports:
  - "5433:5432"  # Use port 5433 instead
```

### Container Won't Start
```bash
# Check logs
docker-compose logs

# Completely reset
docker-compose down -v
docker-compose up -d --build
```

---

## 📚 Next Steps

Your databases are empty and ready! Your team can now:

1. **Run SQL scripts**
   ```bash
   docker exec -i multi_db_postgres psql -U khoidev -d identity_db < your_schema.sql
   ```

2. **Use migration tools**
   - Flyway
   - Liquibase
   - Alembic (Python)
   - Knex.js (Node.js)
   - Entity Framework (C#)

3. **Connect from your application** and create tables programmatically

---

## 📖 Full Documentation

See `README.md` for complete documentation including:
- Configuration options
- Security notes
- Code examples (Node.js, Python, Java, C#)
- Advanced troubleshooting
- Data backup/restore

---

## ⚡ One-Liner Reset

```bash
docker-compose down -v && docker-compose up -d --build
```

Completely wipes everything and starts fresh.
