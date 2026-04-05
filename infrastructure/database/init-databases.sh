#!/bin/bash
set -e

# List of databases to create (empty, no tables)
declare -a DATABASES=(
    "identity_db"
    "back_office_db"
    "product_storage_db"
    "ecommerce_db"
    "khoidev"
)

echo "========================================"
echo "Creating empty PostgreSQL databases..."
echo "========================================"

# Create each database
for db_name in "${DATABASES[@]}"; do
    echo ""
    echo "Creating database: $db_name"
    
    # Create database if it doesn't exist
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $db_name'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db_name')\gexec
EOSQL
    
    echo "✓ Database '$db_name' created (empty - no tables)"
done

echo ""
echo "========================================"
echo "Database creation completed!"
echo "========================================"
echo ""
echo "Created databases:"
for db_name in "${DATABASES[@]}"; do
    echo "  - $db_name"
done
echo ""
echo "All databases are empty and ready for schema creation by your team."
echo ""
