import os
import time
from selenium import webdriver
from selenium.webdriver.edge.options import Options

def generate_er_diagram_image():
    html_content = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>AquaFlow Database Entity-Relationship Diagram</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
  body {
    background-color: #0b0f19;
    color: #f1f5f9;
    padding: 30px;
    width: 2500px;
    height: 1650px;
    position: relative;
    overflow: hidden;
  }
  
  /* Header banner */
  .header-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid #1e293b;
    padding-bottom: 16px;
    margin-bottom: 24px;
  }
  .title-group h1 {
    font-size: 28px;
    font-weight: 800;
    color: #ffffff;
    letter-spacing: -0.5px;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .badge-tag {
    background: #0284c7;
    color: #ffffff;
    font-size: 13px;
    font-weight: 700;
    padding: 4px 10px;
    border-radius: 6px;
    text-transform: uppercase;
  }
  .subtitle {
    color: #94a3b8;
    font-size: 14px;
    margin-top: 4px;
  }
  .legend-bar {
    display: flex;
    gap: 18px;
    align-items: center;
    background: #131b2e;
    padding: 8px 16px;
    border-radius: 8px;
    border: 1px solid #1e293b;
  }
  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    font-weight: 600;
  }
  .dot { width: 10px; height: 10px; border-radius: 50%; }

  /* Canvas layout */
  .diagram-canvas {
    position: relative;
    width: 2440px;
    height: 1520px;
  }

  /* Table Cards */
  .entity-card {
    position: absolute;
    background: #111827;
    border-radius: 8px;
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5);
    overflow: hidden;
    border: 1px solid #1f2937;
    transition: all 0.2s ease;
  }
  
  .card-header {
    padding: 8px 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 700;
    font-size: 13px;
    color: #ffffff;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .card-body {
    padding: 6px 0;
    font-size: 11px;
  }
  
  .field-row {
    display: flex;
    justify-content: space-between;
    padding: 3px 12px;
    color: #cbd5e1;
    border-bottom: 1px solid #172033;
  }
  .field-row:last-child { border-bottom: none; }
  .field-row.pk { background: rgba(14, 165, 233, 0.08); font-weight: 700; color: #38bdf8; }
  .field-row.fk { background: rgba(245, 158, 11, 0.08); font-weight: 600; color: #fbbf24; }
  
  .field-name { display: flex; align-items: center; gap: 6px; }
  .key-badge {
    font-size: 9px;
    padding: 1px 4px;
    border-radius: 3px;
    font-weight: 800;
  }
  .kb-pk { background: #0284c7; color: white; }
  .kb-fk { background: #d97706; color: white; }
  
  .field-type {
    color: #64748b;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 10px;
  }

  /* Domain Colorings */
  .domain-identity .card-header { background: linear-gradient(135deg, #1e40af, #3b82f6); border-top: 3px solid #60a5fa; }
  .domain-property .card-header { background: linear-gradient(135deg, #065f46, #10b981); border-top: 3px solid #34d399; }
  .domain-tenancy .card-header { background: linear-gradient(135deg, #92400e, #f59e0b); border-top: 3px solid #fbbf24; }
  .domain-billing .card-header { background: linear-gradient(135deg, #5b21b6, #8b5cf6); border-top: 3px solid #a78bfa; }
  .domain-ai .card-header { background: linear-gradient(135deg, #9f1239, #f43f5e); border-top: 3px solid #fb7185; }

  /* SVG Connector Layer */
  svg.connectors {
    position: absolute;
    top: 0;
    left: 0;
    width: 2440px;
    height: 1520px;
    pointer-events: none;
    z-index: 10;
  }
  
  .link-line {
    fill: none;
    stroke-width: 2px;
    opacity: 0.75;
  }
  .link-identity { stroke: #60a5fa; }
  .link-property { stroke: #34d399; }
  .link-tenancy { stroke: #fbbf24; }
  .link-billing { stroke: #a78bfa; }
  .link-ai { stroke: #fb7185; }

  .crow-foot {
    fill: none;
    stroke-width: 2px;
  }
</style>
</head>
<body>

<div class="header-bar">
  <div class="title-group">
    <h1>
      <span>AQUAFLOW · DATABASE ARCHITECTURE &amp; ENTITY-RELATIONSHIP MODEL</span>
      <span class="badge-tag">MySQL 8.0</span>
    </h1>
    <div class="subtitle">Complete physical schema specification with 16 normalized tables, primary/foreign keys, and referential cascades</div>
  </div>
  <div class="legend-bar">
    <div class="legend-item"><div class="dot" style="background:#3b82f6;"></div> Identity &amp; Auth</div>
    <div class="legend-item"><div class="dot" style="background:#10b981;"></div> Properties &amp; Meters</div>
    <div class="legend-item"><div class="dot" style="background:#f59e0b;"></div> Tenancy &amp; Occupancy</div>
    <div class="legend-item"><div class="dot" style="background:#8b5cf6;"></div> Procurement &amp; Billing</div>
    <div class="legend-item"><div class="dot" style="background:#f43f5e;"></div> AI Leak Detection</div>
  </div>
</div>

<div class="diagram-canvas">

  <!-- SVG Connectors -->
  <svg class="connectors">
    <defs>
      <marker id="dot-blue" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6">
        <circle cx="5" cy="5" r="3" fill="#60a5fa"/>
      </marker>
      <marker id="dot-green" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6">
        <circle cx="5" cy="5" r="3" fill="#34d399"/>
      </marker>
      <marker id="dot-amber" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6">
        <circle cx="5" cy="5" r="3" fill="#fbbf24"/>
      </marker>
      <marker id="dot-purple" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6">
        <circle cx="5" cy="5" r="3" fill="#a78bfa"/>
      </marker>
      <marker id="dot-rose" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6">
        <circle cx="5" cy="5" r="3" fill="#fb7185"/>
      </marker>
    </defs>

    <!-- Users -> Building Owners -->
    <path class="link-line link-identity" marker-start="url(#dot-blue)" d="M 280 180 L 330 180" />
    
    <!-- Users -> Residents -->
    <path class="link-line link-identity" marker-start="url(#dot-blue)" d="M 160 300 L 160 360" />

    <!-- Building Owners -> Buildings -->
    <path class="link-line link-property" marker-start="url(#dot-green)" d="M 590 180 L 650 180" />

    <!-- Buildings -> Apartments -->
    <path class="link-line link-property" marker-start="url(#dot-green)" d="M 910 180 L 980 180" />

    <!-- Apartments -> Water Meters -->
    <path class="link-line link-property" marker-start="url(#dot-green)" d="M 1240 180 L 1310 180" />

    <!-- Water Meters -> Water Usage -->
    <path class="link-line link-property" marker-start="url(#dot-green)" d="M 1570 180 L 1640 180" />

    <!-- Buildings -> Billing Cycles -->
    <path class="link-line link-billing" marker-start="url(#dot-purple)" d="M 780 320 L 780 420" />

    <!-- Billing Cycles -> Water Purchases -->
    <path class="link-line link-billing" marker-start="url(#dot-purple)" d="M 910 500 L 980 500" />

    <!-- Billing Cycles -> Tariff Plans -->
    <path class="link-line link-billing" marker-start="url(#dot-purple)" d="M 780 610 L 780 700" />

    <!-- Water Usage & Billing Cycle -> Bills -->
    <path class="link-line link-billing" marker-start="url(#dot-purple)" d="M 1770 330 L 1770 420" />

    <!-- Bills -> Payments -->
    <path class="link-line link-billing" marker-start="url(#dot-purple)" d="M 1900 520 L 1980 520" />

    <!-- Apartments & Residents -> Apartment Occupancies -->
    <path class="link-line link-tenancy" marker-start="url(#dot-amber)" d="M 1110 320 L 1110 420" />
    <path class="link-line link-tenancy" marker-start="url(#dot-amber)" d="M 280 470 L 980 470" />

    <!-- Apartments -> Occupancy Requests -->
    <path class="link-line link-tenancy" marker-start="url(#dot-amber)" d="M 1030 320 L 1030 680 L 980 680" />

    <!-- Apartments & Occupancies -> Household Size History -->
    <path class="link-line link-tenancy" marker-start="url(#dot-amber)" d="M 1160 610 L 1160 700" />

    <!-- Apartments & Occupancies -> Away Periods -->
    <path class="link-line link-tenancy" marker-start="url(#dot-amber)" d="M 1240 520 L 1310 520" />

    <!-- Apartments -> Leak Incidents -->
    <path class="link-line link-ai" marker-start="url(#dot-rose)" d="M 1200 320 L 1200 1020 L 980 1020" />

    <!-- Leak Incidents -> Leak Feedback -->
    <path class="link-line link-ai" marker-start="url(#dot-rose)" d="M 910 1120 L 980 1120" />
  </svg>

  <!-- =================================================================== -->
  <!-- 1. IDENTITY & AUTHENTICATION DOMAIN -->
  <!-- =================================================================== -->

  <!-- USERS -->
  <div class="entity-card domain-identity" style="left: 40px; top: 80px; width: 240px;">
    <div class="card-header">
      <span>users</span>
      <span style="font-size:10px; opacity:0.8;">Root User Table</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> user_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">email (UNIQUE)</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">password (BCrypt)</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">name</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">role (ENUM)</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">status</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">created_at</span><span class="field-type">DATETIME</span></div>
    </div>
  </div>

  <!-- BUILDING OWNERS -->
  <div class="entity-card domain-identity" style="left: 330px; top: 80px; width: 260px;">
    <div class="card-header">
      <span>building_owners</span>
      <span style="font-size:10px; opacity:0.8;">Landlords</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> owner_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> user_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">company_name</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">phone_number</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">approval_status</span><span class="field-type">ENUM(PENDING,APPROVED)</span></div>
      <div class="field-row"><span class="field-name">created_at</span><span class="field-type">DATETIME</span></div>
    </div>
  </div>

  <!-- RESIDENTS -->
  <div class="entity-card domain-identity" style="left: 40px; top: 360px; width: 240px;">
    <div class="card-header">
      <span>residents</span>
      <span style="font-size:10px; opacity:0.8;">Tenant Profiles</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> resident_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> user_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">phone</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">alternate_phone</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">status</span><span class="field-type">VARCHAR(50)</span></div>
    </div>
  </div>

  <!-- =================================================================== -->
  <!-- 2. PROPERTY & METERING DOMAIN -->
  <!-- =================================================================== -->

  <!-- BUILDINGS -->
  <div class="entity-card domain-property" style="left: 650px; top: 80px; width: 260px;">
    <div class="card-header">
      <span>buildings</span>
      <span style="font-size:10px; opacity:0.8;">Complex Portfolio</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> owner_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">name</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">building_number</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">address / location</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">total_flats</span><span class="field-type">INT</span></div>
    </div>
  </div>

  <!-- APARTMENTS -->
  <div class="entity-card domain-property" style="left: 980px; top: 80px; width: 260px;">
    <div class="card-header">
      <span>apartments</span>
      <span style="font-size:10px; opacity:0.8;">Flat Units</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">apartment_name</span><span class="field-type">VARCHAR(100)</span></div>
      <div class="field-row"><span class="field-name">room_number</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">floor_number</span><span class="field-type">INT</span></div>
      <div class="field-row"><span class="field-name">unit_type (BHK)</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">status (VACANT/OCC)</span><span class="field-type">ENUM</span></div>
    </div>
  </div>

  <!-- WATER METERS -->
  <div class="entity-card domain-property" style="left: 1310px; top: 80px; width: 260px;">
    <div class="card-header">
      <span>water_meters</span>
      <span style="font-size:10px; opacity:0.8;">Hardware Devices</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> meter_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">meter_number (Unique)</span><span class="field-type">VARCHAR(100)</span></div>
      <div class="field-row"><span class="field-name">current_reading (kL)</span><span class="field-type">DECIMAL(12,2)</span></div>
      <div class="field-row"><span class="field-name">status</span><span class="field-type">ENUM(ACTIVE,FAULT)</span></div>
    </div>
  </div>

  <!-- WATER USAGE -->
  <div class="entity-card domain-property" style="left: 1640px; top: 80px; width: 260px;">
    <div class="card-header">
      <span>water_usage</span>
      <span style="font-size:10px; opacity:0.8;">Telemetry Log</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> usage_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> meter_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> billing_cycle_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">reading_time</span><span class="field-type">DATETIME</span></div>
      <div class="field-row"><span class="field-name">consumption_liters</span><span class="field-type">DECIMAL(12,2)</span></div>
      <div class="field-row"><span class="field-name">cumulative_reading</span><span class="field-type">DECIMAL(12,2)</span></div>
    </div>
  </div>

  <!-- =================================================================== -->
  <!-- 3. BILLING & PROCUREMENT DOMAIN -->
  <!-- =================================================================== -->

  <!-- BILLING CYCLES -->
  <div class="entity-card domain-billing" style="left: 650px; top: 420px; width: 260px;">
    <div class="card-header">
      <span>billing_cycles</span>
      <span style="font-size:10px; opacity:0.8;">Cycle Windows</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> id (cycle_id)</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">start_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">end_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">status (OPEN/FINALIZED)</span><span class="field-type">ENUM</span></div>
      <div class="field-row"><span class="field-name">created_at</span><span class="field-type">DATETIME</span></div>
    </div>
  </div>

  <!-- WATER PURCHASES -->
  <div class="entity-card domain-billing" style="left: 980px; top: 420px; width: 260px;">
    <div class="card-header">
      <span>water_purchases</span>
      <span style="font-size:10px; opacity:0.8;">Tanker Procurements</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> purchase_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> billing_cycle_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">supplier_name</span><span class="field-type">VARCHAR(255)</span></div>
      <div class="field-row"><span class="field-name">volume_kl</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">total_cost</span><span class="field-type">DECIMAL(12,2)</span></div>
      <div class="field-row"><span class="field-name">unit_cost</span><span class="field-type">DECIMAL(10,2)</span></div>
    </div>
  </div>

  <!-- TARIFF PLANS -->
  <div class="entity-card domain-billing" style="left: 650px; top: 700px; width: 260px;">
    <div class="card-header">
      <span>tariff_plans</span>
      <span style="font-size:10px; opacity:0.8;">3-Tier Rate Model</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> id (plan_id)</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> building_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> billing_cycle_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">average_procurement</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">markup_percentage</span><span class="field-type">DECIMAL(5,2)</span></div>
      <div class="field-row"><span class="field-name">base_tariff</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier1_rate (1.00x)</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier2_rate (1.25x)</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier3_rate (1.50x)</span><span class="field-type">DECIMAL(10,2)</span></div>
    </div>
  </div>

  <!-- BILLS -->
  <div class="entity-card domain-billing" style="left: 1640px; top: 420px; width: 260px;">
    <div class="card-header">
      <span>bills</span>
      <span style="font-size:10px; opacity:0.8;">Invoices &amp; Charges</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> bill_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> billing_cycle_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> resident_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> usage_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">consumption_kl</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier1_amount</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier2_amount</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">tier3_amount</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">total_amount</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">status (PAID/UNPAID)</span><span class="field-type">ENUM</span></div>
    </div>
  </div>

  <!-- PAYMENTS -->
  <div class="entity-card domain-billing" style="left: 1980px; top: 420px; width: 250px;">
    <div class="card-header">
      <span>payments</span>
      <span style="font-size:10px; opacity:0.8;">Settlement Ledger</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> payment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> bill_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">payment_reference</span><span class="field-type">VARCHAR(100)</span></div>
      <div class="field-row"><span class="field-name">amount_paid</span><span class="field-type">DECIMAL(10,2)</span></div>
      <div class="field-row"><span class="field-name">payment_method</span><span class="field-type">VARCHAR(50)</span></div>
      <div class="field-row"><span class="field-name">payment_date</span><span class="field-type">DATETIME</span></div>
    </div>
  </div>

  <!-- =================================================================== -->
  <!-- 4. TENANCY & OCCUPANCY DOMAIN -->
  <!-- =================================================================== -->

  <!-- APARTMENT OCCUPANCIES -->
  <div class="entity-card domain-tenancy" style="left: 980px; top: 420px; width: 260px; margin-top:280px;">
    <div class="card-header">
      <span>apartment_occupancies</span>
      <span style="font-size:10px; opacity:0.8;">Lease Contracts</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> occupancy_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> resident_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">start_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">end_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">status (ACTIVE)</span><span class="field-type">ENUM</span></div>
    </div>
  </div>

  <!-- AWAY PERIODS -->
  <div class="entity-card domain-tenancy" style="left: 1310px; top: 420px; width: 260px; margin-top:280px;">
    <div class="card-header">
      <span>away_periods</span>
      <span style="font-size:10px; opacity:0.8;">Vacation Schedules</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> away_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> occupancy_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">start_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">end_date</span><span class="field-type">DATE</span></div>
      <div class="field-row"><span class="field-name">is_active</span><span class="field-type">BOOLEAN</span></div>
    </div>
  </div>

  <!-- =================================================================== -->
  <!-- 5. AI & INCIDENT MONITORING DOMAIN -->
  <!-- =================================================================== -->

  <!-- LEAK INCIDENTS -->
  <div class="entity-card domain-ai" style="left: 650px; top: 1000px; width: 260px;">
    <div class="card-header">
      <span>leak_incidents</span>
      <span style="font-size:10px; opacity:0.8;">ML Anomalies</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> incident_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">severity (CRITICAL,HIGH)</span><span class="field-type">ENUM</span></div>
      <div class="field-row"><span class="field-name">risk_score (0.00-1.00)</span><span class="field-type">DECIMAL(5,4)</span></div>
      <div class="field-row"><span class="field-name">reason_code</span><span class="field-type">VARCHAR(100)</span></div>
      <div class="field-row"><span class="field-name">detected_at</span><span class="field-type">DATETIME</span></div>
      <div class="field-row"><span class="field-name">status (OPEN/RESOLVED)</span><span class="field-type">ENUM</span></div>
    </div>
  </div>

  <!-- LEAK FEEDBACK -->
  <div class="entity-card domain-ai" style="left: 980px; top: 1000px; width: 260px;">
    <div class="card-header">
      <span>leak_feedback</span>
      <span style="font-size:10px; opacity:0.8;">Active Learning</span>
    </div>
    <div class="card-body">
      <div class="field-row pk"><span class="field-name"><span class="key-badge kb-pk">PK</span> feedback_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> apartment_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> usage_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row fk"><span class="field-name"><span class="key-badge kb-fk">FK</span> submitted_by_user_id</span><span class="field-type">BIGINT</span></div>
      <div class="field-row"><span class="field-name">classification</span><span class="field-type">CONFIRMED / FALSE</span></div>
      <div class="field-row"><span class="field-name">comments</span><span class="field-type">TEXT</span></div>
      <div class="field-row"><span class="field-name">created_at</span><span class="field-type">DATETIME</span></div>
    </div>
  </div>

</div>

</body>
</html>
"""

    html_file = os.path.abspath('er_diagram_canvas.html')
    with open(html_file, 'w', encoding='utf-8') as f:
        f.write(html_content)

    print("Rendering high-resolution ER diagram image via Selenium Edge...")
    options = Options()
    options.add_argument('--headless=new')
    options.add_argument('--window-size=2560,1680')
    options.add_argument('--disable-gpu')
    options.add_argument('--hide-scrollbars')
    options.add_argument('--force-device-scale-factor=1.5')

    driver = webdriver.Edge(options=options)
    try:
        driver.get('file:///' + html_file.replace('\\', '/'))
        time.sleep(1.5)
        output_png = 'AquaFlow_ER_Diagram.png'
        driver.save_screenshot(output_png)
        print(f"Successfully generated: {output_png} (Size: {os.path.getsize(output_png)} bytes)")
    finally:
        driver.quit()

if __name__ == '__main__':
    generate_er_diagram_image()
