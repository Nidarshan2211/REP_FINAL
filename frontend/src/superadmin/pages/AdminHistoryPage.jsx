import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function AdminHistoryPage() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('ALL');

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const res = await API.get('/admin/history');
      setHistory(res.data || []);
    } catch (err) {
      console.error('Failed to load owner history', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const joinedCount = history.filter(item => item.category === 'JOINED').length;
  const removedCount = history.filter(item => item.category === 'REMOVED').length;

  const filteredHistory = history.filter(item => {
    const matchesCategory = selectedFilter === 'ALL' || item.category === selectedFilter;
    const matchesSearch = 
      item.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.actor?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const getEventBadge = (category) => {
    if (category === 'JOINED') {
      return (
        <span className="badge bg-success-subtle text-success border border-success-subtle px-2.5 py-1.5 rounded-pill fs-8 fw-semibold">
          <i className="bi bi-person-check-fill me-1"></i> Owner Joined
        </span>
      );
    }
    return (
      <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-2.5 py-1.5 rounded-pill fs-8 fw-semibold">
        <i className="bi bi-person-x-fill me-1"></i> Owner Removed
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
          <h2 className="fw-bold mb-1 text-dark">Building Owner History</h2>
          <p className="text-muted small mb-0">Track chronological history of building owners joining and being removed from the platform</p>
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
                <div className="text-muted fs-8 text-uppercase fw-bold">Total History Records</div>
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
                <div className="text-muted fs-8 text-uppercase fw-bold">Owners Joined</div>
                <div className="fs-3 fw-bold text-success mt-1">{joinedCount}</div>
              </div>
              <div className="rounded-3 bg-success-subtle text-success border border-success-subtle p-2.5 fs-4">
                <i className="bi bi-person-check-fill"></i>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-4">
          <div className="card glass-card border shadow-xs bg-white p-3 rounded-3">
            <div className="d-flex align-items-center justify-content-between">
              <div>
                <div className="text-muted fs-8 text-uppercase fw-bold">Owners Removed</div>
                <div className="fs-3 fw-bold text-danger mt-1">{removedCount}</div>
              </div>
              <div className="rounded-3 bg-danger-subtle text-danger border border-danger-subtle p-2.5 fs-4">
                <i className="bi bi-person-x-fill"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="card glass-card border shadow-sm bg-white mb-4 p-3">
        <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
          {/* Action Filter Pills */}
          <div className="btn-group" role="group">
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'ALL' ? 'btn-dark fw-bold' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('ALL')}
            >
              All Records ({history.length})
            </button>
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'JOINED' ? 'btn-success fw-bold text-white' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('JOINED')}
            >
              <i className="bi bi-person-check-fill me-1"></i>Joined ({joinedCount})
            </button>
            <button
              type="button"
              className={`btn btn-sm ${selectedFilter === 'REMOVED' ? 'btn-danger fw-bold text-white' : 'btn-outline-secondary'}`}
              onClick={() => setSelectedFilter('REMOVED')}
            >
              <i className="bi bi-person-x-fill me-1"></i>Removed / Left ({removedCount})
            </button>
          </div>

          {/* Search Box */}
          <div style={{ minWidth: 260 }}>
            <div className="input-group input-group-sm">
              <span className="input-group-text bg-light border-end-0"><i className="bi bi-search"></i></span>
              <input
                type="text"
                className="form-control border-start-0 bg-light"
                placeholder="Search by owner name or details..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
              {searchTerm && (
                <button className="btn btn-outline-secondary bg-light border-start-0" onClick={() => setSearchTerm('')}>
                  <i className="bi bi-x"></i>
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* History Log Table */}
      {loading ? (
        <LoadingSpinner text="Retrieving building owner lifecycle history..." />
      ) : (
        <div className="card glass-card border shadow-sm bg-white">
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table custom-table mb-0 align-middle">
                <thead>
                  <tr>
                    <th style={{ width: 160 }}>Action</th>
                    <th>Building Owner</th>
                    <th>Activity Details</th>
                    <th>Recorded By</th>
                    <th style={{ width: 180 }}>Date & Time</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredHistory.length > 0 ? (
                    filteredHistory.map((item) => (
                      <tr key={item.id}>
                        <td>{getEventBadge(item.category)}</td>
                        <td>
                          <div className="d-flex align-items-center gap-2">
                            <div 
                              className={`rounded-circle d-flex align-items-center justify-content-center text-white fw-bold fs-8 ${
                                item.category === 'JOINED' ? 'bg-primary' : 'bg-danger'
                              }`}
                              style={{ width: 32, height: 32 }}
                            >
                              {item.actor ? item.actor.charAt(0).toUpperCase() : 'O'}
                            </div>
                            <div>
                              <strong className="text-dark fs-7 d-block">{item.title?.replace('Building Owner Joined: ', '')?.replace('Building Owner Removed: ', '')}</strong>
                              <span className="text-muted fs-9"><code>{item.id}</code></span>
                            </div>
                          </div>
                        </td>
                        <td className="text-muted fs-8" style={{ maxWidth: 420 }}>
                          {item.description}
                        </td>
                        <td>
                          <span className="badge bg-light text-dark border px-2 py-1 fs-8">
                            <i className="bi bi-shield-lock me-1 text-primary"></i>{item.actor || 'System Super Admin'}
                          </span>
                        </td>
                        <td className="text-muted fs-8">
                          <i className="bi bi-clock me-1"></i>
                          {formatDate(item.timestamp)}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" className="text-center py-5 text-muted">
                        <i className="bi bi-person-lines-fill display-6 d-block mb-2 text-muted opacity-50"></i>
                        No building owner history records found matching your filter.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
