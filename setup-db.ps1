# PowerShell script to recreate smart_water_db database and apply schema + seed data
param (
    [string]$dbUser = "postgres",
    [string]$dbPassword = "root",
    [string]$dbHost = "localhost",
    [string]$dbPort = "5432"
)

$env:PGPASSWORD = $dbPassword

Write-Host "Creating database 'smart_water_db' on $dbHost:$dbPort..." -ForegroundColor Cyan
psql -U $dbUser -h $dbHost -p $dbPort -d postgres -c "CREATE DATABASE smart_water_db;" 2>$null

@("V1__init_schema.sql", "V2__seed_data.sql", "V3__week3_4_tables.sql", "V4__decouple_water_purchase_and_tariff.sql", "V5__billing_cycle_three_tier_and_cycle_scoping.sql", "V6__dynamic_procurement_tariff.sql", "V7__resident_invitation_security.sql", "V8__milestone3_uniqueness_and_indexes.sql") | ForEach-Object {
    Write-Host "Applying $_..." -ForegroundColor Cyan
    psql -U $dbUser -h $dbHost -p $dbPort -d smart_water_db -f "backend/src/main/resources/db/migration/$_"
}

Write-Host "Database recreate & seed completed successfully!" -ForegroundColor Green
