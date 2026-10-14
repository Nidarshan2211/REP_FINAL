import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function AdminPendingOwnersPage() {
  const [pendingOwners, setPendingOwners] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchPending = async () => {
    try {
      const res = await API.get('/admin/owners/pending');
      setPendingOwners(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, []);

  const handleApprove = async (id) => {
    try {
      await API.put(`/admin/owners/${id}/approve`);
      fetchPending();
    } catch (err) {
      alert('Approval failed');
    }
  };

  const handleReject = async (id) => {
    if (window.confirm('Reject this owner application?')) {
      try {
        await API.put(`/admin/owners/${id}/reject`);
        fetchPending();
      } catch (err) {
        alert('Rejection failed');
      }
    }
  };

  if (loading) return <LoadingSpinner text="Fetching pending approvals..." />;

  return (
    <div className="container-fluid py-4">
      <div className="mb-4">
        <h2 className="fw-bold mb-1 text-dark">Pending Owner Registrations</h2>
        <p className="text-muted small mb-0">Review and approve new Building Owner registrations</p>
      </div>

      <div className="card glass-card border shadow-sm bg-white">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Owner Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Initial Buildings</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pendingOwners.length ? (
                  pendingOwners.map(owner => (
                    <tr key={owner.ownerId}>
                      <td className="fw-bold text-dark">{owner.name}</td>
                      <td className="text-muted">{owner.email}</td>
                      <td className="text-muted">{owner.phone}</td>
                      <td>
                        <span className="badge bg-secondary-subtle text-dark border px-2.5 py-1">
                          {owner.totalBuildings} Building(s)
                        </span>
                      </td>
                      <td>
                        <span className="badge bg-warning-subtle text-dark border border-warning-subtle px-3 py-1.5 rounded-pill fw-semibold fs-7">
                          <i className="bi bi-clock-fill text-warning me-1"></i> PENDING APPROVAL
                        </span>
                      </td>
                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <button
                            className="btn btn-sm btn-dark px-3 py-1.5 fw-semibold rounded-2 shadow-sm"
                            onClick={() => handleApprove(owner.ownerId)}
                          >
                            <i className="bi bi-check2 me-1"></i> Approve
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger px-3 py-1.5 fw-semibold rounded-2"
                            onClick={() => handleReject(owner.ownerId)}
                          >
                            <i className="bi bi-x-lg me-1"></i> Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center py-5 text-muted">
                      <i className="bi bi-check2-all text-dark fs-1 d-block mb-2"></i>
                      All owner registrations have been reviewed! No pending approvals.
                    </td>
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
