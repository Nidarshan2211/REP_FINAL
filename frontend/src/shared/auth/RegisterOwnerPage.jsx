import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

export default function RegisterOwnerPage() {
  const { registerOwner } = useContext(AuthContext);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    buildingName: '',
    buildingNumber: '',
    totalFlats: 12,
    location: ''
  });

  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setLoading(true);

    try {
      const res = await registerOwner(formData);
      setSuccessMsg(res.message || 'Registration successful! Status: PENDING Super Admin approval.');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please check inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-vh-100 bg-light py-5">
      <div className="container" style={{ maxWidth: 650 }}>
        <div className="card glass-card border-0 p-4 p-md-5 shadow-lg">
          <div className="text-center mb-4">
            <h3 className="fw-bold">Building Owner Registration</h3>
            <p className="text-muted fs-7">Register your property for smart water billing management</p>
          </div>

          {error && <div className="alert alert-danger py-2 px-3 small rounded-3 mb-3">{error}</div>}
          {successMsg && (
            <div className="alert alert-success py-3 px-3 small rounded-3 mb-3">
              <i className="bi bi-check-circle-fill me-2 fs-5"></i>
              <strong>{successMsg}</strong>
              <div className="mt-2">
                <Link to="/login" className="btn btn-sm btn-outline-success fw-bold">Proceed to Login</Link>
              </div>
            </div>
          )}

          {!successMsg && (
            <form onSubmit={handleSubmit}>
              <h6 className="text-uppercase text-primary fw-bold fs-7 mb-3 border-bottom pb-2">Owner Information</h6>
              <div className="row g-3 mb-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Full Name *</label>
                  <input type="text" name="name" className="form-control" placeholder="John Doe" value={formData.name} onChange={handleChange} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Email Address *</label>
                  <input type="email" name="email" className="form-control" placeholder="john@example.com" value={formData.email} onChange={handleChange} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Password *</label>
                  <input type="password" name="password" className="form-control" placeholder="At least 6 chars" value={formData.password} onChange={handleChange} required minLength={6} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Phone Number *</label>
                  <input type="tel" name="phone" className="form-control" placeholder="+1 555-0199" value={formData.phone} onChange={handleChange} required />
                </div>
              </div>

              <h6 className="text-uppercase text-primary fw-bold fs-7 mb-3 border-bottom pb-2">Building Information</h6>
              <div className="row g-3 mb-4">
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Building Name *</label>
                  <input type="text" name="buildingName" className="form-control" placeholder="Skyline Towers" value={formData.buildingName} onChange={handleChange} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Building Number/Code *</label>
                  <input type="text" name="buildingNumber" className="form-control" placeholder="BLK-A" value={formData.buildingNumber} onChange={handleChange} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Total Flats *</label>
                  <input type="number" name="totalFlats" className="form-control" value={formData.totalFlats} onChange={handleChange} required min={1} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold fs-7 text-muted">Location / Address *</label>
                  <input type="text" name="location" className="form-control" placeholder="123 Palm Street" value={formData.location} onChange={handleChange} required />
                </div>
              </div>

              <button type="submit" className="btn btn-primary-gradient w-100 py-2.5 fw-bold" disabled={loading}>
                {loading ? 'Submitting Registration...' : 'Submit Owner Registration'}
              </button>
            </form>
          )}

          <div className="mt-4 text-center">
            <span className="text-muted fs-7">Already registered? </span>
            <Link to="/login" className="text-decoration-none fw-bold text-info fs-7">Sign In</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
