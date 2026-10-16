import os
import sys
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def build_complete_system_documentation():
    print("Initializing AquaFlow Master System Documentation Generator...")
    doc = docx.Document()

    # 1-inch margins
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        section.page_width = Inches(8.5)
        section.page_height = Inches(11.0)

    # Palette
    C_NAVY = RGBColor(15, 23, 42)      # #0F172A
    C_BLUE = RGBColor(2, 132, 199)     # #0284C7
    C_SLATE = RGBColor(30, 41, 59)     # #1E293B
    C_MUTED = RGBColor(100, 116, 139)  # #64748B
    C_EMERALD = RGBColor(16, 185, 129) # #10B981
    C_AMBER = RGBColor(217, 119, 6)    # #D97706
    C_ROSE = RGBColor(225, 29, 72)     # #E11D48

    HEX_NAVY = "0F172A"
    HEX_LIGHT_BLUE = "F0F9FF"
    HEX_LIGHT_GRAY = "F8FAFC"
    HEX_BORDER = "CBD5E1"
    HEX_EMERALD_BG = "ECFDF5"
    HEX_AMBER_BG = "FFFBEB"
    HEX_ROSE_BG = "FFF1F2"

    def set_cell_background(cell, hex_color):
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
        cell._tc.get_or_add_tcPr().append(shd)

    def set_cell_margins(cell, top=120, bottom=120, left=180, right=180):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = OxmlElement('w:tcMar')
        for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
            node = OxmlElement(f'w:{m}')
            node.set(qn('w:w'), str(val))
            node.set(qn('w:type'), 'dxa')
            tcMar.append(node)
        tcPr.append(tcMar)

    def add_title(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(4)
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(26)
        run.font.bold = True
        run.font.color.rgb = C_NAVY
        return p

    def add_subtitle(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(18)
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(13)
        run.font.color.rgb = C_BLUE
        return p

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(22)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(18)
        run.font.bold = True
        run.font.color.rgb = C_NAVY
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(14)
        run.font.bold = True
        run.font.color.rgb = C_BLUE
        return p

    def add_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(12)
        run.font.bold = True
        run.font.color.rgb = C_SLATE
        return p

    def add_p(text, bold=False, italic=False, color=C_SLATE, space_after=6):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(11)
        run.font.bold = bold
        run.font.italic = italic
        run.font.color.rgb = color
        return p

    def add_bullet(bold_prefix, text):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r1 = p.add_run(bold_prefix + ": ")
            r1.font.name = 'Calibri'
            r1.font.size = Pt(11)
            r1.font.bold = True
            r1.font.color.rgb = C_SLATE
        r2 = p.add_run(text)
        r2.font.name = 'Calibri'
        r2.font.size = Pt(11)
        r2.font.color.rgb = C_SLATE
        return p

    def add_callout(title, text, callout_type="info"):
        bg_map = {
            "info": HEX_LIGHT_BLUE,
            "success": HEX_EMERALD_BG,
            "warning": HEX_AMBER_BG,
            "danger": HEX_ROSE_BG
        }
        color_map = {
            "info": C_BLUE,
            "success": C_EMERALD,
            "warning": C_AMBER,
            "danger": C_ROSE
        }
        bg = bg_map.get(callout_type, HEX_LIGHT_BLUE)
        accent = color_map.get(callout_type, C_BLUE)

        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        tbl.columns[0].width = Inches(6.5)

        cell = tbl.cell(0, 0)
        set_cell_background(cell, bg)
        set_cell_margins(cell, top=140, bottom=140, left=200, right=200)

        p = cell.paragraphs[0]
        p.paragraph_format.space_after = Pt(3)
        r_title = p.add_run(f"[{title.upper()}] ")
        r_title.font.name = 'Calibri'
        r_title.font.size = Pt(11)
        r_title.font.bold = True
        r_title.font.color.rgb = accent

        r_text = p.add_run(text)
        r_text.font.name = 'Calibri'
        r_text.font.size = Pt(10.5)
        r_text.font.color.rgb = C_SLATE

        # Space after callout
        p_space = doc.add_paragraph()
        p_space.paragraph_format.space_before = Pt(2)
        p_space.paragraph_format.space_after = Pt(4)

    def add_table_data(headers, rows, col_widths=None):
        tbl = doc.add_table(rows=len(rows) + 1, cols=len(headers))
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False

        # Set column widths
        if col_widths:
            for i, w in enumerate(col_widths):
                tbl.columns[i].width = Inches(w)

        # Header Row
        hdr_cells = tbl.rows[0].cells
        for i, header_text in enumerate(headers):
            hdr_cells[i].text = header_text
            set_cell_background(hdr_cells[i], HEX_NAVY)
            set_cell_margins(hdr_cells[i], top=120, bottom=120, left=140, right=140)
            p = hdr_cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in p.runs:
                run.font.name = 'Calibri'
                run.font.size = Pt(10)
                run.font.bold = True
                run.font.color.rgb = RGBColor(255, 255, 255)

        # Data Rows
        for r_idx, row_data in enumerate(rows):
            row_cells = tbl.rows[r_idx + 1].cells
            row_bg = HEX_LIGHT_GRAY if r_idx % 2 == 1 else "FFFFFF"
            for c_idx, cell_value in enumerate(row_data):
                row_cells[c_idx].text = str(cell_value)
                set_cell_background(row_cells[c_idx], row_bg)
                set_cell_margins(row_cells[c_idx], top=100, bottom=100, left=140, right=140)
                p = row_cells[c_idx].paragraphs[0]
                p.paragraph_format.line_spacing = 1.1
                p.paragraph_format.space_after = Pt(2)
                for run in p.runs:
                    run.font.name = 'Calibri'
                    run.font.size = Pt(9.5)
                    run.font.color.rgb = C_SLATE
                    if c_idx == 0:
                        run.font.bold = True

        p_after = doc.add_paragraph()
        p_after.paragraph_format.space_after = Pt(8)

    # =========================================================================
    # COVER PAGE & DOCUMENT METADATA
    # =========================================================================
    add_title("AQUAFLOW SMART WATER MANAGEMENT SYSTEM")
    add_subtitle("Exhaustive Architectural Specification, Workflow Connections Between User Roles, Computational Models & System Dictionary")

    add_p("Document Metadata & Governance Control:", bold=True)
    add_table_data(
        ["Property", "Specification Details"],
        [
            ["Document Title", "AquaFlow Complete System Architecture, Workflow Matrix & Operations Manual"],
            ["Target Platform", "Enterprise Smart Water Billing, Multi-Tenant Governance & ML Leak Management"],
            ["Backend Technology", "Java 17 (LTS), Spring Boot 3.2.5, Spring Security 6, Spring Data JPA / Hibernate 6.4"],
            ["Frontend Technology", "React 19.0.0, Vite 6.0.0, React Router DOM 7.1.0, Bootstrap 5.3.3, Recharts 3.10.1"],
            ["Machine Learning", "Python 3.12, FastAPI, LightGBM Quantile Regressors (q10, q50, q95), Scikit-Learn"],
            ["Database System", "MySQL 8.0 Enterprise Relational Store with 16 Normalized Entities"],
            ["Report Generation", "iText PDF 5.5.13.3 (Dynamic Invoices) & Spring JavaMailSender (Asynchronous SMTP)"],
            ["Security & Auth", "Stateless JSON Web Tokens (JJWT 0.11.5) with BCrypt Hashing & Method-Level RBAC"],
            ["Target Environment", "Monorepo Deployment (Localhost: Frontend 5173, Backend 8080, ML Service 8000)"],
            ["File Compliance", "Lightweight Structured Word Document (< 30 MB) Optimized for LLM Ingestion & PPT Preparation"]
        ],
        [2.2, 4.3]
    )

    add_callout(
        "Executive Intent & Purpose",
        "This master architectural document represents the single source of truth for the AquaFlow platform. "
        "It provides an exhaustive technical breakdown of every subsystem, user persona, database entity, "
        "mathematical algorithm, and RESTful API endpoint, explicitly detailing the end-to-end operational workflows "
        "and data exchanges between the Super Administrator, Building Owner, and Resident roles.",
        "info"
    )

    doc.add_page_break()

    # =========================================================================
    # CHAPTER 1: EXECUTIVE SUMMARY & SYSTEM ARCHITECTURE
    # =========================================================================
    add_h1("Chapter 1: Executive Summary & System Architecture")

    add_h2("1.1 Executive Summary & Problem Space")
    add_p(
        "Traditional water billing in multi-tenant apartment complexes and gated residential societies suffers from critical systemic inefficiencies: "
        "1) Unmetered flat-rate water sharing penalizes conservative households while subsidizing excessive consumption; "
        "2) Building owners frequently purchase external water tanker loads during municipal water shortages at highly volatile rates, bearing substantial losses or imposing unfair lump-sum levies; "
        "3) Undetected concealed pipe bursts and running toilet valves waste tens of thousands of liters of clean water before discovery; and "
        "4) Generic static-threshold leak alerts generate severe alarm fatigue among residents when benign high-usage events occur (e.g., weekend family gatherings or terrace gardening)."
    )
    add_p(
        "AquaFlow resolves these core problems through a fully automated, multi-tenant digital ecosystem combining digital water metering, dynamic weighted-average procurement tariffs, "
        "and machine-learning-driven Quantile Regression leak anomaly detection paired with resident active learning feedback."
    )

    add_h2("1.2 Hybrid Microservice Topology")
    add_p(
        "AquaFlow is architected as a modern hybrid distributed system consisting of three specialized computing tiers:"
    )
    add_bullet("Presentation Tier (React 19 SPA)", "Engineered with React 19, Vite 6, and Bootstrap 5. Provides role-segregated dashboards for Super Admin, Building Owner, and Resident. Utilizes Recharts for live telemetry visualization and Axios for authenticated HTTP communication.")
    add_bullet("Business Core Tier (Spring Boot 3.2.5)", "High-performance enterprise Java core operating on Java 17. Houses all business domains: building and flat hierarchy, digital meter pairing, bulk water tanker logging, slab-based tariff computation, batch billing execution, iText PDF generation, and asynchronous email dispatch.")
    add_bullet("Predictive AI Tier (Python 3.12 FastAPI)", "Dedicated microservice executing LightGBM Quantile Gradient Boosted Regression models. Exposes high-throughput asynchronous endpoints (/v1/leak/score) protected by X-API-KEY authentication to evaluate per-flat rolling consumption curves and assign human-readable reason attribution codes.")

    add_h2("1.3 High-Reliability Dual-Engine Fallback Design")
    add_p(
        "A critical vulnerability in distributed architectures is the single-point-of-failure (SPOF) introduced when a business backend depends on an external AI service. "
        "If the Python microservice crashes or experiences network degradation, billing runs or daily leak detection schedules could halt. "
        "AquaFlow eliminates this failure mode via a resilient dual-engine pattern:"
    )
    add_bullet("Primary Path", "Spring Boot's dedicated MlClient invokes the FastAPI service with a strict 5000ms connect and read timeout.")
    add_bullet("Automatic Failover Path", "If the Python service times out, throws a 5xx error, or cannot be reached, MlClient seamlessly catches the RestClientException and delegates the batch to Spring Boot's internal in-JVM statistical fallback service.")
    add_bullet("Statistical Rule", "The in-JVM fallback calculates: Threshold = Rolling Mean + 2 * Standard Deviation, dynamically scaled by flat household size and vacation away status. Scheduled jobs complete with 100% reliability and zero user disruption.")

    # =========================================================================
    # CHAPTER 2: MULTI-TENANT ROLE HIERARCHY & GOVERNANCE
    # =========================================================================
    add_h1("Chapter 2: Multi-Tenant Role Hierarchy & Governance Model")

    add_h2("2.1 Hierarchical Privilege Matrix")
    add_p(
        "AquaFlow enforces strict Role-Based Access Control (RBAC) powered by Spring Security 6. Users authenticate using stateless JSON Web Tokens (JWT) "
        "embedded in the HTTP Authorization header as a Bearer token. The platform segregates users into three distinct roles:"
    )

    add_table_data(
        ["User Role", "System Authority", "Primary Operational Responsibilities", "Access Scope"],
        [
            [
                "ROLE_SUPER_ADMIN",
                "Platform Root Governance",
                "Reviews & approves/rejects newly registered Building Owners; monitors platform-wide KPIs, aggregate consumption, and system health.",
                "Global platform-wide visibility across all owners, buildings, and registered users."
            ],
            [
                "ROLE_BUILDING_OWNER",
                "Property Portfolio Manager",
                "Configures buildings and flats; binds water meters; logs bulk water purchases; sets markup %; executes 1-click bulk billing; monitors property leaks.",
                "Strictly restricted to buildings, flats, meters, and residents owned by their specific owner account."
            ],
            [
                "ROLE_RESIDENT",
                "End-User Consumer",
                "Submits occupancy requests; tracks real-time usage; reviews 3-tier itemized bills; pays online; declares household size; schedules away periods; submits leak feedback.",
                "Strictly restricted to their own assigned flat, water meter telemetry, bills, and incident reports."
            ]
        ],
        [1.8, 1.4, 2.3, 1.0]
    )

    add_h2("2.2 Super Admin Approval Gatekeeper Architecture")
    add_p(
        "A core architectural safeguard in AquaFlow is the Super Admin Verification Gatekeeper. "
        "In unmoderated systems, fraudulent or unvetted landlords could self-register, create fictitious apartments, and extract utility payments from unsuspecting tenants. "
        "AquaFlow enforces strict security invariants across both database and API queries:"
    )
    add_bullet("Initial Registration Status", "When a Building Owner registers via /auth/register-owner, their account is initialized with approvalStatus = PENDING.")
    add_bullet("Inventory Privacy & Isolation", "Public endpoints (/api/apartments/owners, /api/apartments/buildings, /api/apartments/available) strictly query only owners whose approvalStatus == APPROVED. Unapproved owners, their properties, and vacant flats are completely invisible to residents.")
    add_bullet("Occupancy Request Rejection", "If a resident attempts to submit an occupancy request targeting a flat owned by a pending or rejected owner, OccupancyService intercepts the request and throws an HTTP 400 BadRequestException.")
    add_bullet("Building Creation Lock", "BuildingOwnerService verifies that the authenticated owner is APPROVED before permitting new building creation.")

    # =========================================================================
    # CHAPTER 3: COMPREHENSIVE WORKFLOWS & ROLE CONNECTIONS
    # =========================================================================
    add_h1("Chapter 3: End-to-End Workflows & Role Interconnections")
    add_p(
        "This chapter details the precise operational workflows and state transitions connecting the Super Admin, Building Owner, and Resident. "
        "Every interaction is mapped from inception to resolution."
    )

    add_h2("3.1 Workflow 1: Building Owner Onboarding & Super Admin Approval")
    add_p("The lifecycle begins when a prospective property owner registers on the platform:")
    add_bullet("Step 1 (Owner Action)", "Prospective landlord visits /register-owner and submits their business name, contact email, phone number, and password.")
    add_bullet("Step 2 (System Action)", "AuthService hashes the password using BCrypt (10 rounds), creates a User record with ROLE_BUILDING_OWNER, and persists a corresponding BuildingOwner entity with approvalStatus = PENDING.")
    add_bullet("Step 3 (Super Admin Inspection)", "Super Admin logs in and navigates to Pending Approvals (/admin/pending-owners). The screen lists all pending owners, their contact details, and registered property count.")
    add_bullet("Step 4 (Approval Execution)", "Super Admin clicks 'Approve'. SuperAdminService updates approvalStatus to APPROVED and records a timestamped entry in the system audit history.")
    add_bullet("Step 5 (Activation Effect)", "The Building Owner's buildings and flats immediately become visible across the public directory, enabling residents to discover and request occupancy.")

    add_h2("3.2 Workflow 2: Property Setup, Flat Cataloging & Meter Binding")
    add_p("Once approved by the Super Admin, the Building Owner provisions their physical property structure:")
    add_bullet("Step 1 (Building Provisioning)", "Building Owner visits /owner/buildings and enters building name (e.g., 'Skyline Luxury Towers'), address, building number, and total flat count.")
    add_bullet("Step 2 (Apartment Matrix Setup)", "Owner configures flat units across floor levels, selecting unit typologies (1BHK, 2BHK, 3BHK, Penthouse). Initial status is set to VACANT.")
    add_bullet("Step 3 (Digital Water Meter Pairing)", "Owner provisions a digital water meter for each apartment, assigning a unique serial number (e.g., 'WM-SKY-101') and recording initial cumulative meter reading.")
    add_bullet("Step 4 (Baseline Readiness)", "The flat is now cataloged, metered, and primed for tenant occupancy.")

    add_h2("3.3 Workflow 3: Resident Onboarding & Tenancy Lifecycle")
    add_p("AquaFlow provides two synchronized pathways for resident onboarding into an apartment:")
    add_bullet("Pathway A: Self-Service Occupancy Request", "Resident registers via /register-resident, navigates to property selection, selects an approved Building Owner, chooses a building, and picks an available vacant flat (e.g., Flat 204). An OccupancyRequest is generated in PENDING status.")
    add_bullet("Pathway B: Owner Tokenized Email Invitation", "Building Owner navigates to Resident Management (/owner/residents), enters tenant email and flat number, and generates a secure tokenized invitation link dispatched via JavaMailSender. Resident clicks link, registers, and bypasses approval.")
    add_bullet("Owner Review & Acceptance", "For Pathway A, Building Owner reviews pending requests in /owner/residents and clicks 'Accept'. OccupancyService transitions the flat status to OCCUPIED and binds the resident User ID to ApartmentOccupancy.")
    add_bullet("Move-Out Processing", "When a tenant departs, Owner executes 'End Lease'. OccupancyService records lease end date, finalizes outstanding meter balances, and resets flat status to VACANT.")

    add_h2("3.4 Workflow 4: Bulk Water Procurement & Dynamic 3-Tier Tariff Recalculation")
    add_p(
        "Unlike legacy systems where static tariffs fail to account for escalating water procurement costs, AquaFlow's tariff engine is dynamically tied to actual bulk water purchases:"
    )
    add_bullet("Step 1 (Procurement Ingestion)", "During municipal deficits or peak summer seasons, Building Owner purchases water tanker loads. In /owner/billing ('2. Bulk Water Purchases' tab), Owner enters vendor name (e.g., 'Metro Tanker Logistics'), volume in kL (e.g., 500 kL), and total cost (e.g., $25,000).")
    add_bullet("Step 2 (Weighted Cost Computation)", "TariffCalculationService sums all active water purchases for the billing cycle and computes: Average Procurement Cost = Total Purchase Cost / Total Volume Purchased.")
    add_bullet("Step 3 (Owner Markup Margin)", "In the '3. Tiered Tariff' tab, Building Owner sets their margin (e.g., 20% markup). Base Tariff is automatically computed: Base Tariff = Average Procurement Cost * (1 + Markup / 100).")
    add_bullet("Step 4 (3-Tier Slab Derivation)", "The engine automatically updates three progressive tariff slabs: Tier 1 (Baseline: 0-20 kL at 1.0x Base Rate), Tier 2 (Moderate: 20-40 kL at 1.25x Base Rate), and Tier 3 (Surcharge: >40 kL at 1.50x Base Rate).")

    add_h2("3.5 Workflow 5: Monthly Billing Cycle & Automated Bulk Invoicing")
    add_p("At the conclusion of each billing period, monthly invoicing is executed with one click:")
    add_bullet("Step 1 (Cycle Initialization)", "Building Owner initializes a new BillingCycle (e.g., Oct 1 to Oct 31). System auto-populates dates based on previous cycle end.")
    add_bullet("Step 2 (Telemetry Delta Ingestion)", "System ingests start and end meter readings for all occupied flats: Delta Volume = Reading_end - Reading_start.")
    add_bullet("Step 3 (Slab Computation)", "BulkBillingService iterates across all active leases. For each flat, delta volume is progressively distributed across Tier 1, Tier 2, and Tier 3 brackets, computing exact subtotals and connection charges.")
    add_bullet("Step 4 (PDF Invoice Assembly)", "iText PDF engine compiles an official, tax-compliant PDF invoice featuring complete itemized tier usage, meter delta readings, property details, and payment instructions.")
    add_bullet("Step 5 (Asynchronous Notification)", "Spring Boot triggers an asynchronous email dispatch containing bill summary, due date, and payment link directly to the resident's registered email address.")

    add_h2("3.6 Workflow 6: Resident Consumption Tracking, Payment & PDF Receipt")
    add_p("Residents maintain total transparency over their water utility obligations:")
    add_bullet("Real-Time Telemetry Tracking", "Resident accesses /resident/dashboard to view live daily and hourly consumption curves, comparing current usage against historical averages and peer units.")
    add_bullet("Bill Inspection", "Resident opens My Bills (/resident/bills) to inspect their itemized invoice, seeing precisely how many liters fell into Tier 1, Tier 2, or Tier 3.")
    add_bullet("Simulated Online Payment", "Resident clicks 'Pay Bill'. System initiates simulated payment gateway, verifies transaction token, updates Bill status to PAID, and records a Payment entity with unique transaction ID.")
    add_bullet("Downloadable Receipt", "Resident instantly downloads their stamped official iText PDF payment receipt for personal record-keeping.")

    add_h2("3.7 Workflow 7: Machine Learning Leak Anomaly Detection & Attribution")
    add_p("Continuous monitoring operates autonomously in the background to detect leaks before catastrophic damage occurs:")
    add_bullet("Scheduled Execution", "AlertScheduler cron executes every night (or on-demand). Gathers rolling 14-day hourly water telemetry for every occupied flat.")
    add_bullet("Feature Engineering", "Constructs feature vectors: rolling mean, standard deviation, continuous night flow (2 AM - 5 AM minimum flow), day-of-week, flat typology, household occupant count, and vacation away status.")
    add_bullet("Microservice Scoring", "Spring Boot MlClient batches features and calls Python FastAPI service (POST /v1/leak/score). LightGBM Quantile Regressors compute q10, q50, and q95 consumption boundaries.")
    add_bullet("Anomaly Classification", "If consumption exceeds q95 and continuous flow is present, an anomaly is flagged with a Risk Score (0.0 to 1.0) and assigned a Reason Code: CONTINUOUS_NIGHT_FLOW, ABNORMAL_SPIKE_FOR_HOUSEHOLD_SIZE, or UNEXPLAINED_AWAY_CONSUMPTION.")
    add_bullet("Alert Notification", "LeakIncident is persisted with severity (LOW, MEDIUM, HIGH, CRITICAL). Immediate visual banners appear on the resident dashboard and owner monitoring console.")

    add_h2("3.8 Workflow 8: Resident Active Learning Feedback Loop")
    add_p("To eliminate false positive alert fatigue, AquaFlow empowers residents to validate detected incidents:")
    add_bullet("Step 1 (Incident Inspection)", "Resident observes a prominent alert banner on /resident/leak-status detailing the detected leak severity, estimated excess volume, and suspected cause.")
    add_bullet("Step 2 (Feedback Submission)", "Resident opens the Feedback Modal and selects: 1) Confirmed Leak (e.g., running toilet valve, dripping tap), 2) False Alarm (e.g., guests visiting, water heater filling), or 3) Resolved (plumber completed repairs).")
    add_bullet("Step 3 (Ground-Truth Persistence)", "ResidentLeakAndHouseholdController persists a LeakFeedback record with verified label, notes, and timestamp.")
    add_bullet("Step 4 (Automated Retraining Pipeline)", "The Python ML retraining script (retrain_from_feedback.py) ingests ground-truth feedback, fine-tuning quantile regression weights and adjusting baseline tolerances to permanently suppress recurring false alarms.")

    add_h2("3.9 Workflow 9: Household Size & Vacation Away-Period Normalization")
    add_p("Demographic normalization ensures fair scoring tailored to actual flat occupancy:")
    add_bullet("Household Occupant Updates", "In /resident/leak-status, resident updates their occupant count (e.g., increasing from 2 to 4 people after family arrives). HouseholdService updates current size and logs a historical record in HouseholdSizeHistory.")
    add_bullet("Expected Consumption Scaling", "ML models and in-JVM fallback automatically scale expected daily baseline consumption per capita (~150 liters/person/day), preventing false alarms when large families consume normal volumes.")
    add_bullet("Vacation Away-Period Scheduling", "Resident schedules upcoming travel dates (e.g., Away from Nov 10 to Nov 20). System creates an AwayPeriod record. During this window, expected consumption drops to 0. Any continuous flow triggers an immediate CRITICAL leak alert, ensuring vacation pipe bursts are caught instantly.")

    # =========================================================================
    # CHAPTER 4: RELATIONAL DATABASE SPECIFICATION
    # =========================================================================
    add_h1("Chapter 4: Complete Relational Database Specification (Data Dictionary)")
    add_p(
        "AquaFlow's persistence layer is built on MySQL 8.0, comprising 16 normalized entities engineered with foreign key constraints, "
        "audit timestamps, and optimized indexes on high-frequency query paths (telemetry timestamps, owner IDs, and cycle IDs)."
    )

    db_tables = [
        ("users", [
            ("user_id", "BIGINT (PK, Auto-Inc)", "Primary user identifier"),
            ("email", "VARCHAR(255) (Unique)", "Login credential & notification destination"),
            ("password", "VARCHAR(255)", "BCrypt 10-round salted password hash"),
            ("name", "VARCHAR(255)", "Full user display name"),
            ("role", "ENUM", "ROLE_SUPER_ADMIN, ROLE_BUILDING_OWNER, ROLE_RESIDENT"),
            ("status", "VARCHAR(50)", "ACTIVE, INACTIVE, SUSPENDED"),
            ("created_at", "DATETIME", "Record creation timestamp")
        ]),
        ("building_owners", [
            ("owner_id", "BIGINT (PK, Auto-Inc)", "Primary building owner identifier"),
            ("user_id", "BIGINT (FK -> users)", "Associated authentication user record"),
            ("company_name", "VARCHAR(255)", "Registered business or property management entity"),
            ("phone_number", "VARCHAR(50)", "Direct contact phone number"),
            ("approval_status", "ENUM", "PENDING, APPROVED, REJECTED (Super Admin Gatekeeper)"),
            ("created_at", "DATETIME", "Registration timestamp")
        ]),
        ("buildings", [
            ("building_id", "BIGINT (PK, Auto-Inc)", "Primary building identifier"),
            ("owner_id", "BIGINT (FK -> building_owners)", "Owning landlord or management firm"),
            ("name", "VARCHAR(255)", "Building name (e.g. Skyline Luxury Towers)"),
            ("building_number", "VARCHAR(50)", "Official identifier code (e.g. B-101)"),
            ("address", "VARCHAR(255)", "Physical street address and postal location"),
            ("total_flats", "INT", "Total number of apartment units constructed")
        ]),
        ("apartments", [
            ("apartment_id", "BIGINT (PK, Auto-Inc)", "Primary apartment identifier"),
            ("building_id", "BIGINT (FK -> buildings)", "Parent building complex"),
            ("apartment_name", "VARCHAR(100)", "Unit display label (e.g. Flat 204)"),
            ("room_number", "VARCHAR(50)", "Physical door/unit number (e.g. 204)"),
            ("floor_number", "INT", "Floor level in the building structure"),
            ("unit_type", "VARCHAR(50)", "STUDIO, 1BHK, 2BHK, 3BHK, PENTHOUSE"),
            ("status", "ENUM", "VACANT, OCCUPIED, MAINTENANCE")
        ]),
        ("water_meters", [
            ("meter_id", "BIGINT (PK, Auto-Inc)", "Primary meter identifier"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Bound apartment unit"),
            ("meter_number", "VARCHAR(100) (Unique)", "Hardware device serial number"),
            ("current_reading", "DECIMAL(12, 2)", "Latest cumulative volume reading (kL)"),
            ("status", "ENUM", "ACTIVE, INACTIVE, FAULTY, REPLACED")
        ]),
        ("water_usages", [
            ("usage_id", "BIGINT (PK, Auto-Inc)", "Telemetry log identifier"),
            ("meter_id", "BIGINT (FK -> water_meters)", "Reporting digital meter"),
            ("reading_time", "DATETIME", "Timestamp of telemetry reading ingestion"),
            ("consumption_liters", "DECIMAL(12, 2)", "Volume consumed during reporting interval"),
            ("cumulative_reading", "DECIMAL(12, 2)", "Total meter reading at timestamp")
        ]),
        ("water_purchases", [
            ("purchase_id", "BIGINT (PK, Auto-Inc)", "Procurement log identifier"),
            ("building_id", "BIGINT (FK -> buildings)", "Recipient building property"),
            ("billing_cycle_id", "BIGINT (FK -> billing_cycles)", "Associated billing cycle"),
            ("supplier_name", "VARCHAR(255)", "Vendor (Municipal Supply / Private Tanker)"),
            ("volume_kl", "DECIMAL(10, 2)", "Water volume received in kiloliters"),
            ("total_cost", "DECIMAL(12, 2)", "Total purchase cost in local currency"),
            ("unit_cost", "DECIMAL(10, 2)", "Cost per kiloliter (calculated)")
        ]),
        ("tariff_plans", [
            ("plan_id", "BIGINT (PK, Auto-Inc)", "Tariff record identifier"),
            ("building_id", "BIGINT (FK -> buildings)", "Property tariff applies to"),
            ("billing_cycle_id", "BIGINT (FK -> billing_cycles)", "Associated billing cycle"),
            ("average_procurement_cost", "DECIMAL(10, 2)", "Weighted cost from water_purchases"),
            ("markup_percentage", "DECIMAL(5, 2)", "Owner margin (e.g. 20.00%)"),
            ("base_tariff", "DECIMAL(10, 2)", "Procurement cost * (1 + markup/100)"),
            ("tier1_limit", "DECIMAL(10, 2)", "Upper boundary for Tier 1 (e.g. 20.0 kL)"),
            ("tier1_rate", "DECIMAL(10, 2)", "Base rate * 1.00"),
            ("tier2_limit", "DECIMAL(10, 2)", "Upper boundary for Tier 2 (e.g. 40.0 kL)"),
            ("tier2_rate", "DECIMAL(10, 2)", "Base rate * 1.25"),
            ("tier3_rate", "DECIMAL(10, 2)", "Base rate * 1.50 (Surcharge bracket)")
        ]),
        ("billing_cycles", [
            ("cycle_id", "BIGINT (PK, Auto-Inc)", "Billing cycle identifier"),
            ("building_id", "BIGINT (FK -> buildings)", "Target complex"),
            ("start_date", "DATE", "Cycle start date (e.g. 2026-10-01)"),
            ("end_date", "DATE", "Cycle end date (e.g. 2026-10-31)"),
            ("status", "ENUM", "OPEN, FINALIZED, ARCHIVED")
        ]),
        ("bills", [
            ("bill_id", "BIGINT (PK, Auto-Inc)", "Invoice identifier"),
            ("billing_cycle_id", "BIGINT (FK -> billing_cycles)", "Originating cycle"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Billed apartment unit"),
            ("resident_id", "BIGINT (FK -> users)", "Responsible resident"),
            ("consumption_kl", "DECIMAL(10, 2)", "Total volume consumed in cycle"),
            ("tier1_amount", "DECIMAL(10, 2)", "Amount incurred under Tier 1"),
            ("tier2_amount", "DECIMAL(10, 2)", "Amount incurred under Tier 2"),
            ("tier3_amount", "DECIMAL(10, 2)", "Amount incurred under Tier 3"),
            ("total_amount", "DECIMAL(10, 2)", "Final payable total"),
            ("payment_status", "ENUM", "PENDING, PAID, OVERDUE, CANCELLED"),
            ("pdf_url", "VARCHAR(255)", "Path to stored iText PDF tax invoice")
        ]),
        ("payments", [
            ("payment_id", "BIGINT (PK, Auto-Inc)", "Transaction identifier"),
            ("bill_id", "BIGINT (FK -> bills)", "Settled bill invoice"),
            ("payment_reference", "VARCHAR(100) (Unique)", "Unique gateway transaction code"),
            ("amount_paid", "DECIMAL(10, 2)", "Total settled amount"),
            ("payment_method", "VARCHAR(50)", "ONLINE_GATEWAY, UPI, CARD, BANK_TRANSFER"),
            ("payment_date", "DATETIME", "Timestamp of successful settlement")
        ]),
        ("apartment_occupancies", [
            ("occupancy_id", "BIGINT (PK, Auto-Inc)", "Lease record identifier"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Occupied flat"),
            ("resident_id", "BIGINT (FK -> users)", "Active resident tenant"),
            ("start_date", "DATE", "Tenancy commencement date"),
            ("end_date", "DATE", "Lease termination date (null if active)"),
            ("status", "ENUM", "ACTIVE, TERMINATED")
        ]),
        ("occupancy_requests", [
            ("request_id", "BIGINT (PK, Auto-Inc)", "Onboarding request identifier"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Target apartment unit"),
            ("resident_id", "BIGINT (FK -> users)", "Requesting resident applicant"),
            ("request_date", "DATETIME", "Submission timestamp"),
            ("status", "ENUM", "PENDING, APPROVED, REJECTED")
        ]),
        ("leak_incidents", [
            ("incident_id", "BIGINT (PK, Auto-Inc)", "Leak record identifier"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Affected apartment unit"),
            ("meter_id", "BIGINT (FK -> water_meters)", "Reporting digital meter"),
            ("severity", "ENUM", "LOW, MEDIUM, HIGH, CRITICAL"),
            ("risk_score", "DECIMAL(5, 4)", "Normalized ML anomaly score (0.0 to 1.0)"),
            ("reason_code", "VARCHAR(100)", "CONTINUOUS_NIGHT_FLOW, ABNORMAL_SPIKE, etc."),
            ("detected_at", "DATETIME", "Detection timestamp"),
            ("status", "ENUM", "OPEN, UNDER_INVESTIGATION, RESOLVED, FALSE_ALARM")
        ]),
        ("leak_feedbacks", [
            ("feedback_id", "BIGINT (PK, Auto-Inc)", "Feedback record identifier"),
            ("incident_id", "BIGINT (FK -> leak_incidents)", "Validated incident"),
            ("resident_id", "BIGINT (FK -> users)", "Resident providing verification"),
            ("classification", "ENUM", "CONFIRMED_LEAK, FALSE_ALARM, RESOLVED"),
            ("resident_comments", "TEXT", "Notes explaining cause (e.g. toilet flapper leak)"),
            ("submitted_at", "DATETIME", "Feedback timestamp")
        ]),
        ("away_periods", [
            ("away_id", "BIGINT (PK, Auto-Inc)", "Travel schedule identifier"),
            ("apartment_id", "BIGINT (FK -> apartments)", "Vacated flat unit"),
            ("resident_id", "BIGINT (FK -> users)", "Traveling resident"),
            ("start_date", "DATE", "Departure date"),
            ("end_date", "DATE", "Return date"),
            ("is_active", "BOOLEAN", "True during the away interval (suppresses alerts)")
        ])
    ]

    for tbl_name, cols in db_tables:
        add_h2(f"Table: {tbl_name}")
        col_rows = []
        for cname, ctype, cdesc in cols:
            col_rows.append([cname, ctype, cdesc])
        add_table_data(["Column Name", "Data Type & Constraints", "Functional Description"], col_rows, [1.8, 2.0, 2.7])

    # =========================================================================
    # CHAPTER 5: MATHEMATICAL FORMULATIONS & ALGORITHMS
    # =========================================================================
    add_h1("Chapter 5: Mathematical Formulations & Computational Algorithms")

    add_h2("5.1 Consumption Volume Delta")
    add_p(
        "Water consumption is computed as the monotonic difference between successive telemetry readings:\n"
        "Delta V = Reading_end - Reading_start\n"
        "If a meter hardware replacement occurs during a cycle, the system records: Delta V = (Reading_pre_replace - Reading_start) + (Reading_end - Reading_post_replace)."
    )

    add_h2("5.2 Weighted-Average Procurement Cost Formula")
    add_p(
        "When a building procures water from multiple tanker vendors at varying rates across a cycle, the average unit cost is computed as:\n"
        "Average Procurement Cost = Sum(Volume_i * UnitCost_i) / Sum(Volume_i)\n"
        "Where Volume_i is volume in kiloliters and UnitCost_i is price per kiloliter for transaction i."
    )

    add_h2("5.3 Dynamic 3-Tier Tariff Calculation Formula")
    add_p(
        "Base Tariff = Average Procurement Cost * (1 + MarkupPercentage / 100)\n\n"
        "For a flat consuming total volume V:\n"
        "• Tier 1 Volume (V1) = Min(V, Tier1_Limit)\n"
        "• Tier 2 Volume (V2) = Max(0, Min(V - Tier1_Limit, Tier2_Limit - Tier1_Limit))\n"
        "• Tier 3 Volume (V3) = Max(0, V - Tier2_Limit)\n\n"
        "Total Bill Amount = (V1 * BaseTariff * 1.00) + (V2 * BaseTariff * 1.25) + (V3 * BaseTariff * 1.50) + ConnectionFee"
    )

    add_h2("5.4 Quantile Regression Loss Function (Pinball Loss)")
    add_p(
        "To predict asymmetric consumption bounds, LightGBM optimizes the pinball loss function for quantile q in {0.10, 0.50, 0.95}:\n"
        "L_q(y, y_pred) = Max( q * (y - y_pred), (q - 1) * (y - y_pred) )\n"
        "Optimizing q=0.10 yields the lower expected bound, q=0.50 yields the median, and q=0.95 yields the upper acceptable limit."
    )

    add_h2("5.5 In-JVM Statistical Fallback Heuristic")
    add_p(
        "When the remote ML microservice is unreachable, Spring Boot computes the threshold locally:\n"
        "Threshold = Mean_14d + (2.0 * StdDev_14d) * (HouseholdSize / DefaultBaseline)\n"
        "If CurrentConsumption > Threshold and ContinuousNightFlow > MinimumBaseline, RiskScore = Min(1.0, (CurrentConsumption - Threshold) / Threshold)."
    )

    # =========================================================================
    # CHAPTER 6: EXHAUSTIVE REST API REFERENCE
    # =========================================================================
    add_h1("Chapter 6: Exhaustive REST API Specification")
    add_p(
        "All endpoints operate on JSON payloads under context path /api. "
        "Secured endpoints require HTTP header: Authorization: Bearer <JWT_TOKEN>."
    )

    api_groups = [
        ("Authentication & Identity Endpoints (/api/auth)", [
            ("POST", "/api/auth/login", "Public", "Authenticates user with email & password; returns JWT token, role, and profile info."),
            ("POST", "/api/auth/register-owner", "Public", "Self-registers Building Owner in PENDING status pending Super Admin approval."),
            ("POST", "/api/auth/register-resident", "Public", "Self-registers Resident account and links to selected approved flat."),
            ("GET", "/api/auth/me", "Authenticated", "Returns authenticated user profile, active role, and assigned property context.")
        ]),
        ("Public Inventory Discovery Endpoints (/api/apartments)", [
            ("GET", "/api/apartments/owners", "Public", "Returns list of Super Admin APPROVED Building Owners only."),
            ("GET", "/api/apartments/owners/{id}/buildings", "Public", "Returns buildings owned by approved owner; empty list if owner unapproved."),
            ("GET", "/api/apartments/buildings", "Public", "Returns all buildings belonging to Super Admin-approved owners."),
            ("GET", "/api/apartments/available", "Public", "Returns vacant apartments in approved buildings ready for resident occupancy.")
        ]),
        ("Super Admin Governance Endpoints (/api/admin)", [
            ("GET", "/api/admin/dashboard", "SUPER_ADMIN", "Returns platform-wide metrics: total owners, buildings, residents, and revenue."),
            ("GET", "/api/admin/owners", "SUPER_ADMIN", "Returns directory of all registered Building Owners with status filter."),
            ("GET", "/api/admin/pending-owners", "SUPER_ADMIN", "Returns queue of Building Owners awaiting Super Admin approval."),
            ("POST", "/api/admin/owners/{id}/approve", "SUPER_ADMIN", "Approves owner account; activates public visibility of their properties."),
            ("POST", "/api/admin/owners/{id}/reject", "SUPER_ADMIN", "Rejects owner account; locks buildings and hides from public views."),
            ("GET", "/api/admin/history", "SUPER_ADMIN", "Returns audit log of owner registration and approval status changes.")
        ]),
        ("Building Owner Operations Endpoints (/api)", [
            ("GET", "/api/owners/buildings", "BUILDING_OWNER", "Returns all buildings belonging to the authenticated building owner."),
            ("POST", "/api/owners/buildings", "BUILDING_OWNER", "Creates a new building complex (requires APPROVED owner status)."),
            ("GET", "/api/owners/apartments", "BUILDING_OWNER", "Returns flat catalog, room numbers, typologies, and meter pairings."),
            ("POST", "/api/owners/apartments", "BUILDING_OWNER", "Creates a new apartment unit in a specified building."),
            ("GET", "/api/water-purchases/cycle/{cId}", "BUILDING_OWNER", "Returns bulk water tanker purchase logs for a billing cycle."),
            ("POST", "/api/water-purchases", "BUILDING_OWNER", "Logs a new bulk water purchase; dynamically recalculates base tariff."),
            ("GET", "/api/tariff-plans/building/{bId}", "BUILDING_OWNER", "Returns current 3-tier tariff plan and calculated slab rates."),
            ("POST", "/api/tariff-plans/cycle/{cId}/markup", "BUILDING_OWNER", "Updates owner markup percentage; dynamically updates 3-tier rates."),
            ("GET", "/api/owner/billing-cycles", "BUILDING_OWNER", "Returns billing cycle history for an owner's building."),
            ("POST", "/api/owner/billing-cycles", "BUILDING_OWNER", "Initializes a new monthly billing cycle."),
            ("POST", "/api/bills/owner/generate-bulk", "BUILDING_OWNER", "Executes 1-click bulk billing across all occupied flats in cycle."),
            ("GET", "/api/owner/leak-monitoring/{bId}", "BUILDING_OWNER", "Returns property-wide leak incidents, risk scores, and statuses.")
        ]),
        ("Resident Self-Service Endpoints (/api/resident)", [
            ("GET", "/api/resident/dashboard", "RESIDENT", "Returns flat occupancy, current meter reading, and usage summary."),
            ("GET", "/api/resident/bills", "RESIDENT", "Returns resident bill history with itemized 3-tier cost breakdown."),
            ("POST", "/api/resident/bills/{id}/pay", "RESIDENT", "Executes simulated online payment; updates bill status to PAID."),
            ("GET", "/api/bills/{id}/invoice", "RESIDENT", "Downloads official iText PDF tax invoice and payment receipt."),
            ("POST", "/api/resident/occupancy-request", "RESIDENT", "Submits occupancy request for an apartment in an approved building."),
            ("GET", "/api/resident/household-size", "RESIDENT", "Returns current flat occupant count and historical adjustment log."),
            ("PUT", "/api/resident/household-size", "RESIDENT", "Updates flat occupant count (1-15); normalizes ML baseline."),
            ("GET", "/api/resident/away-periods", "RESIDENT", "Returns scheduled vacation away periods."),
            ("POST", "/api/resident/away-periods", "RESIDENT", "Schedules upcoming travel away period (suppresses false alarms)."),
            ("GET", "/api/resident/leak-status", "RESIDENT", "Returns active leak incidents and risk scores for the flat."),
            ("POST", "/api/resident/leak-feedback", "RESIDENT", "Submits resident feedback (Confirmed / False Alarm / Resolved).")
        ]),
        ("Machine Learning Microservice Endpoints (/v1/leak)", [
            ("POST", "/v1/leak/score", "API_KEY (X-API-KEY)", "Ingests batch of flat usage features; returns q10/q50/q95 bounds & risk scores."),
            ("GET", "/health", "Public", "Health check endpoint returning ML microservice operational status & model version.")
        ])
    ]

    for grp_title, endpoints in api_groups:
        add_h2(grp_title)
        ep_rows = []
        for method, path, auth_role, desc in endpoints:
            ep_rows.append([method, path, auth_role, desc])
        add_table_data(["HTTP Method", "Path URL", "Auth / Role", "Functional Operation"], ep_rows, [1.0, 2.0, 1.2, 2.3])

    # =========================================================================
    # CHAPTER 7: FRONTEND APPLICATION ARCHITECTURE
    # =========================================================================
    add_h1("Chapter 7: Frontend Architecture & User Interface Design")
    add_p(
        "AquaFlow's frontend is constructed with React 19 and Vite 6, using Bootstrap 5 and Recharts for responsive, "
        "data-intensive interfaces. It operates on a single-page application (SPA) architecture with client-side route guards."
    )

    add_h2("7.1 Component Hierarchy & Route Layouts")
    add_p("The frontend organizes pages into three segregated layout wrappers based on the user's authenticated role:")
    add_bullet("PublicLayout", "Wraps LandingPage, LoginPage, RegisterOwnerPage, RegisterResidentPage, and AcceptInvitationPage. Provides global branding header and public authentication flows.")
    add_bullet("DashboardLayout", "Wraps authenticated dashboards. Enforces allowedRole authorization checks. Renders responsive TopNavbar, role-specific Sidebar, breadcrumb navigation, and child page routes.")

    add_h2("7.2 State Management & Network Interceptors")
    add_bullet("AuthContext", "Maintains global authentication state (user profile, JWT token, active role). Persists session tokens in browser localStorage and manages instantaneous logout upon token expiration.")
    add_bullet("Axios Interceptor", "Configured in api.js. Automatically attaches Authorization: Bearer <token> to every outbound request. Intercepts HTTP 401 Unauthorized responses to seamlessly redirect users to the login screen.")

    # =========================================================================
    # CHAPTER 8: DEVOPS, TESTING & OPERATIONS MANUAL
    # =========================================================================
    add_h1("Chapter 8: DevOps, Testing & Operations Manual")

    add_h2("8.1 Automated Test Suites")
    add_bullet("Backend JUnit 5 & Mockito Tests", "13+ automated test classes verifying tariff formulas, tenant onboarding validations, and in-JVM statistical fallback failover during network partitions.")
    add_bullet("Python Pytest Suite", "Tests FastAPI endpoint authentication, rolling-window feature transformations, quantile regression bounds, and synthetic edge cases across 10,000 simulated apartment days.")

    add_h2("8.2 Deployment Topology & Port Allocations")
    add_table_data(
        ["Component", "Host / Port", "Technology", "Operational Role"],
        [
            ["Frontend Dev Server", "http://localhost:5173", "React 19 / Vite 6", "User interface for Super Admin, Owner, Resident"],
            ["Backend Core API", "http://localhost:8080/api", "Spring Boot 3.2.5", "Core business logic, security filter chain, and REST APIs"],
            ["Swagger UI", "http://localhost:8080/api/swagger-ui.html", "Springdoc OpenAPI", "Interactive API documentation & schema explorer"],
            ["ML Microservice", "http://127.0.0.1:8000", "FastAPI / Uvicorn", "Asynchronous Quantile Regression leak scoring"],
            ["Database Server", "localhost:3306", "MySQL 8.0 (aquatrac)", "ACID relational store hosting all 16 normalized tables"]
        ],
        [1.8, 1.8, 1.4, 1.5]
    )

    # Save Document
    target_path = r'e:\rep-final 1\AquaFlow_Complete_System_Documentation.docx'
    doc.save(target_path)
    file_size = os.path.getsize(target_path)
    print(f"\nMASTER DOCUMENTATION GENERATED SUCCESSFULLY!")
    print(f"File Location: {target_path}")
    print(f"File Size: {file_size / 1024:.2f} KB ({file_size} bytes)")
    print(f"Under 30 MB Limit? {'YES (PERFECT FOR CLAUDE INGESTION)' if file_size < 30 * 1024 * 1024 else 'NO'}")

if __name__ == '__main__':
    build_complete_system_documentation()
