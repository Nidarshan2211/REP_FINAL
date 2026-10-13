import React from 'react';
import { waterSavingTips } from '../data/waterSavingTips';

export default function WaterSavingTips() {
  return (
    <div className="card glass-card border shadow-sm p-4 bg-white rounded-4 h-100">
      <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
        <div>
          <h5 className="fw-bold mb-1 text-dark">
            <i className="bi bi-lightbulb-fill text-warning me-2"></i> Water-Saving Tips & Best Practices
          </h5>
          <p className="text-muted fs-7 mb-0">Practical habits to lower your monthly water bill and conserve resources</p>
        </div>
        <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-1.5 fs-8 fw-bold rounded-pill">
          <i className="bi bi-leaf-fill me-1"></i> Eco-Smart Feed
        </span>
      </div>

      <div className="row g-3">
        {waterSavingTips.map(tip => (
          <div className="col-md-6 col-lg-4" key={tip.id}>
            <div className="p-3 bg-light rounded-3 border h-100 d-flex flex-column justify-content-between transition-hover">
              <div>
                <div className="d-flex align-items-center justify-content-between mb-2">
                  <span className={`badge ${tip.badgeColor} border px-2.5 py-1 fs-8 fw-semibold rounded-pill`}>
                    {tip.category}
                  </span>
                  <div className="rounded-circle bg-white p-2 d-flex align-items-center justify-content-center shadow-sm" style={{ width: 34, height: 34 }}>
                    <i className={`bi ${tip.icon} fs-6 text-primary`}></i>
                  </div>
                </div>
                <h6 className="fw-bold text-dark mb-1 fs-7">{tip.title}</h6>
                <p className="text-muted fs-8 mb-0 leading-normal">{tip.description}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
