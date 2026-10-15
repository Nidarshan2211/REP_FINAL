/**
 * Chapter 11 & 12: Notification & Document Engines, Deployment & Operations Manual
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

function getChapter11And12() {
  const elements = [];

  // ==========================================
  // CHAPTER 11: NOTIFICATION & DOCUMENT ENGINES
  // ==========================================
  elements.push(h1('Chapter 11: Notification & Document Generation Engines'));

  elements.push(h2('11.1 Spring Mail SMTP Email Integration & Simulation Mode'));
  elements.push(p(
    'AquaFlow features an automated transactional email pipeline powered by Spring Boot Mail (JavaMailSender) and MimeMessageHelper. To ensure seamless operation across both production SMTP gateways and local test environments with mock resident email addresses, the EmailService incorporates an intelligent Dual-Mode architecture:'
  ));

  elements.push(codeBlock([
    'Email Delivery Architecture:',
    '1. Production Mode (Valid SMTP Credentials):',
    '   Dispatches genuine multipart HTML emails via TLS on port 587 (e.g., smtp.gmail.com).',
    '   Features branded HTML email templates, action buttons, and attached PDF receipts.',
    '',
    '2. Local Dev / Fallback Simulation Mode:',
    '   If SMTP host is unreachable, credentials are mock values, or recipient email domain is invalid,',
    '   EmailService catches MailException gracefully, logs the complete email content and activation link',
    '   to the Spring Boot application console, and records an in-app Notification record.',
    '   This ensures that local development and automated testing never block or crash due to external network constraints.'
  ]));

  elements.push(h3('Core Email Notification Workflows:'));
  elements.push(bullet('Resident Onboarding Email: Dispatches activation link with embedded 32-byte raw token and 24-hour expiration notice.', '1.'));
  elements.push(bullet('Monthly Bill Issued Notification: Informs resident of newly finalized bill amount, due date, and payment shortcut.', '2.'));
  elements.push(bullet('Critical Leak Anomaly Warning: Urgent alert dispatched immediately when meter reading exceeds μ + 2σ threshold.', '3.'));
  elements.push(bullet('Payment Confirmation & Digital Receipt: Sent upon successful bill payment with Razorpay transaction reference and attached PDF.', '4.'));

  elements.push(h2('11.2 iText PDF Generation Engine'));
  elements.push(p(
    'Official financial documents within AquaFlow are rendered programmatically using the iText PDF engine (com.itextpdf). The PdfGeneratorService creates high-fidelity vector PDF files conforming to strict banking receipt standards:'
  ));
  elements.push(bullet('Structural Grid & Typography: Utilizes Helvetica font families, exact point-size hierarchy, and aligned tables to prevent wrapping artifacts.', '•'));
  elements.push(bullet('Security Hash & Watermarking: Computes a cryptographic checksum of the payment details (billId + transactionReference + timestamp) and renders it at the bottom of the document to certify authenticity.', '•'));
  elements.push(bullet('Digital Payment Stamp: Dynamically draws an official "PAID & VERIFIED" stamp box featuring green border accents (#16A34A), transaction ID, and payment channel confirmation.', '•'));

  elements.push(pageBreak());

  // ==========================================
  // CHAPTER 12: DEPLOYMENT & OPERATIONS MANUAL
  // ==========================================
  elements.push(h1('Chapter 12: Deployment, Operations & Configuration Manual'));

  elements.push(h2('12.1 Configuration Properties Reference (application.yml)'));
  elements.push(p('The backend configuration is managed via application.yml with externalized environment variable overrides:'));
  elements.push(codeBlock([
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
    '    properties:',
    '      hibernate.dialect: org.hibernate.dialect.MySQLDialect',
    '  mail:',
    '    host: ${SPRING_MAIL_HOST:smtp.gmail.com}',
    '    port: ${SPRING_MAIL_PORT:587}',
    '    username: ${SPRING_MAIL_USERNAME:noreply.smartwater@gmail.com}',
    '    password: ${SPRING_MAIL_PASSWORD:secretapppassword}',
    '    properties:',
    '      mail.smtp.auth: true',
    '      mail.smtp.starttls.enable: true',
    '',
    'app:',
    '  jwt:',
    '    secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}',
    '    expiration-ms: 86400000 # 24 Hours in milliseconds',
    '  frontend:',
    '    url: http://localhost:5173'
  ]));

  elements.push(h2('12.2 Local Development Setup & Execution Commands'));
  elements.push(p('To build and run both the Spring Boot backend and React frontend locally:'));
  elements.push(codeBlock([
    '# =========================================================',
    '# 1. Start Backend Server (Spring Boot / Maven)',
    '# =========================================================',
    'cd p:\\REP\\backend',
    'mvn compile -DskipTests',
    'mvn spring-boot:run',
    '# Server will start on http://localhost:8080/api',
    '',
    '# =========================================================',
    '# 2. Start Frontend Server (React / Vite)',
    '# =========================================================',
    'cd p:\\REP\\frontend',
    'npm.cmd install',
    'npm.cmd run dev',
    '# Client will start on http://localhost:5173',
    '',
    '# =========================================================',
    '# 3. Production Frontend Distribution Build',
    '# =========================================================',
    'cd p:\\REP\\frontend',
    'npm.cmd run build',
    '# Static assets generated in p:\\REP\\frontend\\dist\\'
  ]));

  elements.push(h2('12.3 Operational Health Checks & Troubleshooting Playbook'));
  elements.push(p('Common operational scenarios, diagnostics, and recovery procedures:'));
  elements.push(createTable(
    ['Symptom / Scenario', 'Root Cause Diagnosis', 'Resolution Procedure'],
    [
      ['CSV Upload: Previous reading mismatch error', 'Uploaded CSV contains altered previous_reading values that do not match the trusted database records.', 'Download a fresh template directly from /bulk-billing/template and do not alter the previous_reading column.'],
      ['HTTP 403 Forbidden on API Call', 'JWT token has expired (exceeded 24 hours) or the authenticated user role lacks required @PreAuthorize permission.', 'Log out and re-authenticate to obtain a fresh token. Verify user role in users table.'],
      ['Empty Usage Comparison Chart', 'Building has no configured apartments or no readings have been recorded for the selected billing cycle.', 'Ensure floors and rooms are created under /owner/buildings and meter readings are imported for that cycle.'],
      ['Database Connection Refused', 'MySQL 8.0 service is stopped or root password mismatch.', 'Verify MySQL daemon on port 3306 and check SPRING_DATASOURCE_URL and credentials in application.yml.'],
      ['Email Sending Exception', 'SMTP host unreachable or invalid credentials.', 'Verify SPRING_MAIL_HOST and credentials; check application logs to confirm fallback simulation mode is active.']
    ],
    [25, 35, 40]
  ));

  elements.push(p(''));
  elements.push(callout(
    'System Maintenance & Backup Best Practices',
    '1. Perform nightly mysqldump backups of the rep database.\n' +
    '2. Ensure billing cycles are finalized before opening a new cycle to maintain chronological integrity.\n' +
    '3. Regularly monitor the logs in water_usage for POTENTIAL_LEAK flags and notify affected residents.',
    'success'
  ));

  return elements;
}

module.exports = { getChapter11And12 };
