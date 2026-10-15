/**
 * Chapter 5 & 6: Super Admin & Building Owner End-to-End Workflows & Capabilities
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

function getChapter5And6() {
  const elements = [];

  // ==========================================
  // CHAPTER 5: SUPER ADMIN CAPABILITIES
  // ==========================================
  elements.push(h1('Chapter 5: Super Admin Capabilities & Platform Governance'));
  elements.push(p(
    'The Super Administrator represents the top-level platform authority within AquaFlow. The Super Admin account is pre-seeded with no public self-registration interface, protecting the integrity of the multi-tenant SaaS architecture.'
  ));

  elements.push(h2('5.1 Super Admin Dashboard Telemetry'));
  elements.push(p(
    'Upon logging into /superadmin/dashboard, the Super Admin is presented with real-time enterprise telemetry widgets:'
  ));
  elements.push(bullet('Total Registered Property Owners: Displays total registered landlords categorized by approval state (Pending, Approved, Rejected).', '•'));
  elements.push(bullet('Active Housing Societies & Buildings: Count of commercial and residential complexes currently deployed on AquaFlow.', '•'));
  elements.push(bullet('Onboarded Household Residents: Real-time count of active residents with confirmed occupancy.', '•'));
  elements.push(bullet('Total Water Billing Revenue Processed: Lifetime monetary transaction volume cleared through the platform (in INR ₹).', '•'));
  elements.push(bullet('Pending Approval Alert Badge: A dynamic pulsing badge indicating the number of landlord registration requests awaiting administrative review.', '•'));

  elements.push(h2('5.2 Building Owner Verification & Approval Pipeline'));
  elements.push(p(
    'When a new property owner registers via /register-owner, their account status is initialized as PENDING and locked. The Super Admin performs legal verification via /superadmin/pending-owners or /superadmin/owners:'
  ));
  elements.push(bullet('Owner Profile Inspection: Admin examines landlord legal name, business contact email, phone number, and declared property portfolio.', 'Step 1:'));
  elements.push(bullet('One-Click Approval: Clicking Approve triggers an atomic transaction setting approval_status = APPROVED in building_owners and status = ACTIVE in users. The landlord immediately receives an email confirmation and can log in.', 'Step 2:'));
  elements.push(bullet('Rejection with Cause: Clicking Reject transitions the account to REJECTED. The landlord is barred from logging in, with a descriptive reason banner presented upon any login attempt.', 'Step 3:'));

  elements.push(h2('5.3 System-Wide Audit History & Compliance Oversight'));
  elements.push(p(
    'At /superadmin/history, the Super Admin inspects an immutable chronologically ordered audit log of all critical system events:'
  ));
  elements.push(bullet('Security Events: Successful and failed login attempts, password updates, and token redemptions.', '•'));
  elements.push(bullet('Financial Milestones: Billing cycle finalizations, tariff modifications, and high-value bulk water purchases.', '•'));
  elements.push(bullet('Tenant Lifecycle Logs: Resident invitations, move-in occupancies, and apartment vacations.', '•'));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 6: BUILDING OWNER WORKFLOWS
  // ==========================================
  elements.push(h1('Chapter 6: Building Owner Capabilities & Operational Workflows'));
  elements.push(p(
    'The Building Owner role governs properties, physical room inventories, tenant assignments, bulk meter data ingestion, consumption analytics, and billing cycles.'
  ));

  elements.push(h2('6.1 Property & Apartment Room Inventory Setup'));
  elements.push(p(
    'Landlords configure physical properties and floor-by-floor room inventories via /owner/buildings:'
  ));
  elements.push(bullet('Creating a Building: Landlord clicks Add Building, specifying Building Name (e.g., Sukimiya Apartment), Building Code (BLK-A), Total Flats (16), and Physical Address.', 'Step 1:'));
  elements.push(bullet('Automated Floor & Room Generation: Landlord enters the Building Inventory screen and launches the Floor Generator. By selecting Floor Range (1 to 4) and Units Per Floor (4 units: 101, 102, 103, 104; 201, 202...), the system batches all apartment records into the database.', 'Step 2:'));
  elements.push(bullet('Automatic Water Meter Hardware Provisioning: For each created apartment, the backend automatically instantiates and binds an active WaterMeter entity with an immutable hardware serial code: WM-<BUILDING_CODE>-<ROOM_NUMBER> (e.g., WM-BLK-A-101, WM-BLK-A-102). This ensures that every room has an assigned meter before any tenant moves in.', 'Step 3:'));

  elements.push(h2('6.2 Resident Lifecycle Management & Room Assignment'));
  elements.push(p(
    'Tenants are onboarded into apartments via /owner/residents:'
  ));
  elements.push(bullet('Resident Directory & Occupancy Counter: Landlords view all occupied and vacant flats, total residents, and invitation statuses (Pending, Accepted, Vacated).', '•'));
  elements.push(bullet('Inviting a Tenant: Landlord clicks Invite Resident, selects an unoccupied apartment (e.g., Flat 101), enters Tenant Name, Email, and Phone. The backend generates a 32-byte cryptographic token and dispatches the activation link.', '•'));
  elements.push(bullet('Vacating a Resident: When a lease terminates, the landlord clicks Vacate Resident. The backend updates ApartmentOccupancy status to VACATED, unbinds the resident from the room and meter, marks the apartment as vacant, and retains historical billing logs intact.', '•'));

  elements.push(h2('6.3 Bulk Billing Ingestion (Zero-Error Pipeline)'));
  elements.push(p(
    'To streamline monthly meter recording across dozens or hundreds of apartments, AquaFlow provides a state-of-the-art Bulk Billing Ingestion pipeline at /owner/bulk-billing:'
  ));

  elements.push(callout(
    'Bulk Billing Zero-Error Architecture & Anti-Tampering Protocol',
    '1. Building & Open Cycle Selection: Landlord selects target property (Sukimiya Apartment) and open billing cycle (e.g., 2026-11 Cycle #2).\n' +
    '2. Dynamic Template Generation: Clicking Download CSV Template triggers backend endpoint /api/bulk-billing/template. The system inspects trusted database records and returns a CSV with immutable columns: row_number, resident_id, resident_name, apartment_number, meter_number, and previous_reading.\n' +
    '3. Meter Reading Entry: The property manager fills only the current_reading column in the CSV.\n' +
    '4. Drag-and-Drop Dry-Run Validation: The file is uploaded to /api/bulk-billing/validate. The backend executes a strict dry-run without writing to the database: verifies CSV syntax, checks that previous_reading exactly matches trusted database values to detect tampering, asserts current_reading >= previous_reading, and flags missing or duplicate apartments.\n' +
    '5. Live Preview & Error Diagnostics: The frontend renders an interactive validation preview table with color-coded badges (Green = Valid, Red = Error). If a single mismatch occurs, descriptive diagnostics pinpoint the exact row and meter number.\n' +
    '6. Atomic Database Commit: When 100% of rows pass validation, the landlord clicks Confirm & Save Readings. The backend wraps the batch in an atomic database transaction: creates WaterUsage records, calculates unitsConsumed (current - previous), updates WaterMeter previous readings, and records audit timestamps.',
    'info'
  ));

  elements.push(h2('6.4 Water Meter Consumption Logs Viewer'));
  elements.push(p(
    'Following customer operational feedback, the /owner/water-usage interface was re-engineered into a dedicated, high-efficiency log inspection viewer. All manual upload and edit modals were eliminated to prevent operational confusion with the Bulk Ingestion pipeline:'
  ));
  elements.push(bullet('Comprehensive Filter Header: Features three synchronized dropdown selectors: Property Selector, Apartment Room Selector (All Apartments, Flat 101, Flat 102...), and Billing Cycle Month Selector (e.g., 2026-11 Cycle #2, 2026-10 Cycle #1).', '•'));
  elements.push(bullet('High-Speed Search Bar: Instant client-side filtering across Meter Serial Numbers, Flat Numbers, and Resident Names with a one-click Reset Filters action.', '•'));
  elements.push(bullet('Itemized Log Table: Displays Meter Number, Apartment badge, Resident name, Month/Cycle, Previous Reading (m³), Current Reading (m³), Net Units Consumed (kL), Anomaly Status tag (NORMAL, POTENTIAL_LEAK), and Recorded Timestamp.', '•'));
  elements.push(bullet('Empty State Guidance: Clear contextual messages when no logs match the selected filter criteria, prompting the user to select an alternate cycle or apartment.', '•'));

  elements.push(h2('6.5 Complex-Wide Household Comparison & Benchmarking'));
  elements.push(p(
    'At /owner/comparison, landlords analyze consumption across all apartments in the complex with complete inventory visibility:'
  ));
  elements.push(bullet('Full 16-Apartment Inventory Visibility: Displays all configured units (Flats 101 through 405 in Sukimiya Apartment), ensuring unoccupied or zero-consumption flats are clearly distinguishable alongside active households.', '•'));
  elements.push(bullet('Historical Cycle Selector: Allows toggling between Cycle #2 (Nov 2026), Cycle #1 (Oct 2026), or Latest available cycle.', '•'));
  elements.push(bullet('KPI Summary Bar: Shows Building Average Consumption per active household, Total Apartments Tracked (16 Flats, highlighting active vs vacant ratio), and Total High-Consumption Units exceeding average.', '•'));
  elements.push(bullet('Interactive Recharts Bar Visualization: SVG bar chart color-coded by usage tier: Slate Blue (#5A738E) for above-average households, Primary Teal (#005F73) for normal households, and Slate Gray for vacant/zero units.', '•'));
  elements.push(bullet('Detailed Breakdown Table: Itemizes Flat Number, Resident Name, Meter Code, Consumption (kL), Difference vs Building Average (+/- kL), and Conservation Status badge.', '•'));

  elements.push(h2('6.6 External Water Procurement Ledger'));
  elements.push(p(
    'At /owner/purchases, landlords log bulk private water tanker procurement to ensure transparent cost recovery:'
  ));
  elements.push(bullet('Logging Tanker Loads: Landlord records Delivery Date, Supplier Name (e.g., Cauvery Water Supplies), Quantity in Liters (e.g., 12,000 L), and Total Invoice Cost in INR (e.g., ₹1,800.00).', '•'));
  elements.push(bullet('Cycle Binding: Each tanker load is bound to the active billing cycle. When bills are generated, the total tanker expenditure is automatically aggregated by the Shared Community Water Distribution Engine and divided equitably across occupied flats.', '•'));

  elements.push(h2('6.7 Billing Cycle Management & Bill Finalization'));
  elements.push(p(
    'At /owner/bills, landlords manage the financial billing cycle and generate official bills:'
  ));
  elements.push(bullet('Opening a Billing Cycle: Landlord defines Cycle Start Date, End Date, and Accounting Billing Month.', '1.'));
  elements.push(bullet('Reviewing Unbilled Usage: Inspects all recorded meter logs to confirm that all occupied apartments have recorded readings.', '2.'));
  elements.push(bullet('Triggering 3-Tier Bill Calculation: Clicking Calculate Bills invokes TariffCalculationService and DistributionService to compute 3-tier rates, shared amenities allocation, and fixed base charges for every flat.', '3.'));
  elements.push(bullet('Finalizing Cycle & Issuing Invoices: Clicking Finalize Cycle locks the billing cycle (status = FINALIZED), generates immutable Bill and Invoice entities, establishes payment due dates, and dispatches email/in-app notifications to residents.', '4.'));

  elements.push(pageBreak());
  return elements;
}

module.exports = { getChapter5And6 };
