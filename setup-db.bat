@echo off
set PGPASSWORD=postgres
echo Creating database smart_water_db...
psql -U postgres -h localhost -d postgres -c "CREATE DATABASE smart_water_db;"
echo Applying Schema V1__init_schema.sql...
psql -U postgres -h localhost -d smart_water_db -f "backend/src/main/resources/db/migration/V1__init_schema.sql"
echo Populating Seed Data V2__seed_data.sql...
psql -U postgres -h localhost -d smart_water_db -f "backend/src/main/resources/db/migration/V2__seed_data.sql"
echo Database setup finished successfully!
pause
