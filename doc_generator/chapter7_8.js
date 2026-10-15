/**
 * Chapter 7 & 8: Resident Workflows, Computational Engines & Mathematical Algorithms
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

function getChapter7And8() {
  const elements = [];

  // ==========================================
  // CHAPTER 7: RESIDENT SELF-SERVICE WORKFLOWS
  // ==========================================
  elements.push(h1('Chapter 7: Resident Self-Service Portal & User Workflows'));
  elements.push(p(
    'Residents interact with AquaFlow through a personalized self-service portal designed for maximum transparency, friction-free bill payment, and proactive water conservation.'
  ));

  elements.push(h2('7.1 Token-Based Account Activation & Onboarding'));
  elements.push(p(
    'When a resident receives an invitation email from their property owner, the onboarding workflow proceeds as follows:'
  ));
  elements.push(bullet('Activation Link Navigation: Resident clicks the link in their email: http://localhost:5173/accept-invitation?token=...&email=...', '1.'));
  elements.push(bullet('Asymmetric Token Verification: Frontend makes a GET request to /api/auth/validate-invitation. Backend computes SHA-256 of the token, checks the resident_invitations table, confirms status is PENDING, and verifies that the 24-hour expiration window has not elapsed.', '2.'));
  elements.push(bullet('Invitation Context Display: Frontend displays a welcoming card showing Property Name (Sukimiya Apartment), Assigned Apartment (Flat 101), and Registered Landlord.', '3.'));
  elements.push(bullet('Password Configuration: Resident enters their new password with confirmation and password strength validation.', '4.'));
  elements.push(bullet('Account Activation & Redirect: Submitting POST /api/auth/accept-invitation creates/activates the User record with BCrypt-hashed password, updates invitation status to ACCEPTED, binds the ApartmentOccupancy record, and redirects the resident to the login portal with a success banner.', '5.'));

  elements.push(h2('7.2 Resident Dashboard Telemetry & Consumption Gauge'));
  elements.push(p(
    'Upon logging into /resident/dashboard, residents view immediate telemetry regarding their water usage and financial obligations:'
  ));
  elements.push(bullet('Current Outstanding Balance: Bold visual card showing total unpaid bill amount, payment due date, and days remaining before late fee penalties.', '•'));
  elements.push(bullet('Current Cycle Consumption: Real-time gauge showing kiloliters (kL) consumed in the active cycle compared to Tier 1 base allowance.', '•'));
  elements.push(bullet('Recent Payments Widget: Quick summary of the most recently settled invoice with payment date and transaction reference.', '•'));
  elements.push(bullet('Curated Water Saving Tips: Contextual conservation guidance (e.g., fixing faucet aerators, dual-flush toilet tips, identifying hidden flapper leaks) to assist households in staying within Tier 1 base rates.', '•'));

  elements.push(h2('7.3 Historical Consumption Trends & Community Benchmarking'));
  elements.push(p(
    'At /resident/usage and /resident/comparison, residents inspect detailed historical analytics:'
  ));
  elements.push(bullet('Monthly Consumption History: Interactive Recharts bar chart displaying consumption in kL across the last 6 to 12 billing cycles.', '•'));
  elements.push(bullet('Community Benchmarking: Compares household usage directly against the building complex average. Residents see whether they consume below, equal to, or above their neighbors, fostering healthy community-wide conservation habits.', '•'));
  elements.push(bullet('Reading Audit Log: Itemized table of all previous meter readings (Starting m³, Ending m³, Consumed kL, Recorded Date) for complete billing verification.', '•'));

  elements.push(h2('7.4 Electronic Bill Settlement (Razorpay Simulation)'));
  elements.push(p(
    'At /resident/bills, residents review itemized bills and settle payments seamlessly:'
  ));
  elements.push(bullet('Itemized Bill Inspection: Clicking View Details opens a modal breaking down the exact calculation: Tier 1 Units & Rate, Tier 2 Units & Rate, Tier 3 Units & Rate, Fixed Infrastructure Charge, and Shared Water Allocation.', '1.'));
  elements.push(bullet('Payment Modal Launch: Clicking Pay Now opens the integrated Razorpay checkout simulation modal.', '2.'));
  elements.push(bullet('Payment Method Selection: Resident selects payment channel (UPI, Credit/Debit Card, Net Banking) and confirms payment of ₹1,361.80.', '3.'));
  elements.push(bullet('Transaction Processing: Client posts to /api/resident/payments with billId, amount, paymentMethod, and transactionReference (e.g., rzp_pay_7a4b82d91f).', '4.'));
  elements.push(bullet('Atomic Settlement: Backend validates amount against the bill, updates Bill and Invoice status to PAID, inserts a Payment record, triggers an automated confirmation notification, and dispatches an email receipt.', '5.'));

  elements.push(h2('7.5 Official Stamped PDF Receipts'));
  elements.push(p(
    'Once a bill is paid, residents can immediately generate and download an official payment receipt via /api/bills/{id}/pdf:'
  ));
  elements.push(bullet('Automated iText PDF Generation: PdfGeneratorService dynamically constructs a high-resolution A4 document on the fly.', '•'));
  elements.push(bullet('Official Metadata: Contains AquaFlow branding, Society Name, Resident Name, Flat Number, Meter Serial Code, Billing Cycle Dates, and Invoice Due Date.', '•'));
  elements.push(bullet('Itemized Financial Table: Details exact volumetric consumption, Tier 1/2/3 breakdown, fixed charges, and shared water allocation.', '•'));
  elements.push(bullet('Digital Verification Stamp: Prominently renders a colored "PAID & VERIFIED" stamp with Razorpay Transaction Reference ID, Payment Timestamp, and Cryptographic Verification Hash for tamper-proof auditing.', '•'));

  elements.push(h2('7.6 In-App Notification Center'));
  elements.push(p(
    'At /resident/notifications, residents receive immediate alerts concerning account and property events:'
  ));
  elements.push(bullet('Leak Warning Alerts: Critical alerts dispatched by the AlertEngineService when unusual consumption spikes are detected.', '•'));
  elements.push(bullet('New Bill Issued: Announcements when a billing cycle is finalized and a new invoice is ready for payment.', '•'));
  elements.push(bullet('Payment Success Confirmations: Delivery of transaction reference confirmations upon successful bill settlement.', '•'));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 8: COMPUTATIONAL ENGINES & ALGORITHMS
  // ==========================================
  elements.push(h1('Chapter 8: Computational Engines & Mathematical Algorithms'));
  elements.push(p(
    'AquaFlow is powered by four dedicated mathematical engines that automate complex utility billing, cost distribution, and anomaly detection.'
  ));

  elements.push(h2('8.1 Progressive 3-Tier Tariff Calculation Engine'));
  elements.push(p(
    'The TariffCalculationService enforces progressive, conservation-oriented water pricing. The mathematical model divides metered consumption into three progressive volumetric tiers with escalating unit rates:'
  ));

  elements.push(codeBlock([
    'Volumetric Slab Allocation Rules:',
    'Tier 1 (Base Allowance):   0 kL <= Consumption <= BaseLimit (e.g., 20.0 kL)     @ BaseRate (e.g., ₹2.50/kL)',
    'Tier 2 (Moderate Usage):   BaseLimit < Consumption <= BaseLimit + 20.0 kL       @ Tier2Rate (e.g., ₹4.00/kL)',
    'Tier 3 (Excess Penalty):   Consumption > BaseLimit + 20.0 kL (e.g., > 40.0 kL)   @ ExtraRate (e.g., ₹6.00/kL)',
    '',
    'Total Water Bill Formula:',
    'Total Payable = (Tier1_Units * Tier1_Rate) + (Tier2_Units * Tier2_Rate) +',
    '                (Tier3_Units * Tier3_Rate) + FixedBaseCharge + SharedWaterShare'
  ]));

  elements.push(h3('Concrete Numeric Walkthrough:'));
  elements.push(p(
    'Consider Resident user2 in Apartment 101 with consumption of 245.3 kL, Base Limit of 20.0 kL, Base Rate ₹2.50, Tier 2 Rate ₹4.00, Tier 3 Rate ₹6.00, and Fixed Charge ₹50.00:'
  ));
  elements.push(bullet('Tier 1 Allocation: Min(245.3, 20.0) = 20.0 kL billed @ ₹2.50 = ₹50.00', '1.'));
  elements.push(bullet('Tier 2 Allocation: Min(245.3 - 20.0, 20.0) = Min(225.3, 20.0) = 20.0 kL billed @ ₹4.00 = ₹80.00', '2.'));
  elements.push(bullet('Tier 3 Allocation: Remaining 245.3 - 40.0 = 205.3 kL billed @ penalty rate ₹6.00 = ₹1,231.80', '3.'));
  elements.push(bullet('Fixed Infrastructure Charge: Standing monthly charge = ₹50.00', '4.'));
  elements.push(bullet('Grand Total Bill Amount: ₹50.00 + ₹80.00 + ₹1,231.80 + ₹50.00 = ₹1,361.80', 'Total:'));

  elements.push(h2('8.2 Shared Community Water Distribution Engine'));
  elements.push(p(
    'When buildings purchase external water tankers to replenish overhead community reservoirs, the DistributionService distributes the total expenditure equitably across active occupied apartments:'
  ));
  elements.push(codeBlock([
    'Shared Water Cost Distribution Formula:',
    'TotalTankerExpenditure = Sum(Purchase.totalCost for billing cycle)',
    'ActiveOccupiedFlats    = Count(Apartments where occupancy.status == ACTIVE)',
    '',
    'If ActiveOccupiedFlats > 0:',
    '    SharedWaterChargePerFlat = TotalTankerExpenditure / ActiveOccupiedFlats',
    'Else:',
    '    SharedWaterChargePerFlat = 0.00'
  ]));
  elements.push(p(
    'Edge Case Handling: Vacant or unoccupied apartments are excluded from the active denominator, ensuring that moving tenants are not burdened with shared charges and active residents absorb only true active community splits.'
  ));

  elements.push(h2('8.3 Statistical Anomaly & Leak Detection Algorithm'));
  elements.push(p(
    'The AlertEngineService continuously evaluates meter readings against historical rolling baselines to detect underground pipe bursts and domestic plumbing leaks before bill generation:'
  ));
  elements.push(codeBlock([
    'Statistical Leak Detection Model:',
    'Sample Historical Set: H = [u_1, u_2, ..., u_n] for meter across previous cycles (n >= 2)',
    'Sample Mean (μ)       = Sum(u_i) / n',
    'Standard Deviation (σ)= Sqrt( Sum( (u_i - μ)^2 ) / (n - 1) )',
    'Leak Threshold (T)    = μ + (2 * σ)',
    '',
    'Classification Logic:',
    'If n < 2:             anomaly_status = INSUFFICIENT_HISTORY',
    'Else If Current > T:   anomaly_status = POTENTIAL_LEAK -> Dispatch urgent email & in-app alert',
    'Else:                 anomaly_status = NORMAL'
  ]));

  elements.push(h2('8.4 Tariff Heuristic Recommendation Engine'));
  elements.push(p(
    'The TariffRecommendationService analyzes aggregate consumption curves across all apartments in a building to recommend optimized tariff thresholds (Base Limit and Extra Rate). If more than 35% of households routinely breach the base limit, the engine recommends adjusting the base limit to reflect realistic conservation targets without causing punitive billing.'
  ));

  elements.push(pageBreak());
  return elements;
}

module.exports = { getChapter7And8 };
