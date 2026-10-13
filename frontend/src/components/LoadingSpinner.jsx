import React from 'react';

export default function LoadingSpinner({ text = "Loading..." }) {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center p-5 min-vh-50">
      <div className="spinner-border text-info" style={{ width: '3rem', height: '3rem' }} role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
      <p className="mt-3 text-muted fw-semibold">{text}</p>
    </div>
  );
}
