import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function AdminOwnersPage() {
  const [owners, setOwners] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchOwners = () => {
    API.get('/admin/owners')
      .then(res => setOwners(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOwners();
  }, []);

  const handleDeleteOwner = async (ownerId, ownerName) => {
    if (window.confirm(`Are you sure you want to remove building owner "${ownerName}" and all their associated properties from the platform? This action cannot be undone.`)) {
      try {
        await API.delete(`/admin/owners/${ownerId}`);
        setOwners(prev => prev.filter(o => o.ownerId !== ownerId));
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete building owner');
      }
    }
  };

  const filteredOwners = owners.filter(o => 
    o.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    o.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return <LoadingSpinner text="Loading Building Owners..." />;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
        <div>
          <h2 className="fw-bold mb-1 text-dark">Building Owners Directory</h2>
          <p className="text-muted small mb-0">Manage all registered building owner accounts</p>
        </div>
        <div style={{ width: 300 }}>
          <div className="input-group">
            <span className="input-group-text bg-light border-end-0"><i className="bi bi-search"></i></span>
            <input 
              type="text" 
              className="form-control border-start-0 ps-0 bg-light" 
              placeholder="Search by name or email..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </div>

      <div className="card glass-card border shadow-sm bg-white">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Managed Buildings</th>
                  <th>Status</th>
                  <th>Registered On</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredOwners.length ? (
                  filteredOwners.map(o => (
                    <tr key={o.ownerId}>
                      <td><code className="text-dark fw-bold">#{o.ownerId}</code></td>
                      <td className="fw-bold text-dark">{o.name}</td>
                      <td className="text-muted">{o.email}</td>
                      <td className="text-muted">{o.phone}</td>
                      <td>
                        <span className="badge bg-secondary-subtle text-dark border px-2.5 py-1">
                          {o.totalBuildings} Buildings
                        </span>
                      </td>
                      <td>
                        {o.approvalStatus === 'APPROVED' && (
                          <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-1.5 rounded-pill fw-semibold fs-7">
                            <i className="bi bi-check-circle-fill me-1"></i> APPROVED
                          </span>
                        )}
                        {o.approvalStatus === 'PENDING' && (
                          <span className="badge bg-warning-subtle text-dark border border-warning-subtle px-3 py-1.5 rounded-pill fw-semibold fs-7">
                            <i className="bi bi-clock-fill text-warning me-1"></i> PENDING
                          </span>
                        )}
                        {o.approvalStatus === 'REJECTED' && (
                          <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-1.5 rounded-pill fw-semibold fs-7">
                            <i className="bi bi-x-circle-fill me-1"></i> REJECTED
                          </span>
                        )}
                      </td>
                      <td className="text-muted small">{o.createdAt ? o.createdAt.substring(0, 10) : 'N/A'}</td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-danger px-2.5 py-1 fw-semibold rounded-2 d-flex align-items-center gap-1"
                          title="Delete and remove this building owner"
                          onClick={() => handleDeleteOwner(o.ownerId, o.name)}
                        >
                          <i className="bi bi-trash"></i>
                          <span>Delete</span>
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">No owners matched your search query.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
