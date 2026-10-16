import React, { useState, useEffect } from 'react';
import API from '../services/api';

/**
 * LeakFeedbackModal
 * Captures user / owner ground truth on leak detections to retrain ML models.
 */
export default function LeakFeedbackModal({
  show,
  onClose,
  onSubmitted,
  usageId,
  incidentId = null,
  apartmentLabel = 'Flat',
  isOwner = false
}) {
  const residentOptions = [
    { value: 'CONFIRMED_LEAK', label: 'Confirmed Leak (found dripping tap, running toilet, or seep)' },
    { value: 'FALSE_ALARM', label: 'False Alarm (no leak, usage was genuine)' },
    { value: 'TEMPORARY_HIGH_USAGE', label: 'Temporary High Usage (guests staying, deep cleaning, gardening)' },
    { value: 'FIXED_NOW', label: 'Leak Fixed (repair completed)' },
    { value: 'OTHER', label: 'Other / Unsure' }
  ];

  const ownerOptions = [
    { value: 'REAL_LEAK_CONFIRMED', label: 'Real Leak Confirmed (plumber inspected and confirmed)' },
    { value: 'FALSE_ALARM', label: 'False Alarm (resident confirmed legitimate usage spike)' },
    { value: 'METER_FAULT', label: 'Meter Faulty / Hardware Malfunction' },
    { value: 'COMMON_AREA_LEAK', label: 'Shared / Common Area Pipe Issue' },
    { value: 'OTHER', label: 'Other Inspection Result' }
  ];

  const options = isOwner ? ownerOptions : residentOptions;

  const [feedbackReason, setFeedbackReason] = useState(options[0].value);
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (show) {
      setFeedbackReason(options[0].value);
      setNotes('');
      setError('');
      setSuccess(false);
    }
  }, [show, isOwner]);

  if (!show) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!usageId) {
      setError('Missing water usage record reference.');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const payload = {
        feedback: feedbackReason,
        note: notes.trim() || undefined
      };

      const endpoint = isOwner ? '/owner/leak-feedback' : '/resident/leak-feedback';
      await API.post(endpoint, payload, { params: { usageId } });

      setSuccess(true);
      setTimeout(() => {
        if (onSubmitted) onSubmitted();
        onClose();
      }, 1200);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit feedback.');
      setSubmitting(false);
    }
  };

  return (
    <div className="modal fade show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
          
          {/* Header */}
          <div className="modal-header border-bottom bg-light px-4 py-3">
            <h5 className="modal-title fw-bold text-dark d-flex align-items-center gap-2 mb-0">
              <i className="bi bi-chat-square-quote-fill text-primary"></i>
              Report Leak Feedback ({apartmentLabel})
            </h5>
            <button type="button" className="btn-close" onClick={onClose} disabled={submitting}></button>
          </div>

          {/* Body */}
          <div className="modal-body p-4">
            {success ? (
              <div className="text-center py-4 text-success">
                <i className="bi bi-check-circle-fill display-3 mb-2"></i>
                <h5 className="fw-bold text-dark">Thank You For Your Feedback!</h5>
                <p className="text-muted fs-7 mb-0">
                  Your feedback helps continuously calibrate our machine learning models to prevent false alarms.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="p-3 bg-light rounded-3 mb-3 border">
                  <div className="d-flex align-items-start gap-2">
                    <i className="bi bi-cpu text-primary fs-5"></i>
                    <div className="fs-8 text-muted">
                      Your feedback will be logged and incorporated into our automated machine learning retraining pipeline to refine future consumption boundaries.
                    </div>
                  </div>
                </div>

                {error && (
                  <div className="alert alert-danger py-2 px-3 rounded-3 small mb-3">
                    <i className="bi bi-exclamation-circle me-1"></i> {error}
                  </div>
                )}

                <div className="mb-3">
                  <label className="form-label fw-semibold fs-7 text-dark">
                    What best describes this usage cycle? <span className="text-danger">*</span>
                  </label>
                  <select
                    className="form-select fs-7"
                    value={feedbackReason}
                    onChange={(e) => setFeedbackReason(e.target.value)}
                    required
                  >
                    {options.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mb-4">
                  <label className="form-label fw-semibold fs-7 text-dark">
                    Additional Inspection Notes <span className="text-muted fw-normal">(Optional)</span>
                  </label>
                  <textarea
                    rows="3"
                    maxLength="500"
                    className="form-control fs-7"
                    placeholder="e.g., Plumber found small flapper leak in master bathroom, replaced rubber seal on 12th Oct."
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                  />
                </div>

                <div className="d-flex justify-content-end gap-2">
                  <button
                    type="button"
                    className="btn btn-light px-3 fw-semibold"
                    onClick={onClose}
                    disabled={submitting}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn btn-dark px-4 fw-bold"
                    disabled={submitting}
                  >
                    {submitting ? 'Submitting...' : 'Submit Feedback'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
