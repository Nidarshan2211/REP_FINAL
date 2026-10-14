import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function OwnerResidentsPage() {
  const [residents, setResidents] = useState([]);
  const [buildings, setBuildings] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  // Success Banner Alert with Email Sent Status
  const [alertData, setAlertData] = useState(null); // { email, emailSent, link, message }
  const [copied, setCopied] = useState(false);

  // Invite Modal State
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteData, setInviteData] = useState({
    buildingId: '',
    name: '',
    email: '',
    phone: '',
    apartmentNumber: ''
  });

  // Edit Modal State
  const [editingResident, setEditingResident] = useState(null);

  const fetchResidents = () => {
    API.get('/owners/residents', {
      params: { query: searchQuery, status: statusFilter }
    })
      .then(res => setResidents(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  const fetchBuildings = () => {
    API.get('/owners/buildings')
      .then(res => {
        setBuildings(res.data);
        if (res.data.length > 0) {
          setInviteData(prev => ({ ...prev, buildingId: res.data[0].buildingId }));
        }
      });
  };

  useEffect(() => {
    fetchBuildings();
  }, []);

  useEffect(() => {
    fetchResidents();
  }, [searchQuery, statusFilter]);

  const handleInviteSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await API.post('/api/residents/invite', inviteData);
      setShowInviteModal(false);
      const invitedEmail = inviteData.email;
      const emailSent = res.data?.emailSent ?? true;
      const rawToken = res.data?.invitationToken;
      const activationUrl = rawToken ? `${window.location.origin}/accept-invitation?token=${rawToken}&email=${encodeURIComponent(invitedEmail)}` : '';

      setInviteData({ buildingId: buildings[0]?.buildingId || '', name: '', email: '', phone: '', apartmentNumber: '' });
      fetchResidents();

      setAlertData({
        email: invitedEmail,
        emailSent: emailSent,
        link: activationUrl,
        message: emailSent
          ? `✓ Invitation sent successfully! An account activation link has been sent to ${invitedEmail}.`
          : `⚠️ Invitation created, but email delivery failed for ${invitedEmail}. Please check SMTP configuration or use "Resend Invite".`
      });
      setCopied(false);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to send invitation');
    }
  };

  const handleResendInvite = async (residentId, residentEmail) => {
    if (!window.confirm(`Resend invitation link to ${residentEmail}? This will invalidate any previous link.`)) return;
    try {
      const res = await API.post(`/api/residents/invitations/${residentId}/resend`);
      fetchResidents();

      const emailSent = res.data?.success ?? true;
      // Fetch fresh resident to get rawToken if available for dev fallback
      const updatedList = await API.get('/owners/residents');
      const updatedRes = (updatedList.data || []).find(r => r.residentId === residentId);
      const rawToken = updatedRes?.invitationToken;
      const activationUrl = rawToken ? `${window.location.origin}/accept-invitation?token=${rawToken}&email=${encodeURIComponent(residentEmail)}` : '';

      setAlertData({
        email: residentEmail,
        emailSent: emailSent,
        link: activationUrl,
        message: res.data?.message || (emailSent
          ? `✓ New invitation link sent to ${residentEmail}!`
          : `⚠️ Invitation token regenerated, but email delivery failed for ${residentEmail}.`)
      });
      setCopied(false);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to resend invitation');
    }
  };

  const handleCopyLink = () => {
    if (alertData?.link) {
      navigator.clipboard.writeText(alertData.link);
      setCopied(true);
      setTimeout(() => setCopied(false), 3000);
    }
  };

  const handleUpdateResident = async (e) => {
    e.preventDefault();
    try {
      await API.put(`/owners/residents/${editingResident.residentId}`, {
        name: editingResident.name,
        phone: editingResident.phone,
        apartmentNumber: editingResident.apartmentNumber
      });
      setEditingResident(null);
      fetchResidents();
    } catch (err) {
      alert(err.response?.data?.message || 'Update failed');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to remove this resident?')) {
      try {
        await API.delete(`/owners/residents/${id}`);
        fetchResidents();
      } catch (err) {
        alert('Delete failed');
      }
    }
  };

  if (loading) return <LoadingSpinner text="Loading Residents database..." />;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
          <h2 className="fw-bold mb-1 text-dark">Resident Management</h2>
          <p className="text-muted small mb-0">Invite residents, manage accounts, and monitor activation status</p>
        </div>
        <button className="btn btn-dark fw-bold" onClick={() => setShowInviteModal(true)}>
          <i className="bi bi-person-plus-fill me-1.5"></i> Invite Resident
        </button>
      </div>

      {alertData && (
        <div className={`alert ${alertData.emailSent ? 'alert-success border-success-subtle' : 'alert-warning border-warning-subtle'} rounded-4 shadow-sm mb-4 p-4`}>
          <div className="d-flex align-items-center justify-content-between mb-2">
            <div className="d-flex align-items-center gap-2">
              <i className={`bi ${alertData.emailSent ? 'bi-check-circle-fill text-success' : 'bi-exclamation-triangle-fill text-warning'} fs-5`}></i>
              <span className="fw-bold text-dark fs-6">{alertData.message}</span>
            </div>
            <button type="button" className="btn-close" onClick={() => setAlertData(null)}></button>
          </div>

          <div className="text-muted fs-7 mb-2">
            {alertData.emailSent ? (
              <span>The resident can check their email inbox (<strong>{alertData.email}</strong>) and click <strong>"Activate My Account"</strong> to set up their password. The invitation link expires in 24 hours.</span>
            ) : (
              <span>Please check your backend SMTP mail configuration (e.g. Gmail App Password in environment variables) or click <strong>"Resend Invite"</strong>.</span>
            )}
          </div>

          {/* Development Fallback Accordion Toggle */}
          {alertData.link && (
            <details className="mt-2">
              <summary className="btn btn-sm btn-outline-secondary py-1 px-2.5 fs-8 fw-semibold cursor-pointer">
                🛠️ Development Fallback (Click to view activation link)
              </summary>
              <div className="input-group mt-2">
                <input
                  type="text"
                  className="form-control bg-white fw-mono fs-7"
                  readOnly
                  value={alertData.link}
                />
                <button
                  className={`btn ${copied ? 'btn-success' : 'btn-dark'} fw-bold px-3`}
                  onClick={handleCopyLink}
                >
                  <i className={`bi ${copied ? 'bi-check-lg' : 'bi-clipboard'} me-1`}></i>
                  {copied ? 'Copied!' : 'Copy Link'}
                </button>
                <a
                  href={alertData.link}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-outline-dark fw-bold px-3"
                >
                  <i className="bi bi-box-arrow-up-right me-1"></i> Open Link
                </a>
              </div>
            </details>
          )}
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="card glass-card border mb-4 p-3 shadow-sm bg-white">
        <div className="row g-3 align-items-center">
          <div className="col-md-6">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0"><i className="bi bi-search"></i></span>
              <input
                type="text"
                className="form-control border-start-0 ps-0 bg-light"
                placeholder="Search residents by name or email..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
          <div className="col-md-6 d-flex align-items-center justify-content-md-end gap-2">
            <span className="fw-semibold fs-7 text-muted">Account Status:</span>
            {['ALL', 'ACTIVE', 'INACTIVE'].map(status => (
              <button
                key={status}
                className={`btn btn-sm fw-semibold ${statusFilter === status ? 'btn-dark' : 'btn-outline-secondary bg-white'}`}
                onClick={() => setStatusFilter(status)}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card glass-card border shadow-sm bg-white">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Resident</th>
                  <th>Apartment</th>
                  <th>Building</th>
                  <th>Meter Assigned</th>
                  <th>Invitation Status</th>
                  <th>Account Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {residents.length ? (
                  residents.map(r => (
                    <tr key={r.residentId}>
                      <td>
                        <div className="fw-bold text-dark">{r.name}</div>
                        <small className="text-muted">{r.email} • {r.phone}</small>
                      </td>
                      <td><span className="badge bg-secondary-subtle text-dark border">{r.apartmentNumber}</span></td>
                      <td>{r.buildingName}</td>
                      <td><code className="text-dark fw-bold">{r.meterNumber}</code></td>
                      <td>
                        <span className={`badge ${
                          r.invitationStatus === 'ACCEPTED' ? 'bg-success-subtle text-success border border-success-subtle' :
                          r.invitationStatus === 'EXPIRED' ? 'bg-warning-subtle text-dark border border-warning-subtle' :
                          r.invitationStatus === 'CANCELLED' ? 'bg-light text-muted border' :
                          'bg-warning-subtle text-dark border border-warning-subtle'
                        }`}>
                          {r.invitationStatus || 'PENDING'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${r.accountStatus === 'ACTIVE' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-dark border'}`}>
                          {r.accountStatus === 'ACTIVE' ? 'ACTIVE' : 'NOT ACTIVATED'}
                        </span>
                      </td>
                      <td>
                        <div className="d-flex align-items-center gap-1">
                          {(r.invitationStatus === 'PENDING' || r.invitationStatus === 'EXPIRED') && (
                            <button
                              className="btn btn-sm btn-outline-dark fw-bold py-1 px-2 text-nowrap"
                              title="Resend invitation email"
                              onClick={() => handleResendInvite(r.residentId, r.email)}
                            >
                              <i className="bi bi-send me-1"></i> Resend Invite
                            </button>
                          )}
                          <button className="btn btn-sm btn-outline-secondary" title="Edit resident details" onClick={() => setEditingResident(r)}>
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button className="btn btn-sm btn-outline-secondary" title="Remove resident" onClick={() => handleDelete(r.residentId)}>
                            <i className="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-muted">No residents found matching criteria.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Invite Resident Modal */}
      {showInviteModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold text-dark">Invite New Resident</h5>
                <button type="button" className="btn-close" onClick={() => setShowInviteModal(false)}></button>
              </div>
              <form onSubmit={handleInviteSubmit}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Select Building *</label>
                    <select
                      className="form-select"
                      value={inviteData.buildingId}
                      onChange={e => setInviteData({...inviteData, buildingId: e.target.value})}
                      required
                    >
                      {buildings.map(b => (
                        <option key={b.buildingId} value={b.buildingId}>{b.buildingName} ({b.buildingNumber})</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Resident Full Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Jane Doe"
                      value={inviteData.name}
                      onChange={e => setInviteData({...inviteData, name: e.target.value})}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Resident Email *</label>
                    <input
                      type="email"
                      className="form-control"
                      placeholder="jane@example.com"
                      value={inviteData.email}
                      onChange={e => setInviteData({...inviteData, email: e.target.value})}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Phone Number *</label>
                    <input
                      type="tel"
                      className="form-control"
                      placeholder="+1 555-4321"
                      value={inviteData.phone}
                      onChange={e => setInviteData({...inviteData, phone: e.target.value})}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Apartment Number *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Apt 4B"
                      value={inviteData.apartmentNumber}
                      onChange={e => setInviteData({...inviteData, apartmentNumber: e.target.value})}
                      required
                    />
                  </div>
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-light" onClick={() => setShowInviteModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-dark px-4 fw-bold">Send Email Invitation</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Edit Resident Modal */}
      {editingResident && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold text-dark">Edit Resident Details</h5>
                <button type="button" className="btn-close" onClick={() => setEditingResident(null)}></button>
              </div>
              <form onSubmit={handleUpdateResident}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Resident Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={editingResident.name}
                      onChange={e => setEditingResident({...editingResident, name: e.target.value})}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Phone</label>
                    <input
                      type="text"
                      className="form-control"
                      value={editingResident.phone}
                      onChange={e => setEditingResident({...editingResident, phone: e.target.value})}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Apartment Number</label>
                    <input
                      type="text"
                      className="form-control"
                      value={editingResident.apartmentNumber}
                      onChange={e => setEditingResident({...editingResident, apartmentNumber: e.target.value})}
                      required
                    />
                  </div>
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-light" onClick={() => setEditingResident(null)}>Cancel</button>
                  <button type="submit" className="btn btn-dark px-4 fw-bold">Save Changes</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
