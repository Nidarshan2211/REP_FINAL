/**
 * Chapter 9 & 10: Complete REST API Reference & Frontend Architecture Catalog
 */
const {
  COLORS,
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

function getChapter9And10() {
  const elements = [];

  // ==========================================
  // CHAPTER 9: COMPREHENSIVE REST API REFERENCE
  // ==========================================
  elements.push(h1('Chapter 9: Comprehensive REST API Reference'));
  elements.push(p(
    'All backend endpoints operate under the root servlet context path /api. The complete API surface is documented below with HTTP methods, parameter expectations, access roles, and responses:'
  ));

  elements.push(h2('9.1 Authentication & Onboarding Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['POST', '/auth/login', 'Public', '{ email, password }', 'Authenticates user, returns JWT and user role/profile object.'],
      ['POST', '/auth/register-owner', 'Public', '{ name, email, password, phone }', 'Registers new building owner in PENDING approval state.'],
      ['GET', '/auth/validate-invitation', 'Public', '?token=...&email=...', 'Validates raw invitation token against database hash; returns property info.'],
      ['POST', '/auth/accept-invitation', 'Public', '{ token, email, password }', 'Activates resident user account, sets password, marks invitation accepted.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.2 Super Admin Platform Governance Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/super-admin/dashboard/metrics', 'SUPER_ADMIN', 'None', 'Returns platform-wide metrics: owners, buildings, residents, revenue.'],
      ['GET', '/super-admin/owners', 'SUPER_ADMIN', '?status=PENDING/APPROVED', 'Retrieves building owners filtered by approval state.'],
      ['POST', '/super-admin/owners/{id}/approve', 'SUPER_ADMIN', 'Path: ownerId', 'Approves owner profile and activates login account.'],
      ['POST', '/super-admin/owners/{id}/reject', 'SUPER_ADMIN', 'Path: ownerId', 'Rejects owner application and suspends user account.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.3 Building & Inventory Management Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/owners/buildings', 'BUILDING_OWNER', 'None', 'Retrieves all buildings owned by authenticated landlord.'],
      ['POST', '/owners/buildings', 'BUILDING_OWNER', '{ buildingName, buildingNumber, totalFlats, location }', 'Creates new property building entity.'],
      ['GET', '/owners/buildings/{id}', 'BUILDING_OWNER', 'Path: buildingId', 'Fetches single building metadata with occupancy statistics.'],
      ['GET', '/owners/buildings/{id}/apartments', 'BUILDING_OWNER', 'Path: buildingId', 'Returns full room inventory and floor configuration.'],
      ['POST', '/owners/buildings/{id}/apartments', 'BUILDING_OWNER', 'Path: buildingId, { floorNumber, roomNumber, unitType }', 'Creates new apartment and auto-provisions water meter.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.4 Resident & Tenancy Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/owners/residents', 'BUILDING_OWNER', '?query=...&status=...', 'Lists all residents across buildings with occupancy stats.'],
      ['POST', '/owners/residents/invite', 'BUILDING_OWNER', '{ buildingId, apartmentId, name, email, phone }', 'Generates tokenized resident invitation and sends activation email.'],
      ['POST', '/owners/residents/{id}/vacate', 'BUILDING_OWNER', 'Path: residentId', 'Vacates resident from apartment, frees room, unbinds meter.'],
      ['GET', '/resident/dashboard', 'RESIDENT', 'None', 'Resident home summary: outstanding balance, current usage, due date.'],
      ['GET', '/resident/usage', 'RESIDENT', 'None', 'Historical monthly consumption trend logs and readings.'],
      ['GET', '/resident/bills', 'RESIDENT', 'None', 'List of all billed statements for the logged-in resident.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.5 Bulk Billing & Meter Reading Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/bulk-billing/template', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Generates pre-filled CSV reading template with previous readings.'],
      ['POST', '/bulk-billing/validate', 'BUILDING_OWNER', 'Multipart: file, buildingId, billingCycleId', 'Dry-run validation of uploaded CSV; checks tampering & errors.'],
      ['POST', '/bulk-billing/import-readings', 'BUILDING_OWNER', 'Multipart: file, buildingId, billingCycleId', 'Atomic commit of validated meter readings into water_usage.'],
      ['GET', '/usage/records', 'BUILDING_OWNER', '?buildingId=...&apartmentNumber=...&billingCycleId=...', 'Filtered consumption logs viewer for property and cycle.'],
      ['GET', '/owners/usage-comparison', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Per-apartment consumption benchmark comparison data.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.6 Billing Cycle, Invoices & Payments Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/api/owner/billing-cycles', 'BUILDING_OWNER', '?buildingId=...', 'Lists billing cycles for a building.'],
      ['POST', '/api/owner/billing-cycles', 'BUILDING_OWNER', '{ buildingId, startDate, endDate, billingMonth }', 'Opens a new billing cycle.'],
      ['POST', '/api/owner/billing-cycles/{id}/calculate', 'BUILDING_OWNER', 'Path: cycleId', 'Executes 3-tier bill calculations for all cycle readings.'],
      ['POST', '/api/owner/billing-cycles/{id}/finalize', 'BUILDING_OWNER', 'Path: cycleId', 'Finalizes cycle, locks readings, issues bills & invoices.'],
      ['GET', '/api/owner/bills', 'BUILDING_OWNER', '?buildingId=...&billingCycleId=...', 'Retrieves generated bills with resident details.'],
      ['POST', '/resident/payments', 'RESIDENT', '{ billId, amount, paymentMethod, transactionReference }', 'Processes bill settlement (Razorpay simulation).'],
      ['GET', '/bills/{id}/pdf', 'RESIDENT / OWNER', 'Path: billId', 'Streams generated iText PDF invoice or payment receipt.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(h2('9.7 Notifications & Procurement Endpoints'));
  elements.push(createTable(
    ['HTTP', 'Endpoint Route', 'Auth / Role', 'Parameters & Payload', 'Primary Function & Response'],
    [
      ['GET', '/notifications', 'RESIDENT / OWNER', '?unreadOnly=true', 'Retrieves notification alerts inbox.'],
      ['PUT', '/notifications/{id}/read', 'RESIDENT / OWNER', 'Path: notificationId', 'Marks notification alert as read.'],
      ['GET', '/owners/purchases', 'BUILDING_OWNER', '?buildingId=...', 'Retrieves bulk water tanker purchase ledger.'],
      ['POST', '/owners/purchases', 'BUILDING_OWNER', '{ buildingId, billingCycleId, vendorName, quantityLiters, totalCost, purchaseDate }', 'Logs external water tanker procurement entry.']
    ],
    [10, 30, 20, 20, 20]
  ));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 10: FRONTEND ARCHITECTURE & UI DESIGN
  // ==========================================
  elements.push(h1('Chapter 10: Frontend Architecture & User Interface Design'));

  elements.push(h2('10.1 Frontend Architecture Philosophy'));
  elements.push(p(
    'The AquaFlow client application is architected around React 19 and Vite 6. Key architectural patterns include:'
  ));
  elements.push(bullet('Context-Driven Authentication: AuthContext manages user credentials, JWT decoding, role checks, and automatic logout upon token expiration.', '•'));
  elements.push(bullet('Role-Based Route Guarding: ProtectedRoute enforces granular access control in React Router v7, ensuring unauthorized users cannot access role-restricted views.', '•'));
  elements.push(bullet('Shared Layout Structure: DashboardLayout provides a consistent left sidebar, responsive top navbar, notifications drawer, and page container across all roles.', '•'));
  elements.push(bullet('Modern Aesthetic Tokens: Glassmorphism cards with backdrop blur, custom CSS variables, rounded containers, and modern typography.', '•'));

  elements.push(h2('10.2 Color System Specifications'));
  elements.push(createTable(
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

  elements.push(h2('10.3 Complete Frontend Components & Pages Catalog'));
  elements.push(p('The frontend codebase consists of 34 dedicated components, pages, and layouts:'));
  elements.push(createTable(
    ['Component / Page File', 'Route Path', 'Target Role', 'Key Functional Responsibilities'],
    [
      ['LandingPage.jsx', '/', 'Public', 'Hero landing page, conservation statistics, role-based login shortcuts.'],
      ['LoginPage.jsx', '/login', 'Public', 'Unified login form for all roles with role badge detection and error handling.'],
      ['RegisterOwnerPage.jsx', '/register-owner', 'Public', 'Landlord registration form with company name, phone, and pending approval notice.'],
      ['RegisterResidentPage.jsx', '/register-resident', 'Public', 'Resident self-service onboarding entry point.'],
      ['AcceptInvitationPage.jsx', '/accept-invitation', 'Public / Token', 'Validates 32-byte cryptographic token and accepts password setup.'],
      ['AdminDashboard.jsx', '/superadmin/dashboard', 'SUPER_ADMIN', 'Global platform telemetry, owner count, building count, revenue KPI.'],
      ['AdminOwnersPage.jsx', '/superadmin/owners', 'SUPER_ADMIN', 'Directory of all building owners with search, filter, and profile inspection.'],
      ['AdminPendingOwnersPage.jsx', '/superadmin/pending-owners', 'SUPER_ADMIN', 'Rapid triage approval inbox with one-click Approve / Reject actions.'],
      ['AdminHistoryPage.jsx', '/superadmin/history', 'SUPER_ADMIN', 'Platform-wide security and operational audit logs.'],
      ['OwnerDashboard.jsx', '/owner/dashboard', 'BUILDING_OWNER', 'Live occupancy metrics, current cycle consumption, quick billing links.'],
      ['OwnerBuildingsPage.jsx', '/owner/buildings', 'BUILDING_OWNER', 'Building portfolio management, floor & room generator, auto-meter assignment.'],
      ['OwnerResidentsPage.jsx', '/owner/residents', 'BUILDING_OWNER', 'Resident directory, invite resident modal, vacate tenant action.'],
      ['BulkBillingPage.jsx', '/owner/bulk-billing', 'BUILDING_OWNER', 'CSV template generator, dry-run validator, tamper detector, batch commit.'],
      ['OwnerUsagePage.jsx', '/owner/water-usage', 'BUILDING_OWNER', 'Dedicated meter logs viewer with apartment, cycle, and anomaly filters.'],
      ['OwnerUsageComparisonPage.jsx', '/owner/comparison', 'BUILDING_OWNER', '16-flat benchmark analysis, Cycle #2 / #1 selector, Recharts SVG bar chart.'],
      ['OwnerBillingPage.jsx', '/owner/bills', 'BUILDING_OWNER', 'Billing cycle manager, 3-tier bill calculator, cycle finalization.'],
      ['InvoicesPage.jsx', '/owner/invoices', 'BUILDING_OWNER', 'Itemized invoices table with base charges, tier charges, and shared water.'],
      ['OwnerHistoryPage.jsx', '/owner/history', 'BUILDING_OWNER', 'Historical audit trail of all completed billing cycles and payments.'],
      ['ResidentDashboard.jsx', '/resident/dashboard', 'RESIDENT', 'Outstanding balance card, due date, consumption gauge, water saving tips.'],
      ['ResidentBillsPage.jsx', '/resident/bills', 'RESIDENT', 'Itemized bill viewer, Razorpay payment simulation modal, PDF receipt download.'],
      ['ResidentComparisonPage.jsx', '/resident/comparison', 'RESIDENT', 'Household consumption vs building average comparative benchmark chart.'],
      ['ResidentNotificationsPage.jsx', '/resident/notifications', 'RESIDENT', 'Alerts inbox with leak detection warnings and bill readiness notices.'],
      ['ProfilePage.jsx', '/profile', 'All Authenticated', 'Profile metadata inspection and password modification.'],
      ['Navbar.jsx', 'Global Layout', 'All', 'Top navigation bar with brand logo, notifications bell, user menu, logout.'],
      ['Sidebar.jsx', 'Global Layout', 'All', 'Collapsible left navigation menu with active route indicator and role badges.'],
      ['StatCard.jsx', 'Reusable Component', 'All', 'Standardized KPI display card with title, value, icon, and trend indicator.'],
      ['WaterSavingTips.jsx', 'Reusable Component', 'Resident', 'Rotating conservation guidance box with actionable plumbing advice.'],
      ['LoadingSpinner.jsx', 'Reusable Component', 'All', 'Centered modern loading spinner for asynchronous data fetching.'],
      ['AuthContext.jsx', 'Context Provider', 'All', 'JWT token storage in localStorage, user state, login/logout handlers.'],
      ['DashboardLayout.jsx', 'Route Layout', 'Authenticated', 'Wraps authenticated pages with Sidebar, Navbar, and responsive content canvas.']
    ],
    [25, 20, 15, 40]
  ));

  elements.push(pageBreak());
  return elements;
}

module.exports = { getChapter9And10 };
