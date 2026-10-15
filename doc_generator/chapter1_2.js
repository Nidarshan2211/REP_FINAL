/**
 * Chapter 1 & 2: Executive Summary, System Purpose, Architecture & Tech Stack
 */
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
} = require('../doc_helpers');

function getChapter1And2() {
  const elements = [];

  // ==========================================
  // TITLE & COVER PAGE
  // ==========================================
  elements.push(pTitle('AQUAFLOW SMART WATER MANAGEMENT SYSTEM'));
  elements.push(pSubtitle('Complete Architectural Specification, Database Schema, User Functionalities, Computational Engines & Operations Manual'));
  
  elements.push(p('Document Metadata & Governance Control:', { bold: true }));
  elements.push(createTable(
    ['Property', 'Specification Details'],
    [
      ['Document Title', 'AquaFlow Enterprise Comprehensive System Documentation'],
      ['Project Name', 'Smart Water Usage & Consumer Billing System (REP)'],
      ['Target Audience', 'Enterprise Clients, System Architects, Full-Stack Engineers, DevOps Engineers, QA Specialists, RWA Executives'],
      ['Backend Platform', 'Spring Boot 3.2.5 (Java 17/25 Target) / Hibernate 6.4.4 / Spring Data JPA / Spring Security 6'],
      ['Frontend Platform', 'React 19.0.0 / Vite 6.4.3 / Bootstrap 5.3.3 / Recharts 3.10.1 / Axios 1.7.2'],
      ['Database Engines', 'MySQL 8.0 (Active Production-Grade) & PostgreSQL 14+ (Compatible Entity Schema)'],
      ['Security Framework', 'Spring Security 6 with Stateless JWT (JJWT 0.11.5) & BCrypt 10-Round Password Hashing'],
      ['Reporting & Output', 'iText PDF 5.5.13.3 (Receipts & Invoices) / JavaMailSender (Email Notifications)'],
      ['Document Version', '2.5.0 (Final Enterprise Master Release)'],
      ['Release Status', 'Production Verified / Active Local Development & Staging Deployment'],
      ['Date of Generation', 'October 2026'],
      ['Repository Root', 'p:\\REP (Unified Monorepo: /backend and /frontend)']
    ],
    [30, 70]
  ));

  elements.push(p(''));
  elements.push(callout(
    'Executive System Overview',
    'AquaFlow is a full-stack, enterprise-grade multi-tenant platform designed to automate residential water management, equitable tiered tariff billing, digital bulk meter data ingestion, tampering detection, automated leak anomaly alerts, and secure resident payment tracking with verified PDF receipt delivery.',
    'info'
  ));

  elements.push(p(''));
  elements.push(p('Document Revision & Change Control History:', { bold: true }));
  elements.push(createTable(
    ['Version', 'Release Date', 'Primary Authors', 'Key Architectural Milestones & Functional Changes'],
    [
      ['v1.0.0', 'Jan 2026', 'Core Engineering Team', 'Initial MVP: Spring Boot REST API, basic User entities, single-rate manual billing.'],
      ['v1.5.0', 'Apr 2026', 'Platform Architects', 'Multi-tenant hierarchy: Super Admin, Building Owner, and Resident roles. Multi-building management.'],
      ['v2.0.0', 'Jul 2026', 'Algorithm Team', 'Progressive 3-Tier Tariff calculation engine, rolling statistical leak anomaly detection.'],
      ['v2.2.0', 'Aug 2026', 'Security Team', 'Asymmetric tokenized resident invitation flow (32-byte raw token, SHA-256 hash storage).'],
      ['v2.4.0', 'Sep 2026', 'Frontend & Billing', 'Zero-Error Bulk Billing Ingestion pipeline, dry-run tampering validation, dedicated logs viewer.'],
      ['v2.5.0', 'Oct 2026', 'Enterprise Core', 'Automated PDF receipt generation with QR-stamped verification and Spring Mail transactional delivery.']
    ],
    [12, 15, 23, 50]
  ));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 1: EXECUTIVE SUMMARY & PROJECT PURPOSE
  // ==========================================
  elements.push(h1('Chapter 1: Executive Summary & Project Purpose'));
  
  elements.push(h2('1.1 The Urban Water Crisis in Multi-Unit Communities'));
  elements.push(p(
    'In rapidly expanding metropolitan centers, residential apartment complexes, gated housing societies, and multi-tenant commercial properties face acute operational challenges surrounding water procurement, distribution, and fair financial accounting. Municipal water supplies are frequently intermittent, forcing residential welfare associations (RWAs) and property owners to rely heavily on expensive private water tankers and borewell pumping networks with fluctuating electricity overheads.'
  ));
  elements.push(p(
    'Traditional property management practices rely on archaic, manual procedures that create chronic friction between residents and landlords:'
  ));
  elements.push(bullet('Equal-Split Inequity: Dividing water procurement expenses equally across flats penalizes single occupants, small families, and conservation-conscious households, while heavily subsidizing high-consumption households and incentivizing waste.', '1.'));
  elements.push(bullet('Clerical Errors & Billing Disputes: Manual logbook recording of mechanical meters leads to clerical calculation errors, illegible entries, missed billing deadlines, and protracted disputes between residents and property managers.', '2.'));
  elements.push(bullet('Catastrophic Undetected Leaks: Underground line fractures, running toilet cisterns, and leaking internal fixtures go unnoticed for weeks until astronomical municipal or tanker bills arrive, or building foundations suffer permanent moisture damage.', '3.'));
  elements.push(bullet('Lack of Payment Tracking: Landlords spend dozens of hours each month manually cross-referencing paper receipts, bank deposit slips, and UPI screenshots against spreadsheets.', '4.'));
  elements.push(bullet('Opacity in Shared Resource Costs: Common amenities (gardens, clubhouses, swimming pools, car washing areas) lack transparent allocation models, leading to resident distrust and delayed maintenance fee collections.', '5.'));

  elements.push(h2('1.2 The AquaFlow Solution'));
  elements.push(p(
    'AquaFlow introduces a fully digitized, closed-loop software ecosystem connecting Building Owners, Property Managers, and Household Residents:'
  ));
  elements.push(bullet('Tiered Progressive Tariffs: Incentivizes water conservation by applying increasing slab rates (Tier 1 Base, Tier 2 Moderate, Tier 3 Heavy) so wasteful usage bears fair financial weight.', '•'));
  elements.push(bullet('Equitable Shared Water Allocation: Automatically balances community-wide amenities (gardens, clubhouses, swimming pools, security stations) with transparent proportional formulas.', '•'));
  elements.push(bullet('Zero-Error Bulk Billing Ingestion: Allows building owners to import building-wide meter logs via CSV with real-time tampering detection, historical reading verification, and immediate rollback safeguards.', '•'));
  elements.push(bullet('Statistical Alert Engine: Evaluates household consumption against statistical benchmarks (Mean + 2 Standard Deviations) to instantly flag potential leaks.', '•'));
  elements.push(bullet('Automated Digital Invoicing & Instant PDF Receipts: Generates official water consumption bills and digital payment receipts featuring QR-stamp-styled verification and transactional tracking.', '•'));
  elements.push(bullet('Multi-Role Governance: Cleanly partitions responsibilities across Super Administrators (platform oversight), Building Owners (property, inventory, and cycle administration), and Residents (self-service analytics and payments).', '•'));

  elements.push(h2('1.3 Strategic Business Value & ROI'));
  elements.push(createTable(
    ['Stakeholder Role', 'Operational Challenges Before AquaFlow', 'Transformative Outcomes With AquaFlow'],
    [
      ['Building Owners / RWAs', 'Manual meter reading errors, uncollected bills, resident disputes, untracked tanker expenses.', '100% automated billing calculations, digital audit trail, zero-tampering CSV ingestion, 40% reduction in billing cycle turnaround.'],
      ['Residents / Tenants', 'Unfair equal billing, no visibility into consumption patterns, lack of proof for payments made.', 'Transparent itemized invoices, real-time consumption benchmarks, automated PDF payment receipts, instant leak warnings.'],
      ['Society Facility Teams', 'Unnoticed plumbing leaks leading to foundation dampness and tank overflows.', 'Automated anomaly detection triggers instant alerts before structural damage or excessive tanker costs occur.'],
      ['Super Administrators', 'No central visibility into multi-property compliance or platform monetization.', 'Single-pane-of-glass platform telemetry, automated owner approvals, comprehensive system audit logs.']
    ],
    [22, 38, 40]
  ));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 2: SYSTEM ARCHITECTURE & TECH STACK
  // ==========================================
  elements.push(h1('Chapter 2: Architecture & Technology Stack'));

  elements.push(h2('2.1 Architectural Philosophy'));
  elements.push(p(
    'AquaFlow is built upon a high-performance decoupled Client-Server architecture. The backend is an enterprise Spring Boot REST micro-monolith providing stateless API services, robust transactional boundaries, and role-based data isolation. The client is a modern Single Page Application (SPA) powered by React 19 and Vite, ensuring instant UI rendering, interactive visualization charts, and zero page flickers.'
  ));

  elements.push(h2('2.2 Technology Stack Matrix'));
  elements.push(createTable(
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

  elements.push(h2('2.3 Network Topology & Environment Allocation'));
  elements.push(p(
    'The system executes across predictable network ports with integrated proxying to circumvent Cross-Origin Resource Sharing (CORS) complications:'
  ));
  elements.push(bullet('Frontend Dev Server: Runs at http://localhost:5173 with Vite hot module replacement.', '•'));
  elements.push(bullet('Backend API Server: Runs at http://localhost:8080 with root servlet context path /api.', '•'));
  elements.push(bullet('Vite API Proxy: Proxies all frontend calls matching /api/* directly to backend port 8080.', '•'));
  elements.push(bullet('Database Server: Connects to local MySQL 8.0 on standard port 3306 (database name: rep).', '•'));

  elements.push(h2('2.4 Repository Organization'));
  elements.push(codeBlock([
    'p:/REP/',
    '├── backend/                                   # Spring Boot Java Backend',
    '│   ├── pom.xml                                # Maven build descriptor and dependencies',
    '│   └── src/main/java/com/smartwater/billing/  # Main Java Application Sources',
    '│       ├── common/                            # Shared domain entities, DTOs, security, services',
    '│       │   ├── config/                        # SecurityConfig, JwtUtils, CorsConfig, DataInitializer',
    '│       │   ├── controller/                    # AuthController, BillController, BillingCycleController, UserController',
    '│       │   ├── dto/                           # UsageDTOs, DashboardDTOs, AuthDTOs, BillingDTOs, UserDTOs',
    '│       │   ├── entity/                        # User, WaterMeter, WaterUsage, BillingCycle, Bill, Invoice, Payment',
    '│       │   ├── exception/                     # GlobalExceptionHandler, BadRequestException, ResourceNotFoundException',
    '│       │   ├── repository/                    # JPA repositories for core entities',
    '│       │   ├── scheduler/                     # AlertScheduler (automated cron background tasks)',
    '│       │   ├── service/                       # EmailService, PdfGeneratorService, AlertEngineService, BillingService',
    '│       │   └── util/                          # CsvHelper, TokenGenerator',
    '│       ├── owner/                             # Building Owner sub-domain',
    '│       │   ├── controller/                    # BuildingOwnerController, ApartmentController, BulkBillingController, WaterUsageController, WaterPurchaseController, TariffController',
    '│       │   ├── dto/                           # ApartmentDTOs, BuildingDTOs, BulkBillingDTOs, BillingEngineDTOs',
    '│       │   ├── entity/                        # Building, Apartment, BuildingOwner, Tariff, TariffPlan, WaterPurchase',
    '│       │   ├── repository/                    # ApartmentRepository, BuildingRepository, TariffPlanRepository, WaterPurchaseRepository',
    '│       │   └── service/                       # BulkBillingService, ApartmentService, TariffCalculationService, WaterPurchaseCostService, DistributionService, TariffRecommendationService',
    '│       ├── resident/                          # Resident sub-domain',
    '│       │   ├── controller/                    # ResidentController, OccupancyController, NotificationController',
    '│       │   ├── dto/                           # ResidentDTOs, OccupancyDTOs, HouseholdDTOs',
    '│       │   ├── entity/                        # Resident, ApartmentOccupancy, ResidentInvitation, OccupancyRequest, Notification',
    '│       │   ├── repository/                    # ResidentRepository, ApartmentOccupancyRepository, ResidentInvitationRepository, NotificationRepository',
    '│       │   └── service/                       # ResidentService, OccupancyService',
    '│       └── superadmin/                        # Super Admin platform governance',
    '│           ├── controller/                    # SuperAdminController',
    '│           └── service/                       # SuperAdminService',
    '└── frontend/                                  # React 19 / Vite Single Page Application',
    '    ├── package.json                           # NPM dependencies and scripts',
    '    ├── vite.config.js                         # Vite dev server and proxy configuration',
    '    └── src/',
    '        ├── auth/                              # LoginPage, RegisterOwnerPage, RegisterResidentPage, AcceptInvitationPage',
    '        ├── owner/pages/                       # OwnerDashboard, OwnerBuildingsPage, OwnerResidentsPage, OwnerUsagePage, OwnerUsageComparisonPage, BulkBillingPage, OwnerBillingPage, InvoicesPage, OwnerHistoryPage',
    '        ├── resident/pages/                    # ResidentDashboard, ResidentBillsPage, ResidentComparisonPage, ResidentNotificationsPage',
    '        ├── superadmin/pages/                  # AdminDashboard, AdminOwnersPage, AdminPendingOwnersPage, AdminHistoryPage',
    '        ├── shared/components/                 # Navbar, Sidebar, StatCard, WaterSavingTips, LoadingSpinner',
    '        ├── shared/context/                    # AuthContext (JWT management, role validation, login/logout)',
    '        └── shared/layouts/                    # DashboardLayout, PublicLayout, ProtectedRoute'
  ]));

  elements.push(pageBreak());
  return elements;
}

module.exports = { getChapter1And2 };
