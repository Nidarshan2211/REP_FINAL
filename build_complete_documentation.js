/**
 * AquaFlow - Complete System Master Documentation Generator
 * Generates an exhaustive Word document (.docx) detailing every aspect of the project.
 */

const fs = require('fs');
const path = require('path');
const { Document, Packer } = require('docx');
const {
  COLORS,
  pTitle,
  pSubtitle,
  h1,
  h2,
  h3,
  p,
  bullet,
  callout,
  codeBlock,
  createTable,
  pageBreak
} = require('./doc_helpers');

async function generateDocumentation() {
  console.log('Compiling comprehensive system documentation...');

  const children = [];

  // ==========================================
  // TITLE & COVER PAGE
  // ==========================================
  children.push(pTitle('AQUAFLOW SMART WATER MANAGEMENT SYSTEM'));
  children.push(pSubtitle('Complete Architectural Specification, Database Reference, User Functionalities & Operations Manual'));
  
  children.push(p('Document Metadata & Governance:', { bold: true }));
  children.push(createTable(
    ['Property', 'Specification Details'],
    [
      ['Document Title', 'AquaFlow End-to-End Enterprise System Documentation'],
      ['Project Name', 'Smart Water Usage & Consumer Billing System (REP)'],
      ['Target Audience', 'System Architects, Lead Engineers, Product Managers, QA Specialists, Enterprise Clients'],
      ['Backend Platform', 'Spring Boot 3.2.5 (Java 17/25 Target) / Hibernate 6 / Spring Data JPA'],
      ['Frontend Platform', 'React 19.0.0 / Vite 6.4.3 / Bootstrap 5.3.3 / Recharts 3.10.1 / Axios 1.7.2'],
      ['Database Engines', 'MySQL 8.0 (Active) & PostgreSQL 14+ (Flyway Schema Compatibility)'],
      ['Security Framework', 'Spring Security 6 with Stateless JWT (JJWT 0.11.5) & BCrypt Password Hashing'],
      ['Document Version', '2.5.0 (Enterprise Comprehensive Release)'],
      ['Release Status', 'Production Verified / Active Local Dev Deployment'],
      ['Date of Generation', 'October 2026']
    ],
    [30, 70]
  ));

  children.push(p(''));
  children.push(callout(
    'Executive System Overview',
    'AquaFlow is a full-stack, enterprise-grade multi-tenant platform designed to automate residential water management, equitable tiered tariff billing, digital bulk meter data ingestion, tampering detection, automated leak anomaly alerts, and secure resident payment tracking with verified PDF receipt delivery.',
    'info'
  ));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 1: EXECUTIVE SUMMARY & BUSINESS VALUE
  // ==========================================
  children.push(h1('Chapter 1: Executive Summary & Project Purpose'));
  
  children.push(h2('1.1 The Urban Water Crisis in Multi-Unit Communities'));
  children.push(p(
    'In modern residential apartment complexes, gated societies, and commercial communities, water procurement and equitable billing represent one of the most operationally challenging domains. Complexes rarely rely solely on municipal tap lines; they regularly purchase high-cost bulk water through private tanker fleets and operate borewell pumping networks with fluctuating electricity overheads.'
  ));
  children.push(p(
    'Traditional management practices suffer from severe structural handicaps:'
  ));
  children.push(bullet('Equal-Split Inequity: Dividing water procurement expenses equally across apartments penalizes small households, single residents, and conservation-conscious families, while heavily subsidizing high-consumption households.', '1.'));
  children.push(bullet('Delayed Manual Meter Reading: Physical logbooks and manual paper recording lead to clerical calculation errors, disputes between residents and management, and missed billing deadlines.', '2.'));
  children.push(bullet('Undetected Leaks & Water Wastage: Underground pipe fractures, leaking toilet flappers, and internal apartment line ruptures go unnoticed for weeks until astronomical procurement bills arrive or building foundations suffer moisture damage.', '3.'));
  children.push(bullet('Manual Invoice Reconciliation: Building owners spend dozens of hours every month manually matching bank deposits, cash slips, and UPI screenshots against spreadsheets.', '4.'));

  children.push(h2('1.2 The AquaFlow Solution'));
  children.push(p(
    'AquaFlow introduces a fully digitized, closed-loop software ecosystem connecting Building Owners, Property Managers, and Household Residents:'
  ));
  children.push(bullet('Tiered Progressive Tariffs: Incentivizes water conservation by applying increasing slab rates (Tier 1 Base, Tier 2 Moderate, Tier 3 Heavy) so wasteful usage bears fair financial weight.', '•'));
  children.push(bullet('Equitable Shared Water Allocation: Automatically balances community-wide amenities (gardens, clubhouses, swimming pools, security stations) with transparent proportional formulas.', '•'));
  children.push(bullet('Zero-Error Bulk Billing Ingestion: Allows building owners to import building-wide meter logs via CSV with real-time tampering detection, historical reading verification, and immediate rollback safeguards.', '•'));
  children.push(bullet('Statistical Alert Engine: Evaluates household consumption against statistical benchmarks (Mean + 2 Standard Deviations) to instantly flag potential leaks.', '•'));
  children.push(bullet('Automated Digital Invoicing & Instant PDF Receipts: Generates official water consumption bills and digital payment receipts featuring QR-stamp-styled verification and transactional tracking.', '•'));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 2: SYSTEM ARCHITECTURE & TECH STACK
  // ==========================================
  children.push(h1('Chapter 2: Architecture & Technology Stack'));

  children.push(h2('2.1 Architectural Philosophy'));
  children.push(p(
    'AquaFlow is built upon a high-performance decoupled Client-Server architecture. The backend is an enterprise Spring Boot REST micro-monolith providing stateless API services, robust transactional boundaries, and role-based data isolation. The client is a modern Single Page Application (SPA) powered by React 19 and Vite, ensuring instant UI rendering, interactive visualization charts, and zero page flickers.'
  ));

  children.push(h2('2.2 Technology Stack Matrix'));
  children.push(createTable(
    ['Tier / Layer', 'Technology', 'Version', 'Role & Technical Responsibility'],
    [
      ['Backend Core', 'Java', 'JDK 17 (Target) / 25', 'Core typed language for high-throughput concurrency and robust enterprise logic'],
      ['Application Framework', 'Spring Boot', '3.2.5', 'REST MVC controllers, Spring IoC, dependency injection, auto-configuration'],
      ['Security Framework', 'Spring Security', '6.2.4', 'Servlet filter chains, BCrypt hashing, method-level security (@PreAuthorize)'],
      ['JWT Engine', 'JJWT (io.jsonwebtoken)', '0.11.5', 'Stateless token issuance, HMAC-SHA256 signature generation and validation'],
      ['ORM & Persistence', 'Spring Data JPA / Hibernate', '6.4.4', 'Declarative data access, Criteria queries, managed relational mappings'],
      ['Relational Databases', 'MySQL & PostgreSQL', 'MySQL 8.0 / PG 14+', 'Production database with ACID transactions, foreign keys, and indexes'],
      ['Document Engine', 'iText PDF', '5.5.13.3', 'Programmatic PDF rendering of official invoices and stamped payment receipts'],
      ['Email Engine', 'Spring Mail (JavaMailSender)', '3.2.5', 'Automated SMTP email dispatch with MIME multipart support for attachments'],
      ['Frontend Framework', 'React', '19.0.0', 'Modern component-based reactive user interface with hooks and state management'],
      ['Build & Bundler', 'Vite', '6.4.3', 'Ultra-fast HMR dev server and optimized rollup production bundling'],
      ['Client Routing', 'React Router DOM', '7.1.0', 'Client-side SPA route guards, nested layouts, and history navigation'],
      ['UI Design System', 'Bootstrap & Custom CSS', '5.3.3', 'Custom Glassmorphism tokens, responsive grid, badges, and Bootstrap Icons'],
      ['Data Visualization', 'Recharts', '3.10.1', 'SVG-based responsive bar charts for household consumption benchmarking'],
      ['HTTP Client', 'Axios', '1.7.2', 'Asynchronous HTTP communication with JWT auth interceptors and 401 handlers'],
      ['API Documentation', 'Springdoc OpenAPI', '2.5.0', 'Swagger UI and interactive OpenAPI v3 specification (/api/swagger-ui.html)']
    ],
    [20, 25, 15, 40]
  ));

  children.push(h2('2.3 Network Topology & Environment Allocation'));
  children.push(p(
    'The system executes across predictable network ports with integrated proxying to circumvent Cross-Origin Resource Sharing (CORS) complications:'
  ));
  children.push(bullet('Frontend Dev Server: Runs at http://localhost:5173 with Vite hot module replacement.', '•'));
  children.push(bullet('Backend API Server: Runs at http://localhost:8080 with root servlet context path /api.', '•'));
  children.push(bullet('Vite API Proxy: Proxies all frontend calls matching /api/* directly to backend port 8080.', '•'));
  children.push(bullet('Database Server: Connects to local MySQL 8.0 on standard port 3306 (database name: rep).', '•'));

  children.push(h2('2.4 Repository Organization'));
  children.push(codeBlock([
    'p:/REP/',
    '├── backend/                                   # Spring Boot Java Backend',
    '│   ├── pom.xml                                # Maven build descriptor and dependencies',
    '│   └── src/main/java/com/smartwater/billing/  # Main Java Application Sources',
    '│       ├── common/                            # Shared domain entities, DTOs, security, services',
    '│       │   ├── config/                        # SecurityConfig, JwtUtils, CorsConfig, DataInitializer',
    '│       │   ├── controller/                    # AuthController, BillController, BillingCycleController',
    '│       │   ├── dto/                           # UsageDTOs, DashboardDTOs, AuthDTOs',
    '│       │   ├── entity/                        # User, WaterMeter, WaterUsage, BillingCycle, Bill, Invoice',
    '│       │   ├── repository/                    # JPA repositories for core entities',
    '│       │   └── service/                       # EmailService, PdfGeneratorService, AlertEngineService',
    '│       ├── owner/                             # Building Owner sub-domain',
    '│       │   ├── controller/                    # BuildingController, BulkBillingController, WaterUsageController',
    '│       │   ├── entity/                        # Building, Apartment, BuildingOwner',
    '│       │   └── service/                       # BulkBillingService, ApartmentService, TariffCalculationService',
    '│       ├── resident/                          # Resident sub-domain',
    '│       │   ├── controller/                    # ResidentController, OccupancyController',
    '│       │   ├── entity/                        # Resident, ApartmentOccupancy, ResidentInvitation',
    '│       │   └── service/                       # ResidentService, OccupancyService',
    '│       └── superadmin/                        # Super Admin platform governance',
    '│           ├── controller/                    # SuperAdminController',
    '│           └── service/                       # SuperAdminService',
    '└── frontend/                                  # React 19 / Vite Single Page Application',
    '    ├── package.json                           # NPM dependencies and scripts',
    '    ├── vite.config.js                         # Vite dev server and proxy configuration',
    '    └── src/',
    '        ├── auth/                              # LoginPage, RegisterPage, AcceptInvitationPage',
    '        ├── owner/pages/                       # OwnerDashboard, Buildings, UsageLogs, Comparison, BulkBilling',
    '        ├── resident/pages/                    # ResidentDashboard, ResidentBills, ResidentUsage, Payments',
    '        ├── superadmin/pages/                  # SuperAdminDashboard, OwnersApproval, TariffManagement',
    '        └── shared/                            # API service, Navbar, Sidebar, LoadingSpinner, ProtectedRoute'
  ]));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 3: SECURITY & IDENTITY ARCHITECTURE
  // ==========================================
  children.push(h1('Chapter 3: Security & Access Control Architecture'));

  children.push(h2('3.1 Stateless JWT Authentication Lifecycle'));
  children.push(p(
    'AquaFlow enforces zero-session state architecture. The server stores no HTTP session state in memory. Every incoming request must provide an Authorization: Bearer <token> HTTP header containing a cryptographically signed JSON Web Token.'
  ));

  children.push(callout(
    'JWT Token Generation & Verification Protocol',
    '1. Client submits credentials to POST /api/auth/login.\n' +
    '2. AuthenticationManager validates credentials via BCrypt against UserRepository.\n' +
    '3. JwtUtils builds a compact token signed with HMAC-SHA256 using a 256-bit secret key.\n' +
    '4. Token includes Claims: Subject (User Email), Role (SUPER_ADMIN, BUILDING_OWNER, RESIDENT), IssuedAt, and Expiration (24 Hours).\n' +
    '5. JwtAuthenticationFilter intercepts subsequent requests, parses claims, loads UserDetails, and establishes SecurityContextHolder authentication.',
    'info'
  ));

  children.push(h2('3.2 Role-Based Access Control (RBAC) Matrix'));
  children.push(createTable(
    ['Feature / Endpoint Domain', 'SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT', 'Public / Unauth'],
    [
      ['User Login (/api/auth/login)', 'Allowed', 'Allowed', 'Allowed', 'Allowed'],
      ['Owner Registration (/api/auth/register-owner)', 'Deny', 'Self', 'Deny', 'Allowed'],
      ['Invitation Acceptance (/api/auth/accept-invitation)', 'Deny', 'Deny', 'Self', 'Allowed with Token'],
      ['Global Governance (/api/super-admin/*)', 'Full Access', 'Deny (403)', 'Deny (403)', 'Deny (401)'],
      ['Owner Dashboard & Buildings (/api/owners/*)', 'Inspect Only', 'Full Access', 'Deny (403)', 'Deny (401)'],
      ['Apartment & Room Inventory Setup', 'Inspect Only', 'Full Access', 'Deny (403)', 'Deny (401)'],
      ['Bulk Billing Import (/api/bulk-billing/*)', 'Deny', 'Full Access', 'Deny (403)', 'Deny (401)'],
      ['Water Consumption Logs & Comparison', 'Inspect Only', 'Full Access', 'Deny (403)', 'Deny (401)'],
      ['Billing Cycle & Bill Finalization', 'Deny', 'Full Access', 'Deny (403)', 'Deny (401)'],
      ['Resident Personal Dashboard & Bills', 'Deny', 'Deny', 'Own Data Only', 'Deny (401)'],
      ['Bill Payment Simulation & PDF Receipts', 'Deny', 'Deny', 'Own Bills Only', 'Deny (401)']
    ],
    [30, 18, 18, 18, 16]
  ));

  children.push(h2('3.3 Cryptographic Tokenized Resident Invitation Flow'));
  children.push(p(
    'Residents cannot self-register directly into arbitrary apartments to prevent unauthorized access to tenant billing records. The onboarding follows an asymmetric token verification pipeline:'
  ));
  children.push(bullet('Owner triggers an invitation specifying Resident Name, Email, Phone, Building, and Apartment Room.', 'Step 1:'));
  children.push(bullet('TokenGenerator creates a high-entropy 32-byte cryptographically secure random token (rawToken) and computes its SHA-256 hash (tokenHash).', 'Step 2:'));
  children.push(bullet('The rawToken is embedded into the activation link sent via email; only the tokenHash is persisted in the database with an exact 24-hour expiration timestamp.', 'Step 3:'));
  children.push(bullet('When the resident clicks the link, the backend computes SHA-256 of the presented token and validates expiration. Once accepted, the resident enters their password, their User record is activated, and the token is invalidated forever.', 'Step 4:'));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 4: COMPLETE RELATIONAL DATABASE SCHEMA
  // ==========================================
  children.push(h1('Chapter 4: Complete Relational Database Specification'));
  children.push(p(
    'The database schema is organized into 17 strongly-typed relational tables with foreign key cascades, unique constraints, and optimized indexes to support high-speed billing and consumption analytics.'
  ));

  // TABLE 1: USERS
  children.push(h2('4.1 Table: users'));
  children.push(p('Stores core login identities and credentials for all system roles.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['user_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key identifier for the user account'],
      ['email', 'VARCHAR(150)', 'UNIQUE, NOT NULL', 'Unique login email address across the entire platform'],
      ['password', 'VARCHAR(255)', 'NOT NULL', 'BCrypt salted hash of the user password (60 chars)'],
      ['name', 'VARCHAR(100)', 'NOT NULL', 'Full name of the user'],
      ['role', 'VARCHAR(50)', 'NOT NULL', 'Enum: SUPER_ADMIN, BUILDING_OWNER, RESIDENT'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, APPROVED, ACTIVE, REJECTED, SUSPENDED'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Audit timestamp when the user account was registered']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 2: BUILDING_OWNERS
  children.push(h2('4.2 Table: building_owners'));
  children.push(p('Profile metadata for building owners, linked 1-to-1 with a User record.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['owner_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the building owner profile'],
      ['user_id', 'BIGINT (FK)', 'UNIQUE, NOT NULL -> users.user_id', 'One-to-one foreign key mapping to the login user account'],
      ['name', 'VARCHAR(100)', 'NOT NULL', 'Full registered name of the building owner'],
      ['phone', 'VARCHAR(50)', 'NULLABLE', 'Contact phone number of the building owner'],
      ['approval_status', 'VARCHAR(50)', 'NOT NULL', 'Super Admin approval state: PENDING, APPROVED, REJECTED'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Creation timestamp of the owner profile']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 3: BUILDINGS
  children.push(h2('4.3 Table: buildings'));
  children.push(p('Properties and apartment complexes managed by building owners.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['building_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the building entity'],
      ['owner_id', 'BIGINT (FK)', 'NOT NULL -> building_owners.owner_id', 'Foreign key identifying the owning landlord/manager'],
      ['building_name', 'VARCHAR(100)', 'NOT NULL', 'Commercial or society name (e.g., Sukimiya Apartment)'],
      ['building_number', 'VARCHAR(50)', 'NOT NULL', 'Unique block/property code (e.g., BLK-A, EH-04)'],
      ['total_flats', 'INT', 'NOT NULL, DEFAULT 10', 'Declared total household capacity of the building'],
      ['location', 'VARCHAR(255)', 'NULLABLE', 'Physical geographical street address or locality'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp when building record was initialized']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 4: APARTMENTS
  children.push(h2('4.4 Table: apartments'));
  children.push(p('Physical apartment units, flats, and rooms within each floor of a building.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['apartment_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the apartment unit'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Foreign key to the parent building complex'],
      ['floor_number', 'INT', 'NOT NULL', 'Floor number (1, 2, 3, 4, etc.)'],
      ['room_number', 'VARCHAR(50)', 'NOT NULL', 'Room/flat number (e.g., 101, 102, 201, 304)'],
      ['apartment_name', 'VARCHAR(100)', 'NOT NULL', 'Display title (e.g., Flat 101, Penthouse A)'],
      ['unit_type', 'VARCHAR(50)', 'NULLABLE', 'Configuration type: 1BHK, 2BHK, 3BHK, STUDIO, PENTHOUSE'],
      ['description', 'VARCHAR(255)', 'NULLABLE', 'Optional architectural notes or tenant specifications'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Creation timestamp of the apartment unit']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 5: WATER_METERS
  children.push(h2('4.5 Table: water_meters'));
  children.push(p('Sub-meter hardware installed to measure consumption per room or common line.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['meter_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the water meter device'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Parent building where meter is physically installed'],
      ['apartment_id', 'BIGINT (FK)', 'NULLABLE -> apartments.apartment_id', 'Apartment room linked to this meter'],
      ['resident_id', 'BIGINT (FK)', 'NULLABLE -> residents.resident_id', 'Active resident currently responsible for consumption'],
      ['meter_number', 'VARCHAR(50)', 'UNIQUE, NOT NULL', 'Serial number (e.g., WM-BLK-A-101)'],
      ['installation_date', 'DATE', 'NOT NULL', 'Date the meter hardware was deployed'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Operational status: ACTIVE, INACTIVE, FAULTY, REPLACED']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 6: RESIDENTS
  children.push(h2('4.6 Table: residents'));
  children.push(p('Tenant records and account profiles associated with building flats.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['resident_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the resident profile'],
      ['user_id', 'BIGINT (FK)', 'NULLABLE -> users.user_id', 'Associated user login account once activated'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Building complex where resident resides'],
      ['name', 'VARCHAR(100)', 'NOT NULL', 'Full legal name of the resident'],
      ['email', 'VARCHAR(150)', 'NOT NULL', 'Contact email used for invitations and billing'],
      ['phone', 'VARCHAR(50)', 'NULLABLE', 'Contact mobile number'],
      ['apartment_number', 'VARCHAR(50)', 'NOT NULL', 'Flat/room number occupied by resident (e.g., 101)'],
      ['invitation_status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, ACCEPTED, EXPIRED, CANCELLED'],
      ['account_status', 'VARCHAR(50)', 'NOT NULL', 'Enum: ACTIVE, INACTIVE, MOVED_OUT, SUSPENDED'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp resident was invited into system']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 7: BILLING_CYCLES
  children.push(h2('4.7 Table: billing_cycles'));
  children.push(p('Monthly accounting cycles delineating billing periods for a building.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the billing cycle'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Building for which cycle is established'],
      ['start_date', 'DATE', 'NOT NULL', 'First date of the billing consumption window'],
      ['end_date', 'DATE', 'NOT NULL', 'Closing date of the billing consumption window'],
      ['billing_month', 'DATE', 'NOT NULL', 'First of the month representing cycle accounting period'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: OPEN, FINALIZED, CANCELLED'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp cycle was opened by building owner']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 8: WATER_USAGE
  children.push(h2('4.8 Table: water_usage'));
  children.push(p('Meter consumption log records recording initial, final, and consumed units.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['usage_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for usage log entry'],
      ['meter_id', 'BIGINT (FK)', 'NOT NULL -> water_meters.meter_id', 'Meter hardware where readings were captured'],
      ['billing_cycle_id', 'BIGINT (FK)', 'NULLABLE -> billing_cycles.id', 'Accounting billing cycle linked to this reading'],
      ['month', 'INT', 'NOT NULL', 'Calendar month (1 through 12)'],
      ['year', 'INT', 'NOT NULL', 'Calendar year (e.g., 2026)'],
      ['previous_reading', 'DOUBLE', 'NOT NULL', 'Starting cumulative meter reading in cubic meters (m³)'],
      ['current_reading', 'DOUBLE', 'NOT NULL', 'Ending cumulative meter reading in cubic meters (m³)'],
      ['unitsConsumed', 'DOUBLE', 'NOT NULL', 'Net water consumed in kiloliters (kL = m³)'],
      ['anomaly_status', 'VARCHAR(50)', 'NOT NULL', 'Enum: NORMAL, POTENTIAL_LEAK, INSUFFICIENT_HISTORY'],
      ['recorded_at', 'DATETIME', 'NOT NULL', 'Timestamp reading was captured or imported']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 9: TARIFF_PLANS
  children.push(h2('4.9 Table: tariff_plans'));
  children.push(p('Tiered pricing structures governing rate calculations per building.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the tariff plan'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Building to which tariff applies'],
      ['base_limit', 'DOUBLE', 'NOT NULL', 'Tier 1 volume allowance threshold in kL (e.g., 20.0 kL)'],
      ['base_rate', 'DOUBLE', 'NOT NULL', 'Base tariff rate per kL in Tier 1 (e.g., ₹2.50)'],
      ['extra_rate', 'DOUBLE', 'NOT NULL', 'Progressive tariff rate per kL above base limit (e.g., ₹6.00)'],
      ['fixed_charge', 'DOUBLE', 'NOT NULL', 'Mandatory monthly community infrastructure service charge'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: ACTIVE, INACTIVE, DRAFT'],
      ['effective_date', 'DATE', 'NOT NULL', 'Date tariff rates become legally operative']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 10: BILLS
  children.push(h2('4.10 Table: bills'));
  children.push(p('Individual billing statements issued to residents for consumption cycles.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['bill_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the generated bill statement'],
      ['resident_id', 'BIGINT (FK)', 'NOT NULL -> residents.resident_id', 'Billed resident debtor'],
      ['billing_cycle_id', 'BIGINT (FK)', 'NOT NULL -> billing_cycles.id', 'Cycle for which bill was generated'],
      ['usage_id', 'BIGINT (FK)', 'NULLABLE -> water_usage.usage_id', 'Usage log record backing this bill'],
      ['units', 'DOUBLE', 'NOT NULL', 'Total chargeable consumption in kiloliters (kL)'],
      ['amount', 'DOUBLE', 'NOT NULL', 'Total final payable invoice amount in rupees (₹)'],
      ['tier1_units', 'DOUBLE', 'NULLABLE', 'Units billed at Tier 1 rates'],
      ['tier1_rate', 'DOUBLE', 'NULLABLE', 'Applied rate per kL for Tier 1'],
      ['tier1_amount', 'DOUBLE', 'NULLABLE', 'Monetary subtotal for Tier 1'],
      ['tier2_units', 'DOUBLE', 'NULLABLE', 'Units billed at Tier 2 rates'],
      ['tier2_rate', 'DOUBLE', 'NULLABLE', 'Applied rate per kL for Tier 2'],
      ['tier2_amount', 'DOUBLE', 'NULLABLE', 'Monetary subtotal for Tier 2'],
      ['tier3_units', 'DOUBLE', 'NULLABLE', 'Units billed at Tier 3 penalty rate'],
      ['tier3_rate', 'DOUBLE', 'NULLABLE', 'Applied rate per kL for Tier 3'],
      ['tier3_amount', 'DOUBLE', 'NULLABLE', 'Monetary subtotal for Tier 3'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, PAID, OVERDUE, CANCELLED'],
      ['due_date', 'DATE', 'NOT NULL', 'Payment due date before late interest incurs'],
      ['generated_at', 'DATETIME', 'NOT NULL', 'Timestamp bill was computed and locked']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 11: INVOICES
  children.push(h2('4.11 Table: invoices'));
  children.push(p('Comprehensive billing invoice itemizing base consumption and shared amenities.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for the invoice document'],
      ['billing_cycle_id', 'BIGINT (FK)', 'NOT NULL -> billing_cycles.id', 'Billing cycle linked to invoice'],
      ['resident_id', 'BIGINT (FK)', 'NOT NULL -> residents.resident_id', 'Resident charged on invoice'],
      ['usage_units', 'DOUBLE', 'NOT NULL', 'Total metered consumption in kL'],
      ['base_charge', 'DECIMAL(10,2)', 'NOT NULL', 'Fixed standing charge'],
      ['extra_charge', 'DECIMAL(10,2)', 'NOT NULL', 'Volumetric consumption charges across all tiers'],
      ['shared_water_charge', 'DECIMAL(10,2)', 'NOT NULL', 'Proportionate share of common tanker/amenity water'],
      ['total_amount', 'DECIMAL(10,2)', 'NOT NULL', 'Grand total due across all charges'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, PAID, OVERDUE, CANCELLED'],
      ['due_date', 'DATE', 'NOT NULL', 'Payment due date'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp invoice was generated']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 12: PAYMENTS
  children.push(h2('4.12 Table: payments'));
  children.push(p('Transaction receipts confirming settlement of generated bills.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['payment_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for payment transaction'],
      ['bill_id', 'BIGINT (FK)', 'NOT NULL -> bills.bill_id', 'Bill settled by this payment'],
      ['amount', 'DOUBLE', 'NOT NULL', 'Paid monetary value in INR (₹)'],
      ['payment_date', 'DATETIME', 'NOT NULL', 'Timestamp payment was cleared'],
      ['payment_method', 'VARCHAR(50)', 'NOT NULL', 'Payment gateway or channel: RAZORPAY_PAYMENT, UPI, CASH'],
      ['transaction_reference', 'VARCHAR(100)', 'NOT NULL', 'Gateway unique ID (e.g., rzp_pay_7a4b82d91f)'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: SUCCESS, PENDING, FAILED, REFUNDED']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 13: NOTIFICATIONS
  children.push(h2('4.13 Table: notifications'));
  children.push(p('In-app alert inbox messages delivered to residents and owners.'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['notification_id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for notification'],
      ['resident_id', 'BIGINT (FK)', 'NULLABLE -> residents.resident_id', 'Recipient resident'],
      ['title', 'VARCHAR(150)', 'NOT NULL', 'Headline alert summary (e.g., Leak Detected)'],
      ['message', 'TEXT', 'NOT NULL', 'Detailed markdown or text alert body'],
      ['is_read', 'BOOLEAN', 'NOT NULL, DEFAULT FALSE', 'Read/Unread status flag'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp notification was dispatched']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 14: WATER_PURCHASES
  children.push(h2('4.14 Table: water_purchases'));
  children.push(p('Bulk external water procurement ledger (private tankers, municipal tankers).'));
  children.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for purchase entry'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Building receiving the tanker load'],
      ['billing_cycle_id', 'BIGINT (FK)', 'NULLABLE -> billing_cycles.id', 'Cycle in which tanker costs are distributed'],
      ['purchase_date', 'DATE', 'NOT NULL', 'Delivery date of water tanker'],
      ['vendor_name', 'VARCHAR(100)', 'NOT NULL', 'Commercial supplier name (e.g., Cauvery Water Supplies)'],
      ['quantity_liters', 'DOUBLE', 'NOT NULL', 'Total volume delivered in Liters'],
      ['total_cost', 'DOUBLE', 'NOT NULL', 'Total invoice cost paid to vendor in INR (₹)'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp purchase was logged']
    ],
    [20, 20, 25, 35]
  ));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 5: USER FUNCTIONALITIES & WORKFLOWS
  // ==========================================
  children.push(h1('Chapter 5: Role-by-Role User Functionality & End-to-End Workflows'));

  children.push(h2('5.1 Super Admin Workflows'));
  children.push(p(
    'The Super Admin is the global governor of the AquaFlow instance. Access is pre-seeded with no public self-registration.'
  ));
  
  children.push(h3('Workflow 1: Admin Governance & Owner Approvals'));
  children.push(p(
    'When a new property landlord registers via /register-owner, their status is set to PENDING. The Super Admin performs legal verification:'
  ));
  children.push(bullet('Super Admin logs in with administrative credentials at /login.', '1.'));
  children.push(bullet('Navigates to /superadmin/owners where all registered owners appear in filtered tabs: All, Pending, Approved, Rejected.', '2.'));
  children.push(bullet('Inspects owner contact information, phone, business email, and property counts.', '3.'));
  children.push(bullet('Clicks Approve: Updates approval_status to APPROVED and user account to ACTIVE, enabling the owner to log in and manage properties.', '4.'));
  children.push(bullet('Clicks Reject: Sets status to REJECTED with an optional reason banner; the owner is denied login access.', '5.'));

  children.push(h3('Workflow 2: System Metrics & Audit Log Inspection'));
  children.push(p(
    'Super Admin monitors high-level platform health through /superadmin/dashboard:'
  ));
  children.push(bullet('Global KPIs: Total registered owners, total buildings, active households, and total billing revenue processed.', '•'));
  children.push(bullet('Pending Approval Pipeline: Immediate badge showing outstanding owner applications.', '•'));
  children.push(bullet('Audit Log Stream: System activity log tracking security events, role changes, and system errors.', '•'));

  children.push(h2('5.2 Building Owner Workflows'));
  children.push(p(
    'The Building Owner manages properties, apartment inventories, resident accounts, water reading logs, and monthly billing.'
  ));

  children.push(h3('Workflow 3: Property & Apartment Room Inventory Setup'));
  children.push(p(
    'Landlords configure buildings and establish floor-by-floor room inventories:'
  ));
  children.push(bullet('Owner navigates to /owner/buildings and clicks Add New Building.', '1.'));
  children.push(bullet('Enters Property Name (e.g., Sukimiya Apartment), Building Code (BLK-A), Total Flats, and Address.', '2.'));
  children.push(bullet('Navigates into Building Inventory Setup and triggers Floor & Room Generation.', '3.'));
  children.push(bullet('Specifies floors (e.g., Floor 1 to 4) and room numbers per floor (e.g., 101, 102, 103, 104, 201...).', '4.'));
  children.push(bullet('Hardware Auto-Provisioning: The system automatically provisions a dedicated WaterMeter record for each room (e.g., WM-BLK-A-101) with status ACTIVE.', '5.'));

  children.push(h3('Workflow 4: Resident Invitation & Onboarding'));
  children.push(p(
    'To assign tenants to configured rooms:'
  ));
  children.push(bullet('Owner opens /owner/residents and clicks Invite Resident.', '1.'));
  children.push(bullet('Selects the target apartment (e.g., Flat 101) from available unoccupied rooms.', '2.'));
  children.push(bullet('Enters Resident Name, Email Address, and Mobile Phone.', '3.'));
  children.push(bullet('Backend generates cryptographic raw token and sends email activation URL with 24h validity.', '4.'));
  children.push(bullet('The resident record is linked to Flat 101, and the meter WM-BLK-A-101 is mapped to this resident.', '5.'));

  children.push(h3('Workflow 5: Bulk Billing Ingestion (Zero-Error Pipeline)'));
  children.push(p(
    'To record monthly consumption across the entire building in a single operation:'
  ));
  children.push(bullet('Owner opens /owner/bulk-billing and selects the Property and target OPEN Billing Cycle.', '1.'));
  children.push(bullet('Clicks Download CSV Template: Backend dynamically generates a pre-formatted CSV containing: row_number, resident_id, resident_name, apartment_number, meter_number, and the immutable previous_reading from the last cycle.', '2.'));
  children.push(bullet('Owner enters the current_reading in the CSV and uploads the file.', '3.'));
  children.push(bullet('Dry-Run Validation Gate: Backend executes strict multi-point verification: checks numerical formats, verifies previous reading matches database records to detect tampering, ensures current reading >= previous reading, and flags missing apartments.', '4.'));
  children.push(bullet('If validation passes with 0 errors, owner clicks Confirm & Save Readings: The atomic transaction inserts all WaterUsage records and updates meter previous readings.', '5.'));

  children.push(h3('Workflow 6: Water Meter Consumption Logs Inspection'));
  children.push(p(
    'Following customer feedback, the /owner/water-usage page was transformed into a dedicated log inspection viewer:'
  ));
  children.push(bullet('All upload and manual entry modals were removed to eliminate operational confusion.', '•'));
  children.push(bullet('Filter Controls: Top bar provides Property Selector, Apartment Room Selector (e.g., All Apartments, Flat 101, Flat 102...), and Billing Cycle Month Selector (e.g., 2026-11 Cycle #2).', '•'));
  children.push(bullet('Table View: Displays Meter Number, Apartment badge, Resident name, Month/Cycle, Previous Reading, Current Reading, Units Consumed (kL), Leak Status tag, and Recorded Timestamp.', '•'));
  children.push(bullet('Instant Search: Allows searching across meters, flat numbers, and resident names with quick filter reset.', '•'));

  children.push(h3('Workflow 7: Household Usage Comparison & Benchmarking'));
  children.push(p(
    'At /owner/comparison, landlords analyze consumption across all apartments in the complex:'
  ));
  children.push(bullet('Full Inventory Integration: Displays all 16 configured apartments (101 to 405) in Sukimiya Apartment, ensuring vacant/unoccupied flats are visible alongside active households.', '•'));
  children.push(bullet('Cycle Selector: Dropdown allows comparing consumption specifically for Cycle #2 (Nov 2026), Cycle #1 (Oct 2026), or Latest.', '•'));
  children.push(bullet('KPI Metrics: Building Average Consumption per active household, Total Apartments Tracked (16 Apartments, with active vs vacant breakdown), and Count of Above-Average Households.', '•'));
  children.push(bullet('Interactive BarChart: Recharts bar chart colored by consumption state: Slate Blue for high consumers exceeding average, Teal for normal usage, and Slate Gray for vacant/zero usage.', '•'));
  children.push(bullet('Breakdown Table: Itemizes Apartment #, Resident, Meter, Units Consumed, Difference vs Building Average (+/- kL), and Status badge.', '•'));

  children.push(h3('Workflow 8: Billing Cycle Management & Bill Finalization'));
  children.push(p(
    'To conclude an accounting cycle and generate enforceable financial obligations:'
  ));
  children.push(bullet('Owner navigates to /owner/bills and selects the open billing cycle.', '1.'));
  children.push(bullet('Reviews unbilled consumption records and bulk water procurement expenses.', '2.'));
  children.push(bullet('Clicks Calculate Bills: Tariff Calculation Engine computes 3-tier rates, shared amenities allocation, and fixed base charges for every apartment.', '3.'));
  children.push(bullet('Clicks Finalize Cycle: Atomic transaction transitions cycle status from OPEN to FINALIZED, creates official Bill and Invoice records, sets due dates, and locks consumption records against modification.', '4.'));

  children.push(h2('5.3 Resident Workflows'));
  children.push(p(
    'Residents access a personalized self-service portal to monitor consumption, pay bills, and download receipts.'
  ));

  children.push(h3('Workflow 9: Invitation Acceptance & Credential Setup'));
  children.push(bullet('Resident opens activation link from invitation email: http://localhost:5173/accept-invitation?token=...&email=...', '1.'));
  children.push(bullet('System validates token and displays invitation banner: Building Name, Apartment Room, and Owner.', '2.'));
  children.push(bullet('Resident sets a strong password; system activates user account and redirects to login.', '3.'));

  children.push(h3('Workflow 10: Resident Dashboard & Water Analytics'));
  children.push(bullet('Resident logs in and views /resident/dashboard with outstanding bill total, due date, and payment status.', '•'));
  children.push(bullet('Navigates to /resident/usage to review monthly consumption bar charts, historical cubic meter readings, and comparative feedback against building average.', '•'));

  children.push(h3('Workflow 11: Bill Payment & Official PDF Receipt Generation'));
  children.push(bullet('Resident opens /resident/bills, reviews detailed tier breakdown of the current bill, and clicks Pay Now.', '1.'));
  children.push(bullet('Payment Modal executes Razorpay simulation: generates transaction reference (rzp_pay_...) and records payment method.', '2.'));
  children.push(bullet('Payment Processing: Backend transitions Bill and Invoice to PAID, saves Payment entity, and creates resident confirmation notification.', '3.'));
  children.push(bullet('Official PDF Receipt: Resident clicks Download Receipt: PdfGeneratorService dynamically generates a stamped A4 PDF document containing: Digital Payment Stamp, Razorpay Transaction ID, Payment Timestamp, Tier Breakdown, and Consumption Volume.', '4.'));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 6: COMPUTATIONAL ENGINES & FORMULAS
  // ==========================================
  children.push(h1('Chapter 6: Computational Engines & Mathematical Algorithms'));

  children.push(h2('6.1 Progressive 3-Tier Tariff Calculation Engine'));
  children.push(p(
    'AquaFlow enforces progressive conservation-oriented water pricing. The mathematical model divides consumption into three progressive volumetric tiers with increasing unit rates:'
  ));

  children.push(codeBlock([
    'Volumetric Slab Allocation Rules:',
    'Tier 1 (Base Allowance):   0 kL <= Consumption <= BaseLimit (e.g., 20.0 kL)     @ BaseRate (e.g., ₹2.50/kL)',
    'Tier 2 (Moderate Usage):   BaseLimit < Consumption <= BaseLimit + 20.0 kL       @ Tier2Rate (e.g., ₹4.00/kL)',
    'Tier 3 (Excess Penalty):   Consumption > BaseLimit + 20.0 kL (e.g., > 40.0 kL)   @ ExtraRate (e.g., ₹6.00/kL)',
    '',
    'Total Water Bill Formula:',
    'Total Payable = (Tier1_Units * Tier1_Rate) + (Tier2_Units * Tier2_Rate) +',
    '                (Tier3_Units * Tier3_Rate) + FixedBaseCharge + SharedWaterShare'
  ]));

  children.push(h3('Concrete Calculation Walkthrough:'));
  children.push(p(
    'Consider Resident user2 in Apartment 101 with consumption of 245.3 kL, Base Limit of 20.0 kL, Base Rate ₹2.50, Tier 2 Rate ₹4.00, Tier 3 Rate ₹6.00, and Fixed Charge ₹50.00:'
  ));
  children.push(bullet('Tier 1: Min(245.3, 20.0) = 20.0 kL @ ₹2.50 = ₹50.00', '1.'));
  children.push(bullet('Tier 2: Min(225.3, 20.0) = 20.0 kL @ ₹4.00 = ₹80.00', '2.'));
  children.push(bullet('Tier 3: Remaining 205.3 kL @ ₹6.00 = ₹1,231.80', '3.'));
  children.push(bullet('Fixed Infrastructure Charge: ₹50.00', '4.'));
  children.push(bullet('Total Bill Amount: ₹50.00 + ₹80.00 + ₹1,231.80 + ₹50.00 = ₹1,361.80', 'Total:'));

  children.push(h2('6.2 Shared Community Water Distribution Engine'));
  children.push(p(
    'When buildings purchase bulk water tankers to replenish overhead tanks, the total procurement cost is distributed equitably across occupied apartments:'
  ));
  children.push(codeBlock([
    'Shared Water Cost Allocation:',
    'TotalTankerExpenditure = Sum(Purchase.totalCost for billing cycle)',
    'ActiveOccupiedFlats    = Count(Apartments with active residents)',
    '',
    'SharedWaterChargePerFlat = TotalTankerExpenditure / ActiveOccupiedFlats'
  ]));

  children.push(h2('6.3 Statistical Anomaly & Leak Detection Algorithm'));
  children.push(p(
    'The AlertEngineService continuously evaluates meter readings against historical rolling baselines to detect underground pipe bursts and domestic plumbing leaks before bill generation:'
  ));
  children.push(codeBlock([
    'Leak Detection Criterion:',
    'Sample Historical Set: H = [u_1, u_2, ..., u_n] for meter across previous cycles (n >= 2)',
    'Sample Mean (μ)       = Sum(u_i) / n',
    'Standard Deviation (σ)= Sqrt( Sum( (u_i - μ)^2 ) / (n - 1) )',
    'Leak Threshold (T)    = μ + (2 * σ)',
    '',
    'Evaluation:',
    'If n < 2:             Status = INSUFFICIENT_HISTORY',
    'Else If Current > T:   Status = POTENTIAL_LEAK -> Dispatch urgent email & in-app alert',
    'Else:                 Status = NORMAL'
  ]));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 7: COMPLETE REST API REFERENCE
  // ==========================================
  children.push(h1('Chapter 7: Comprehensive REST API Reference'));
  children.push(p(
    'All backend endpoints operate under the servlet context path /api. The complete API surface is documented below:'
  ));

  children.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function'],
    [
      ['POST', '/auth/login', 'Public', '{ email, password }', 'Authenticates user, returns JWT and user role/profile'],
      ['POST', '/auth/register-owner', 'Public', '{ name, email, password, phone }', 'Registers new building owner in PENDING state'],
      ['GET', '/auth/validate-invitation', 'Public', '?token=...&email=...', 'Validates raw invitation token against database hash'],
      ['POST', '/auth/accept-invitation', 'Public', '{ token, email, password }', 'Activates resident account and sets login password'],
      ['GET', '/super-admin/dashboard/metrics', 'SUPER_ADMIN', 'None', 'Returns global platform metrics and KPI counts'],
      ['GET', '/super-admin/owners', 'SUPER_ADMIN', '?status=PENDING', 'Retrieves building owners filtered by approval state'],
      ['POST', '/super-admin/owners/{id}/approve', 'SUPER_ADMIN', 'Path: ownerId', 'Approves owner profile and activates login account'],
      ['POST', '/super-admin/owners/{id}/reject', 'SUPER_ADMIN', 'Path: ownerId', 'Rejects owner application and suspends account'],
      ['GET', '/owners/buildings', 'BUILDING_OWNER', 'None', 'Retrieves all buildings owned by authenticated owner'],
      ['POST', '/owners/buildings', 'BUILDING_OWNER', '{ buildingName, buildingNumber, totalFlats, location }', 'Creates new property building entity'],
      ['GET', '/owners/buildings/{id}/apartments', 'BUILDING_OWNER', 'Path: buildingId', 'Returns full room inventory and floor configuration'],
      ['POST', '/owners/buildings/{id}/apartments', 'BUILDING_OWNER', 'Path: buildingId, { floorNumber, roomNumber, unitType }', 'Creates new apartment and auto-provisions water meter'],
      ['GET', '/owners/residents', 'BUILDING_OWNER', '?query=...&status=...', 'Lists all residents across buildings with occupancy stats'],
      ['POST', '/owners/residents/invite', 'BUILDING_OWNER', '{ buildingId, apartmentId, name, email, phone }', 'Generates tokenized resident invitation'],
      ['POST', '/owners/residents/{id}/vacate', 'BUILDING_OWNER', 'Path: residentId', 'Vacates resident from apartment and frees room'],
      ['GET', '/bulk-billing/template', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Generates pre-filled CSV reading template with previous readings'],
      ['POST', '/bulk-billing/validate', 'BUILDING_OWNER', 'Multipart: file, buildingId, billingCycleId', 'Dry-run validation of uploaded CSV, checks tampering'],
      ['POST', '/bulk-billing/import-readings', 'BUILDING_OWNER', 'Multipart: file, buildingId, billingCycleId', 'Atomic commit of validated meter readings into water_usage'],
      ['GET', '/usage/records', 'BUILDING_OWNER', '?buildingId=...&apartmentNumber=...&billingCycleId=...', 'Filtered consumption logs viewer for property and cycle'],
      ['GET', '/owners/usage-comparison', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Per-apartment consumption benchmark comparison data'],
      ['GET', '/api/owner/billing-cycles', 'BUILDING_OWNER', '?buildingId=...', 'Lists billing cycles for a building'],
      ['POST', '/api/owner/billing-cycles', 'BUILDING_OWNER', '{ buildingId, startDate, endDate, billingMonth }', 'Opens a new billing cycle'],
      ['POST', '/api/owner/billing-cycles/{id}/calculate', 'BUILDING_OWNER', 'Path: cycleId', 'Executes 3-tier bill calculations for all cycle readings'],
      ['POST', '/api/owner/billing-cycles/{id}/finalize', 'BUILDING_OWNER', 'Path: cycleId', 'Finalizes cycle, locks readings, issues bills & invoices'],
      ['GET', '/api/owner/bills', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Retrieves generated bills with resident details'],
      ['GET', '/resident/dashboard', 'RESIDENT', 'None', 'Resident home summary: outstanding balance, recent usage'],
      ['GET', '/resident/usage', 'RESIDENT', 'None', 'Historical monthly consumption trend logs'],
      ['GET', '/resident/bills', 'RESIDENT', 'None', 'List of all billed statements for the logged-in resident'],
      ['POST', '/resident/payments', 'RESIDENT', '{ billId, amount, paymentMethod, transactionReference }', 'Processes bill settlement (Razorpay simulation)'],
      ['GET', '/bills/{id}/pdf', 'RESIDENT / OWNER', 'Path: billId', 'Streams generated iText PDF invoice or payment receipt'],
      ['GET', '/notifications', 'RESIDENT / OWNER', '?unreadOnly=true', 'Retrieves notification alerts inbox'],
      ['PUT', '/notifications/{id}/read', 'RESIDENT / OWNER', 'Path: notificationId', 'Marks notification alert as read']
    ],
    [10, 30, 20, 20, 20]
  ));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 8: FRONTEND DESIGN SYSTEM & UI
  // ==========================================
  children.push(h1('Chapter 8: Frontend Architecture & User Interface Design'));

  children.push(h2('8.1 Design Philosophy & Aesthetics'));
  children.push(p(
    'AquaFlow follows high-end modern SaaS aesthetics. The UI utilizes custom Glassmorphism cards with subtle rgba(255,255,255,0.85) backdrop filters, tailored Slate & Teal color tokens, rounded corners (rounded-3, rounded-4), and fluid micro-animations.'
  ));

  children.push(h2('8.2 Color System Specifications'));
  children.push(createTable(
    ['Color Token Name', 'Hex Value', 'Design Application'],
    [
      ['Primary Navy', '#003554', 'Navbar headers, primary branding text, high-emphasis titles'],
      ['Primary Teal', '#005F73', 'Active navigation links, primary buttons, standard consumption chart bars'],
      ['Slate Blue', '#5A738E', 'Above-average household indicators, comparison chart high bars'],
      ['Accent Cyan', '#0A9396', 'KPI icons, secondary buttons, progress indicators'],
      ['Background Canvas', '#F8FAFC', 'Page backdrop, subtle input backgrounds, table borders'],
      ['Alert Red', '#DC2626', 'Potential leak badges, destructive action buttons, validation error banners'],
      ['Success Green', '#16A34A', 'Paid receipt stamps, normal consumption badges, approved account badges']
    ],
    [25, 20, 55]
  ));

  children.push(h2('8.3 Responsive Navigation & Route Guarding'));
  children.push(p(
    'Client-side security is managed by ProtectedRoute in React Router v7. If an unauthenticated user attempts to access /owner/* or /resident/*, they are immediately redirected to /login with the intended return location preserved in navigation state. If a logged-in resident attempts to access an owner URL, the guard renders an unauthorized access boundary.'
  ));

  children.push(pageBreak());

  // ==========================================
  // CHAPTER 9: DEPLOYMENT & OPERATIONS MANUAL
  // ==========================================
  children.push(h1('Chapter 9: Deployment, Operations & Configuration Manual'));

  children.push(h2('9.1 Configuration Properties (application.yml)'));
  children.push(codeBlock([
    'server:',
    '  port: 8080',
    '  servlet:',
    '    context-path: /api',
    '',
    'spring:',
    '  datasource:',
    '    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/rep?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}',
    '    username: ${SPRING_DATASOURCE_USERNAME:root}',
    '    password: ${SPRING_DATASOURCE_PASSWORD:nobita@18}',
    '  jpa:',
    '    hibernate:',
    '      ddl-auto: update',
    '    show-sql: false',
    '  mail:',
    '    host: ${SPRING_MAIL_HOST:smtp.gmail.com}',
    '    port: ${SPRING_MAIL_PORT:587}',
    '    username: ${SPRING_MAIL_USERNAME:noreply.smartwater@gmail.com}',
    '    password: ${SPRING_MAIL_PASSWORD:secretapppassword}',
    'app:',
    '  jwt:',
    '    secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}',
    '    expiration-ms: 86400000 # 24 Hours',
    '  frontend:',
    '    url: http://localhost:5173'
  ]));

  children.push(h2('9.2 Operational Execution Commands'));
  children.push(p('To compile, test, and run the complete ecosystem in development or production:'));
  children.push(codeBlock([
    '# 1. Start Backend Server (Spring Boot):',
    'cd p:\\REP\\backend',
    'mvn compile -DskipTests',
    'mvn spring-boot:run',
    '',
    '# 2. Build and Start Frontend Server (React / Vite):',
    'cd p:\\REP\\frontend',
    'npm.cmd install',
    'npm.cmd run dev',
    '',
    '# 3. Production Frontend Distribution Build:',
    'cd p:\\REP\\frontend',
    'npm.cmd run build'
  ]));

  children.push(h2('9.3 System Health Checks & Troubleshooting'));
  children.push(createTable(
    ['Symptom / Scenario', 'Root Cause Diagnosis', 'Resolution Procedure'],
    [
      ['CSV Upload Mismatch Error', 'Uploaded CSV contains altered previous_reading not matching database.', 'Download fresh template from /bulk-billing/template and do not alter previous_reading column.'],
      ['403 Forbidden on API Call', 'JWT token has expired or user role lacks required authority.', 'Log out and re-authenticate; check @PreAuthorize role annotations on target controller.'],
      ['Empty Usage Comparison', 'Building has no configured apartments or no readings recorded for cycle.', 'Ensure floors/rooms are created under /owner/buildings and meter readings are imported.'],
      ['Database Connection Refused', 'MySQL 8.0 service is stopped or root password mismatch.', 'Verify MySQL daemon on port 3306 and check SPRING_DATASOURCE_URL and credentials.']
    ],
    [25, 35, 40]
  ));

  // ==========================================
  // DOCUMENT PACKING & GENERATION
  // ==========================================
  const doc = new Document({
    creator: 'AquaFlow System Architecture Team',
    title: 'AquaFlow Smart Water Billing System - Complete System Documentation',
    description: 'Exhaustive architecture, database specification, REST API, computational engines, and user workflows manual.',
    sections: [
      {
        properties: {
          page: {
            margin: {
              top: 1440,    // 1 inch
              bottom: 1440, // 1 inch
              left: 1440,   // 1 inch
              right: 1440   // 1 inch
            }
          }
        },
        children: children
      }
    ]
  });

  const buffer = await Packer.toBuffer(doc);
  const outputPath = path.resolve('p:\\REP', 'AquaFlow_Complete_System_Documentation.docx');
  fs.writeFileSync(outputPath, buffer);

  console.log(`Document successfully generated at: ${outputPath}`);
  console.log(`File Size: ${(buffer.length / 1024).toFixed(2)} KB`);
}

generateDocumentation().catch(err => {
  console.error('Error generating document:', err);
  process.exit(1);
});
