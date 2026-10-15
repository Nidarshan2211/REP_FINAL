/**
 * Chapter 3 & 4: Security, Identity, Access Control & Relational Database Specification
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

function getChapter3And4() {
  const elements = [];

  // ==========================================
  // CHAPTER 3: SECURITY & IDENTITY ARCHITECTURE
  // ==========================================
  elements.push(h1('Chapter 3: Security & Access Control Architecture'));

  elements.push(h2('3.1 Stateless JWT Authentication Lifecycle'));
  elements.push(p(
    'AquaFlow enforces a zero-session state architecture. The backend server maintains no HTTP session state in memory, enabling effortless horizontal scaling and bulletproof containerized deployment. Every incoming request to protected API routes must supply an Authorization: Bearer <token> HTTP header containing a cryptographically signed JSON Web Token.'
  ));

  elements.push(callout(
    'JWT Token Generation & Verification Protocol',
    '1. Client submits credentials to POST /api/auth/login.\n' +
    '2. AuthenticationManager validates credentials via BCrypt against UserRepository.\n' +
    '3. JwtUtils builds a compact token signed with HMAC-SHA256 using a 256-bit secret key.\n' +
    '4. Token includes Claims: Subject (User Email), Role (SUPER_ADMIN, BUILDING_OWNER, RESIDENT), IssuedAt, and Expiration (24 Hours = 86,400,000 ms).\n' +
    '5. JwtAuthenticationFilter intercepts subsequent requests, parses claims, loads UserDetails, and establishes SecurityContextHolder authentication.\n' +
    '6. If token expires or signature is tampered with, the filter immediately aborts execution and yields HTTP 401 Unauthorized.',
    'info'
  ));

  elements.push(h2('3.2 Role-Based Access Control (RBAC) Matrix'));
  elements.push(p(
    'The system defines three strictly separated actor roles enforced via Spring Security method-level annotations (@PreAuthorize) and URL pattern matchers in SecurityConfig:'
  ));
  elements.push(createTable(
    ['Feature / Endpoint Domain', 'SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT', 'Public / Unauth'],
    [
      ['User Login (/api/auth/login)', 'Allowed', 'Allowed', 'Allowed', 'Allowed'],
      ['Owner Registration (/api/auth/register-owner)', 'Deny', 'Self', 'Deny', 'Allowed'],
      ['Invitation Validation (/api/auth/validate-invitation)', 'Deny', 'Deny', 'Self', 'Allowed with Token'],
      ['Invitation Acceptance (/api/auth/accept-invitation)', 'Deny', 'Deny', 'Self', 'Allowed with Token'],
      ['Global Platform KPIs (/api/super-admin/dashboard/*)', 'Full Access', 'Deny (403)', 'Deny (403)', 'Deny (401)'],
      ['Owner Approval Triage (/api/super-admin/owners/*)', 'Full Access', 'Deny (403)', 'Deny (403)', 'Deny (401)'],
      ['Building & Property Management (/api/owners/buildings/*)', 'Inspect Only', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Apartment & Room Inventory Setup', 'Inspect Only', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Bulk Billing Template & Import (/api/bulk-billing/*)', 'Deny', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Consumption Logs Viewer (/api/usage/records)', 'Inspect Only', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Household Usage Comparison (/api/owners/usage-comparison)', 'Inspect Only', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Billing Cycle & Bill Finalization (/api/owner/billing-cycles/*)', 'Deny', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Water Procurement Ledger (/api/owners/purchases/*)', 'Deny', 'Full Access (Own)', 'Deny (403)', 'Deny (401)'],
      ['Resident Personal Dashboard (/api/resident/dashboard)', 'Deny', 'Deny', 'Own Data Only', 'Deny (401)'],
      ['Resident Consumption History (/api/resident/usage)', 'Deny', 'Deny', 'Own Data Only', 'Deny (401)'],
      ['Resident Bills & Invoices (/api/resident/bills)', 'Deny', 'Deny', 'Own Data Only', 'Deny (401)'],
      ['Bill Payment Simulation (/api/resident/payments)', 'Deny', 'Deny', 'Own Bills Only', 'Deny (401)'],
      ['PDF Receipt Download (/api/bills/{id}/pdf)', 'Deny', 'Own Buildings', 'Own Bills Only', 'Deny (401)'],
      ['Notifications Inbox (/api/notifications/*)', 'Deny', 'Full Access (Own)', 'Full Access (Own)', 'Deny (401)']
    ],
    [30, 18, 18, 18, 16]
  ));

  elements.push(h2('3.3 Cryptographic Tokenized Resident Onboarding Flow'));
  elements.push(p(
    'Residents cannot self-register directly into arbitrary apartments to prevent unauthorized access to tenant billing records and privacy violations. The onboarding follows an asymmetric token verification pipeline:'
  ));
  elements.push(bullet('Owner triggers an invitation specifying Resident Name, Email, Phone, Building, and Apartment Room.', 'Step 1:'));
  elements.push(bullet('TokenGenerator creates a high-entropy 32-byte cryptographically secure random token (rawToken) using SecureRandom and computes its SHA-256 hash (tokenHash).', 'Step 2:'));
  elements.push(bullet('The rawToken is embedded into the activation URL sent via email (http://localhost:5173/accept-invitation?token=...&email=...). Crucially, only the SHA-256 tokenHash is stored in the database with an exact 24-hour expiration timestamp. If the database is compromised, invitations cannot be forged.', 'Step 3:'));
  elements.push(bullet('When the resident clicks the link, the backend computes the SHA-256 hash of the presented token and validates expiration. Once validated, the resident sets their password, their User record is activated, the ApartmentOccupancy is formalized, and the invitation token is permanently revoked.', 'Step 4:'));

  elements.push(h2('3.4 Password Hashing Standard'));
  elements.push(p(
    'All passwords within AquaFlow are hashed using Spring Security\'s BCryptPasswordEncoder with a work factor (strength) of 10 rounds. BCrypt incorporates a random 128-bit salt and adapts its computational cost over time, rendering brute-force and rainbow table attacks computationally infeasible.'
  ));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 4: COMPLETE RELATIONAL DATABASE SCHEMA
  // ==========================================
  elements.push(h1('Chapter 4: Complete Relational Database Specification'));
  elements.push(p(
    'The database schema is organized into 17 strongly-typed relational tables with foreign key cascades, unique constraints, and optimized indexes to support high-speed billing and consumption analytics. The active production database is MySQL 8.0, with full DDL compatibility for PostgreSQL 14+.'
  ));

  // TABLE 1: USERS
  elements.push(h2('4.1 Table: users'));
  elements.push(p('Stores core login identities and credentials for all system roles.'));
  elements.push(createTable(
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
  elements.push(h2('4.2 Table: building_owners'));
  elements.push(p('Profile metadata for building owners, linked 1-to-1 with a User record.'));
  elements.push(createTable(
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
  elements.push(h2('4.3 Table: buildings'));
  elements.push(p('Properties and apartment complexes managed by building owners.'));
  elements.push(createTable(
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
  elements.push(h2('4.4 Table: apartments'));
  elements.push(p('Physical apartment units, flats, and rooms within each floor of a building.'));
  elements.push(createTable(
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
  elements.push(h2('4.5 Table: water_meters'));
  elements.push(p('Sub-meter hardware installed to measure consumption per room or common line.'));
  elements.push(createTable(
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
  elements.push(h2('4.6 Table: residents'));
  elements.push(p('Tenant records and account profiles associated with building flats.'));
  elements.push(createTable(
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

  // TABLE 7: APARTMENT_OCCUPANCIES
  elements.push(h2('4.7 Table: apartment_occupancies'));
  elements.push(p('Historical and active tenancy mappings connecting residents to physical apartments.'));
  elements.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for occupancy tenancy record'],
      ['apartment_id', 'BIGINT (FK)', 'NOT NULL -> apartments.apartment_id', 'Apartment room occupied'],
      ['resident_id', 'BIGINT (FK)', 'NOT NULL -> residents.resident_id', 'Tenant occupying the unit'],
      ['start_date', 'DATE', 'NOT NULL', 'Move-in occupancy commencement date'],
      ['end_date', 'DATE', 'NULLABLE', 'Move-out vacation date (NULL for active tenancies)'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: ACTIVE, VACATED, CANCELLED'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp record was created']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 8: OCCUPANCY_REQUESTS
  elements.push(h2('4.8 Table: occupancy_requests'));
  elements.push(p('Prospective tenant applications or room transfer requests submitted for landlord approval.'));
  elements.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for occupancy request'],
      ['apartment_id', 'BIGINT (FK)', 'NOT NULL -> apartments.apartment_id', 'Target apartment room requested'],
      ['user_id', 'BIGINT (FK)', 'NOT NULL -> users.user_id', 'Applicant user account'],
      ['request_date', 'DATETIME', 'NOT NULL', 'Timestamp request was filed'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, APPROVED, REJECTED, WITHDRAWN'],
      ['notes', 'VARCHAR(255)', 'NULLABLE', 'Optional comments or landlord review notes']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 9: RESIDENT_INVITATIONS
  elements.push(h2('4.9 Table: resident_invitations'));
  elements.push(p('Secure cryptographic token store for inviting prospective residents.'));
  elements.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for invitation record'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Target building complex'],
      ['apartment_id', 'BIGINT (FK)', 'NOT NULL -> apartments.apartment_id', 'Specific flat assigned to invitee'],
      ['email', 'VARCHAR(150)', 'NOT NULL', 'Email address where invitation was dispatched'],
      ['name', 'VARCHAR(100)', 'NOT NULL', 'Full legal name of the invited resident'],
      ['phone', 'VARCHAR(50)', 'NULLABLE', 'Phone number of invited resident'],
      ['token_hash', 'VARCHAR(64)', 'UNIQUE, NOT NULL', 'SHA-256 hash of the 32-byte high-entropy raw activation token'],
      ['status', 'VARCHAR(50)', 'NOT NULL', 'Enum: PENDING, ACCEPTED, EXPIRED, REVOKED'],
      ['expires_at', 'DATETIME', 'NOT NULL', 'Expiration cutoff timestamp (24 hours after creation)'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp invitation was dispatched']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 10: BILLING_CYCLES
  elements.push(h2('4.10 Table: billing_cycles'));
  elements.push(p('Monthly accounting cycles delineating billing periods for a building.'));
  elements.push(createTable(
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

  // TABLE 11: WATER_USAGE
  elements.push(h2('4.11 Table: water_usage'));
  elements.push(p('Meter consumption log records recording initial, final, and consumed units.'));
  elements.push(createTable(
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

  // TABLE 12: TARIFF_PLANS
  elements.push(h2('4.12 Table: tariff_plans'));
  elements.push(p('Tiered pricing structures governing rate calculations per building.'));
  elements.push(createTable(
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

  // TABLE 13: TARIFF
  elements.push(h2('4.13 Table: tariff'));
  elements.push(p('Legacy single-tier or specialized building tariff schedule table.'));
  elements.push(createTable(
    ['Column Name', 'Data Type', 'Constraints', 'Description'],
    [
      ['id', 'BIGINT (PK)', 'AUTO_INCREMENT, NOT NULL', 'Unique primary key for tariff entity'],
      ['building_id', 'BIGINT (FK)', 'NOT NULL -> buildings.building_id', 'Associated building entity'],
      ['rate_per_unit', 'DOUBLE', 'NOT NULL', 'Flat unit consumption rate in INR per kL'],
      ['fixed_charge', 'DOUBLE', 'NOT NULL', 'Base service overhead charge in INR'],
      ['created_at', 'DATETIME', 'NOT NULL', 'Timestamp tariff was established']
    ],
    [20, 20, 25, 35]
  ));

  // TABLE 14: BILLS
  elements.push(h2('4.14 Table: bills'));
  elements.push(p('Individual billing statements issued to residents for consumption cycles.'));
  elements.push(createTable(
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

  // TABLE 15: INVOICES
  elements.push(h2('4.15 Table: invoices'));
  elements.push(p('Comprehensive billing invoice itemizing base consumption and shared amenities.'));
  elements.push(createTable(
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

  // TABLE 16: PAYMENTS
  elements.push(h2('4.16 Table: payments'));
  elements.push(p('Transaction receipts confirming settlement of generated bills.'));
  elements.push(createTable(
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

  // TABLE 17: NOTIFICATIONS
  elements.push(h2('4.17 Table: notifications'));
  elements.push(p('In-app alert inbox messages delivered to residents and owners.'));
  elements.push(createTable(
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

  // TABLE 18: WATER_PURCHASES
  elements.push(h2('4.18 Table: water_purchases'));
  elements.push(p('Bulk external water procurement ledger (private tankers, municipal tankers).'));
  elements.push(createTable(
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

  elements.push(pageBreak());
  return elements;
}

module.exports = { getChapter3And4 };
