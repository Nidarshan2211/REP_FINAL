/**
 * Helper to display human-friendly labels for leak feedback categories
 */
export function formatFeedbackLabel(feedback) {
  if (!feedback) return '';
  const map = {
    'FALSE_ALARM': 'False Alarm',
    'REAL_LEAK_CONFIRMED': 'Confirmed Leak',
    'CONFIRMED_LEAK': 'Confirmed Leak',
    'PLUMBING_WORK': 'Plumbing Work',
    'GUESTS_VISITING': 'Guests Visiting',
    'TEMPORARY_HIGH_USAGE': 'Temporary High Usage',
    'FIXED_NOW': 'Leak Fixed',
    'METER_FAULT': 'Meter Fault',
    'COMMON_AREA_LEAK': 'Common Area Leak',
    'OTHER': 'Other'
  };
  return map[feedback.toUpperCase()] || feedback.replace(/_/g, ' ');
}

/**
 * LeakStatusBadge
 * Renders a standardized, color-coded badge for leak anomaly status.
 * Statuses: NORMAL, POTENTIAL_LEAK, INSUFFICIENT_HISTORY
 */
export default function LeakStatusBadge({
  status,
  confidence,
  size = 'md',
  className = '',
  hasFeedback = false,
  feedbackLabel = ''
}) {
  const normStatus = (status || 'INSUFFICIENT_HISTORY').toUpperCase();

  let config = {
    label: 'Baseline Building',
    icon: 'bi-clock-history',
    bgClass: 'bg-secondary-subtle text-secondary border border-secondary-subtle',
    pulse: false
  };

  if (hasFeedback) {
    const formatted = feedbackLabel ? formatFeedbackLabel(feedbackLabel) : 'Feedback Logged';
    config = {
      label: `Feedback: ${formatted}`,
      icon: 'bi-check-circle-fill',
      bgClass: 'bg-success-subtle text-success border border-success-subtle',
      pulse: false
    };
  } else if (normStatus === 'NORMAL') {
    config = {
      label: 'Normal Usage',
      icon: 'bi-check-circle-fill',
      bgClass: 'bg-success-subtle text-success border border-success-subtle',
      pulse: false
    };
  } else if (normStatus === 'POTENTIAL_LEAK') {
    config = {
      label: 'Potential Leak',
      icon: 'bi-exclamation-triangle-fill',
      bgClass: 'bg-danger-subtle text-danger border border-danger-subtle',
      pulse: true
    };
  }

  const sizeClasses = {
    sm: 'px-2 py-0.5 fs-8',
    md: 'px-2.5 py-1 fs-7',
    lg: 'px-3 py-1.5 fs-6'
  };

  let confLabel = null;
  if (typeof confidence === 'number' && !isNaN(confidence)) {
    const val = (confidence > 0 && confidence <= 1.0) ? Math.round(confidence * 100) : Math.round(confidence);
    confLabel = `${val}%`;
  } else if (typeof confidence === 'string' && confidence.trim()) {
    const num = parseFloat(confidence);
    if (!isNaN(num)) {
      const val = (num > 0 && num <= 1.0) ? Math.round(num * 100) : Math.round(num);
      confLabel = `${val}%`;
    } else {
      confLabel = confidence.toUpperCase();
    }
  }

  return (
    <span
      className={`badge rounded-pill fw-semibold d-inline-flex align-items-center gap-1.5 shadow-xs ${config.bgClass} ${sizeClasses[size] || sizeClasses.md} ${className}`}
      style={{ letterSpacing: '0.02em' }}
    >
      <i className={`bi ${config.icon} ${config.pulse ? 'text-danger animate-pulse' : ''}`}></i>
      <span>{config.label}</span>
      {confLabel && (
        <span className="opacity-75 small ms-0.5" title={`Model Confidence: ${confLabel}`}>
          ({confLabel})
        </span>
      )}
    </span>
  );
}
