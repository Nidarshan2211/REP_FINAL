import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function OwnerHistoryPage() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('ALL');

  const fetchHistory = async () => {
    setLoading(true);
    try {
      let res;
      try {
        res = await API.get('/owners/history');
      } catch (err1) {
        res = await API.get('/owner/history');
      }
      setHistory(res.data || []);
    } catch (err) {
      console.error('Failed to load resident occupancy history', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const joinedCount = history.filter(item => item.activityType === 'JOINED').length;
  const leftCount = history.filter(item => item.activityType === 'LEFT').length;

  const filteredHistory = history.filter(item => {
    const matchesCategory = selectedFilter === 'ALL' || item.activityType === selectedFilter;
    const s = searchTerm.toLowerCase();
    const matchesSearch =
      (item.residentName && item.residentName.toLowerCase().includes(s)) ||
      (item.residentEmail && item.residentEmail.toLowerCase().includes(s)) ||
      (item.buildingName && item.buildingName.toLowerCase().includes(s)) ||
      (item.apartmentName && item.apartmentName.toLowerCase().includes(s)) ||
      (item.roomNumber && item.roomNumber.toLowerCase().includes(s)) ||
      (item.details && item.details.toLowerCase().includes(s)) ||
      (item.actor && item.actor.toLowerCase().includes(s));
    return matchesCategory && matchesSearch;
  });

  const getActionBadge = (type) => {
    if (type === 'JOINED') {
      return (
        <span className="badge bg-success-subtle text-success border border-success-subtle px-2.5 py-1.5 rounded-pill fs-8 fw-semibold d-inline-flex align-items-center gap-1.5">
          <i className="bi bi-door-open-fill"></i>
          <span>Moved In</span>
        </span>
      );
    }
    return (
      <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-2.5 py-1.5 rounded-pill fs-8 fw-semibold d-inline-flex align-items-center gap-1.5">
        <i className="bi bi-box-arrow-right"></i>
        <span>Moved Out / Left</span>
      </span>
    );
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      const d = new Date(dateStr);
      return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="container-fluid py-4">
      {/* Page Header */}
      <div className="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
        <div>
          <h2 className="fw-bold mb-1 text-dark">Resident Occupancy History</h2>
          <p className="text-muted small mb-0">
            Track chronological activity of residents moving into and moving out of apartments across your buildings
          </p>
        </div>
        <div className="d-flex align-items-center gap-2">
          <button
            type="button"
            className="btn btn-outline-dark btn-sm px-3 py-1.5 fw-semibold d-flex align-items-center gap-1.5"
            onClick={fetchHistory}
            disabled={loading}
          >
            <i className={`bi bi-arrow-clockwise ${loading ? 'spin' : ''}`}></i>
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Stats Summary Cards */}
      <div className="row g-3 mb-4">
        <div className="col-sm-6 col-md-4">
          <div className="card glass-card border shadow-xs bg-white p-3 rounded-3">
            <div className="d-flex align-items-center justify-content-between">
              <div>
                <div className="text-muted fs-8 text-uppercase fw-bold">Total Movement Records</div>
                <div className="fs-3 fw-bold text-dark mt-1">{history.length}</div>
              </div>
              <div className="rounded-3 bg-light border p-2.5 text-dark fs-4">
                <i className="bi bi-clock-history"></i>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-4">
          <div className="card glass-card border shadow-xs bg-white p-3 rounded-3">
            <div className="d-flex align-items-center justify-content-between">
              <div>
                <div className="text-success fs-8 text-uppercase fw-bold">Residents Moved In</div>
                <div className="fs-3 fw-bold text-success mt-1">{joinedCount}</div>
              </div>
              <div className="rounded-3 bg-success-subtle border border-success-subtle p-2.5 text-success fs-4">
                <i className="bi bi-door-open-fill"></i>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-4">
          <div className="card glass-card border shadow-xs bg-white p-3 rounded-3">
            <div className="d-flex align-items-center justify-content-between">
              <div>
                <div className="text-danger fs-8 text-uppercase fw-bold">Residents Moved Out / Left</div>
                <div className="fs-3 fw-bold text-danger mt-1">{leftCount}</div>
              </div>
              <div className="rounded-3 bg-danger-subtle border border-danger-subtle p-2.5 text-danger fs-4">
                <i className="bi bi-box-arrow-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filters & Search Toolbar */}
      <div className="card glass-card border shadow-xs bg-white p-3 rounded-3 mb-4">
        <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div className="btn-group" role="group">
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'ALL' ? 'btn-dark' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('ALL')}
            >
              All Activity ({history.length})
            </button>
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'JOINED' ? 'btn-success' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('JOINED')}
            >
              Moved In ({joinedCount})
            </button>
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'LEFT' ? 'btn-danger' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('LEFT')}
            >
              Moved Out / Left ({leftCount})
            </button>
          </div>

          <div className="input-group" style={{ maxWidth: '340px' }}>
            <span className="input-group-text bg-light border-end-0">
              <i className="bi bi-search text-muted"></i>
            </span>
            <input
              type="text"
              className="form-control form-control-sm border-start-0"
              placeholder="Search resident, room, building..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button
                className="btn btn-sm btn-outline-secondary border-start-0"
                type="button"
                onClick={() => setSearchTerm('')}
              >
                <i className="bi bi-x"></i>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* History Table */}
      <div className="card glass-card border shadow-xs bg-white rounded-3 overflow-hidden">
        {loading ? (
          <div className="p-5 text-center">
            <LoadingSpinner text="Loading occupancy movements..." />
          </div>
        ) : filteredHistory.length === 0 ? (
          <div className="p-5 text-center text-muted">
            <div className="mb-3">
              <i className="bi bi-journal-x fs-1 text-secondary opacity-50"></i>
            </div>
            <h5 className="fw-semibold text-dark mb-1">No Activity Records Found</h5>
            <p className="small mb-0">
              {searchTerm || selectedFilter !== 'ALL'
                ? 'Try adjusting your search criteria or filter tags.'
                : 'No resident movements recorded yet for your buildings.'}
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-uppercase fs-8 text-muted">
                <tr>
                  <th className="py-3 px-3">Movement Type</th>
                  <th className="py-3 px-3">Resident</th>
                  <th className="py-3 px-3">Apartment & Building</th>
                  <th className="py-3 px-3">Activity Details</th>
                  <th className="py-3 px-3">Initiator / Actor</th>
                  <th className="py-3 px-3">Date & Time</th>
                </tr>
              </thead>
              <tbody className="fs-7">
                {filteredHistory.map((item) => (
                  <tr key={item.id}>
                    <td className="py-3 px-3">
                      {getActionBadge(item.activityType)}
                    </td>
                    <td className="py-3 px-3">
                      <div className="fw-bold text-dark">{item.residentName}</div>
                      <div className="text-muted fs-8">
                        {item.residentEmail}
                        {item.residentPhone ? ` • ${item.residentPhone}` : ''}
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <div className="fw-semibold text-dark">
                        {item.apartmentName || (item.roomNumber ? `Flat ${item.roomNumber}` : 'Apartment')}
                      </div>
                      <div className="text-muted fs-8">
                        <i className="bi bi-building me-1"></i>
                        {item.buildingName}
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <div className="text-dark small" style={{ maxWidth: '320px' }}>
                        {item.details}
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <span className="badge bg-light text-dark border px-2 py-1 fs-8">
                        <i className="bi bi-person me-1"></i>
                        {item.actor || 'System'}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-nowrap text-muted small">
                      <i className="bi bi-calendar3 me-1.5 opacity-75"></i>
                      {formatDate(item.timestamp)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
