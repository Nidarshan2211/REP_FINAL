import React from 'react';

/**
 * RiskScoreBar
 * Visual 0–100% risk meter with Low (<40%), Medium (40-69%), and High (>=70%) risk thresholds.
 */
export default function RiskScoreBar({ score = 0, showLabel = true, height = 8, compact = false }) {
  // Normalize score to 0..100
  let normScore = typeof score === 'number' ? score : 0;
  if (normScore > 0 && normScore <= 1.0) {
    normScore = Math.round(normScore * 100);
  } else {
    normScore = Math.max(0, Math.min(100, Math.round(normScore)));
  }

  let level = 'LOW';
  let color = '#198754'; // Bootstrap success green
  let bgSoft = 'rgba(25, 135, 84, 0.15)';
  let textColor = 'text-success';

  if (normScore >= 70) {
    level = 'HIGH';
    color = '#dc3545'; // Bootstrap danger red
    bgSoft = 'rgba(220, 53, 69, 0.15)';
    textColor = 'text-danger';
  } else if (normScore >= 40) {
    level = 'MEDIUM';
    color = '#fd7e14'; // Bootstrap orange / warning
    bgSoft = 'rgba(253, 126, 20, 0.15)';
    textColor = 'text-warning-emphasis';
  }

  return (
    <div className="w-100">
      {showLabel && !compact && (
        <div className="d-flex justify-content-between align-items-center mb-1">
          <span className="fs-8 text-muted text-uppercase fw-semibold">Leak Risk Assessment</span>
          <span className={`fs-7 fw-bold ${textColor}`}>
            {normScore}% <small className="fw-normal">({level})</small>
          </span>
        </div>
      )}

      {showLabel && compact && (
        <div className="d-flex align-items-center justify-content-between gap-2 mb-1">
          <span className={`badge rounded-pill px-2 py-0.5 fs-8 fw-bold ${textColor}`} style={{ backgroundColor: bgSoft }}>
            {normScore}% {level}
          </span>
        </div>
      )}

      <div
        className="progress rounded-pill overflow-hidden"
        style={{
          height: `${height}px`,
          backgroundColor: '#e9ecef',
          boxShadow: 'inset 0 1px 2px rgba(0,0,0,0.05)'
        }}
        role="progressbar"
        aria-valuenow={normScore}
        aria-valuemin="0"
        aria-valuemax="100"
      >
        <div
          className="progress-bar rounded-pill transition-all"
          style={{
            width: `${normScore}%`,
            backgroundColor: color,
            transition: 'width 0.6s cubic-bezier(0.4, 0, 0.2, 1)'
          }}
        />
      </div>

      {!compact && (
        <div className="d-flex justify-content-between mt-1 fs-9 text-muted px-0.5">
          <span>0% Normal</span>
          <span>40% Watch</span>
          <span>70% Suspect</span>
          <span>100% Leak</span>
        </div>
      )}
    </div>
  );
}
