import React from 'react';
import { Link } from 'react-router-dom';

export default function LandingPage() {
  return (
    <div className="landing-page" style={{ backgroundColor: '#F0FAFF' }}>
      {/* Hero Section - Sprink Style Light Sky-Aqua */}
      <section className="hero-section text-center py-5">
        <div className="container py-4">
          <div className="row justify-content-center">
            <div className="col-lg-9 text-center">
              <span className="badge bg-white px-3.5 py-2 rounded-pill fw-bold mb-3 fs-7 shadow-sm border" style={{ color: '#005F73', borderColor: '#D4EBF8' }}>
                <i className="bi bi-droplet-fill me-1.5" style={{ color: '#0A9396' }}></i> Sprink : Smart Water Platform
              </span>
              <h1 className="display-4 fw-extrabold mb-3" style={{ color: '#003554' }}>
                Intelligent Water Metering & Automated Billing
              </h1>
              <p className="lead mb-4 mx-auto" style={{ color: '#486581', maxWidth: 720, fontSize: '1.05rem' }}>
                Empowering Building Owners and Residents with real-time water consumption insights, automated 3-tier tariff billing engines, seamless payment processing, and instant notification alerts.
              </p>
              <div className="d-flex flex-wrap gap-3 justify-content-center">
                <Link to="/register-owner" className="btn btn-sprink-primary btn-lg px-4 text-white fw-bold shadow-sm" style={{ fontSize: '0.95rem' }}>
                  Register as Building Owner <i className="bi bi-arrow-right ms-1"></i>
                </Link>
                <Link to="/login" className="btn btn-sprink-secondary btn-lg px-4 fw-bold shadow-sm" style={{ fontSize: '0.95rem' }}>
                  Portal Login
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-5 bg-white border-bottom">
        <div className="container py-4">
          <div className="text-center max-w-75 mx-auto mb-5">
            <h2 className="fw-bold" style={{ color: '#003554' }}>Designed for Scalable Property Management</h2>
            <p style={{ color: '#486581' }}>
              AquaFlow eliminates manual water meter reading errors, resolves tenant billing disputes, and streamlines utility revenue collection through pure clean architecture.
            </p>
          </div>
          <div className="row g-4">
            <div className="col-md-4">
              <div className="card h-100 border p-4 text-center glass-card shadow-sm">
                <div className="rounded-circle p-3 mx-auto mb-3 text-white d-flex align-items-center justify-content-center" style={{ width: 64, height: 64, backgroundColor: '#E3F6FF' }}>
                  <i className="bi bi-shield-check fs-2" style={{ color: '#005F73' }}></i>
                </div>
                <h5 className="fw-bold" style={{ color: '#003554' }}>Super Admin Governance</h5>
                <p className="fs-7 mb-0" style={{ color: '#486581' }}>
                  Strict approval workflow for building owners ensuring complete database security and operational oversight.
                </p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border p-4 text-center glass-card shadow-sm">
                <div className="rounded-circle p-3 mx-auto mb-3 text-white d-flex align-items-center justify-content-center" style={{ width: 64, height: 64, backgroundColor: '#E3F6FF' }}>
                  <i className="bi bi-building fs-2" style={{ color: '#005F73' }}></i>
                </div>
                <h5 className="fw-bold" style={{ color: '#003554' }}>Building Owner Portal</h5>
                <p className="fs-7 mb-0" style={{ color: '#486581' }}>
                  Invite residents, manage smart water meters, record monthly readings, and generate batch invoices in one click.
                </p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border p-4 text-center glass-card shadow-sm">
                <div className="rounded-circle p-3 mx-auto mb-3 text-white d-flex align-items-center justify-content-center" style={{ width: 64, height: 64, backgroundColor: '#E3F6FF' }}>
                  <i className="bi bi-person-badge fs-2" style={{ color: '#005F73' }}></i>
                </div>
                <h5 className="fw-bold" style={{ color: '#003554' }}>Resident Dashboard</h5>
                <p className="fs-7 mb-0" style={{ color: '#486581' }}>
                  Transparent breakdown of water usage, instant online bill payments, PDF bill downloads, and automated reminders.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section id="services" className="py-5" style={{ backgroundColor: '#F0FAFF' }}>
        <div className="container py-4">
          <h2 className="text-center fw-bold mb-5" style={{ color: '#003554' }}>Key System Capabilities</h2>
          <div className="row g-4">
            <div className="col-md-6 col-lg-3">
              <div className="p-4 bg-white rounded-4 border shadow-sm h-100 glass-card">
                <i className="bi bi-calculator fs-3 mb-2 d-block" style={{ color: '#005F73' }}></i>
                <h6 className="fw-bold" style={{ color: '#003554' }}>3-Tier Tariff Engine</h6>
                <small style={{ color: '#486581' }}>Automated progressive slab pricing (Tier 1: 0-20 kL, Tier 2: 20-40 kL, Tier 3: &gt;40 kL).</small>
              </div>
            </div>
            <div className="col-md-6 col-lg-3">
              <div className="p-4 bg-white rounded-4 border shadow-sm h-100 glass-card">
                <i className="bi bi-envelope-paper fs-3 mb-2 d-block" style={{ color: '#005F73' }}></i>
                <h6 className="fw-bold" style={{ color: '#003554' }}>Tokenized Invitations</h6>
                <small style={{ color: '#486581' }}>Secure email invitation tokens for resident onboarding and password setup.</small>
              </div>
            </div>
            <div className="col-md-6 col-lg-3">
              <div className="p-4 bg-white rounded-4 border shadow-sm h-100 glass-card">
                <i className="bi bi-file-earmark-pdf fs-3 mb-2 d-block" style={{ color: '#005F73' }}></i>
                <h6 className="fw-bold" style={{ color: '#003554' }}>PDF Invoice Generator</h6>
                <small style={{ color: '#486581' }}>Generate and download official PDF billing statements on demand.</small>
              </div>
            </div>
            <div className="col-md-6 col-lg-3">
              <div className="p-4 bg-white rounded-4 border shadow-sm h-100 glass-card">
                <i className="bi bi-credit-card fs-3 mb-2 d-block" style={{ color: '#005F73' }}></i>
                <h6 className="fw-bold" style={{ color: '#003554' }}>Payment Gateway Mock</h6>
                <small style={{ color: '#486581' }}>Instant payment confirmation with automated receipt generation.</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer id="contact" className="py-4 border-top text-white" style={{ backgroundColor: '#003554' }}>
        <div className="container text-center">
          <p className="mb-1 fw-bold">Sprink | AquaFlow Smart Water Billing and Management System</p>
          <small className="opacity-75">© 2026 Smart Water Inc. All Rights Reserved. Built with React 19 & Spring Boot 3.</small>
        </div>
      </footer>
    </div>
  );
}
