import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import API from '../services/api';

export default function AcceptInvitation() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [loading, setLoading] = useState(true);
  const [inviteData, setInviteData] = useState(null);
  const [status, setStatus] = useState('LOADING'); // 'LOADING', 'PENDING', 'EXPIRED', 'ALREADY_ACCEPTED', 'INVALID', 'SUCCESS'
  const [errorMessage, setErrorMessage] = useState('');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!token) {
      setStatus('INVALID');
      setErrorMessage('No invitation token was provided in the URL.');
      setLoading(false);
      return;
    }

    API.get(`/api/residents/invitations/validate?token=${encodeURIComponent(token)}`)
      .then(res => {
        const data = res.data;
        setInviteData(data);
        setStatus(data.status || 'PENDING');
        setErrorMessage(data.message || '');
      })
      .catch(err => {
        setStatus('INVALID');
        setErrorMessage(err.response?.data?.message || 'Invalid or expired invitation token.');
      })
      .finally(() => setLoading(false));
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password.length < 6) {
      alert('Password must be at least 6 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      alert('Passwords do not match. Please verify your entry.');
      return;
    }

    setSubmitting(true);
    try {
      await API.post('/api/residents/invitations/accept', {
        token: token,
        password: password
      });
      setStatus('SUCCESS');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to activate account.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center bg-slate-50">
        <div className="text-center">
          <div className="spinner-border text-dark mb-3" role="status" style={{ width: '3rem', height: '3rem' }}></div>
          <h5 className="fw-bold text-dark">Validating Secure Token...</h5>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-slate-50 py-5 px-3">
      <div className="card border-0 shadow-lg p-4 p-md-5 rounded-4" style={{ maxWidth: '540px', width: '100%', backgroundColor: '#ffffff' }}>
        
        {/* Brand Header */}
        <div className="text-center mb-4">
          <div className="d-inline-flex align-items-center justify-content-center bg-dark text-white rounded-circle mb-3" style={{ width: '56px', height: '56px' }}>
            <i className="bi bi-droplet-fill fs-3"></i>
          </div>
          <h3 className="fw-bold text-dark mb-1">AquaFlow</h3>
          <p className="text-muted small mb-0">Smart Water Management & Billing Portal</p>
        </div>

        {/* State 1: SUCCESS */}
        {status === 'SUCCESS' && (
          <div className="text-center py-3">
            <div className="mb-3 text-success">
              <i className="bi bi-check-circle-fill display-1"></i>
            </div>
            <h4 className="fw-bold text-dark mb-2">Account Activated Successfully!</h4>
            <p className="text-muted mb-4 fs-7">
              Your AquaFlow resident account has been created. You can now log in using your email and new password.
            </p>
            <Link to="/login" className="btn btn-dark w-100 py-2.5 fw-bold shadow-sm">
              <i className="bi bi-box-arrow-in-right me-2"></i> Go to Login
            </Link>
          </div>
        )}

        {/* State 2: PENDING (Valid Token Activation Form) */}
        {status === 'PENDING' && (
          <div>
            <div className="alert alert-light border border-secondary-subtle rounded-3 mb-4 p-3">
              <h6 className="fw-bold text-dark mb-2"><i className="bi bi-person-check-fill text-dark me-2"></i>Welcome, {inviteData?.residentName}!</h6>
              <div className="fs-7 text-muted">
                <div><strong>Email:</strong> {inviteData?.email}</div>
                <div><strong>Building:</strong> {inviteData?.buildingName}</div>
                <div><strong>Apartment:</strong> {inviteData?.apartmentNumber}</div>
              </div>
            </div>

            <h5 className="fw-bold text-dark mb-3">Set Up Your Password</h5>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label fw-semibold fs-7 text-dark">Create Password *</label>
                <input
                  type="password"
                  className="form-control form-control-lg fs-6"
                  placeholder="At least 6 characters"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  minLength={6}
                  required
                />
              </div>

              <div className="mb-4">
                <label className="form-label fw-semibold fs-7 text-dark">Confirm Password *</label>
                <input
                  type="password"
                  className="form-control form-control-lg fs-6"
                  placeholder="Re-enter your password"
                  value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)}
                  minLength={6}
                  required
                />
              </div>

              <button
                type="submit"
                className="btn btn-dark w-100 py-2.5 fw-bold shadow-sm"
                disabled={submitting}
              >
                {submitting ? 'Activating Account...' : 'Activate Account'}
              </button>
            </form>
          </div>
        )}

        {/* State 3: EXPIRED */}
        {status === 'EXPIRED' && (
          <div className="text-center py-3">
            <div className="mb-3 text-warning">
              <i className="bi bi-exclamation-triangle-fill display-1"></i>
            </div>
            <h4 className="fw-bold text-dark mb-2">Invitation Expired</h4>
            <p className="text-muted mb-4 fs-7">
              {errorMessage || 'This invitation link has expired. Please contact your Building Owner and request a new invitation.'}
            </p>
            <Link to="/login" className="btn btn-outline-dark w-100 py-2.5 fw-bold">
              Back to Login
            </Link>
          </div>
        )}

        {/* State 4: ALREADY_ACCEPTED */}
        {status === 'ALREADY_ACCEPTED' && (
          <div className="text-center py-3">
            <div className="mb-3 text-info">
              <i className="bi bi-info-circle-fill display-1"></i>
            </div>
            <h4 className="fw-bold text-dark mb-2">Invitation Already Used</h4>
            <p className="text-muted mb-4 fs-7">
              {errorMessage || 'This invitation has already been accepted. Please log in using your registered email and password.'}
            </p>
            <Link to="/login" className="btn btn-dark w-100 py-2.5 fw-bold shadow-sm">
              <i className="bi bi-box-arrow-in-right me-2"></i> Go to Login
            </Link>
          </div>
        )}

        {/* State 5: INVALID */}
        {status === 'INVALID' && (
          <div className="text-center py-3">
            <div className="mb-3 text-danger">
              <i className="bi bi-x-circle-fill display-1"></i>
            </div>
            <h4 className="fw-bold text-dark mb-2">Invalid Invitation</h4>
            <p className="text-muted mb-4 fs-7">
              {errorMessage || 'This invitation link is invalid or no longer exists.'}
            </p>
            <Link to="/login" className="btn btn-outline-dark w-100 py-2.5 fw-bold">
              Back to Login
            </Link>
          </div>
        )}

      </div>
    </div>
  );
}
