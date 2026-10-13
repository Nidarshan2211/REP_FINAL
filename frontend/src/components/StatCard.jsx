import React from 'react';

export default function StatCard({ title, value, subtext, icon }) {
  return (
    <div className="card glass-card h-100 border p-3 shadow-sm bg-white">
      <div className="card-body p-2 d-flex align-items-center justify-content-between">
        <div>
          <h6 className="text-uppercase text-muted fw-bold mb-1 fs-8">{title}</h6>
          <h3 className="fw-bold mb-0 text-dark">{value}</h3>
          {subtext && <small className="text-muted fs-8">{subtext}</small>}
        </div>
        {icon && (
          <div className="rounded-3 bg-light border p-2.5 text-dark fs-4 d-flex align-items-center justify-content-center" style={{ width: 48, height: 48 }}>
            <i className={`bi ${icon}`}></i>
          </div>
        )}
      </div>
    </div>
  );
}
