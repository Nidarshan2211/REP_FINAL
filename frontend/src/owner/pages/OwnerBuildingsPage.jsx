import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function OwnerBuildingsPage() {
  const [buildings, setBuildings] = useState([]);
  const [loading, setLoading] = useState(true);

  const [formData, setFormData] = useState({
    buildingName: '',
    buildingNumber: '',
    totalFlats: 10,
    location: ''
  });
  const [showModal, setShowModal] = useState(false);

  const fetchBuildings = () => {
    API.get('/owners/buildings')
      .then(res => setBuildings(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchBuildings();
  }, []);

  const handleCreateBuilding = async (e) => {
    e.preventDefault();
    try {
      await API.post('/owners/buildings', formData);
      setShowModal(false);
      setFormData({ buildingName: '', buildingNumber: '', totalFlats: 10, location: '' });
      fetchBuildings();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create building');
    }
  };

  if (loading) return <LoadingSpinner text="Fetching buildings..." />;

  return (
    <div className="container-fluid py-4">
      <div className="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
        <div>
          <h2 className="fw-bold mb-1 text-dark">Building Directory</h2>
          <p className="text-muted small mb-0">Manage properties, flats, and meter installation scopes</p>
        </div>
        <button className="btn btn-dark fw-bold" onClick={() => setShowModal(true)}>
          <i className="bi bi-plus-lg me-1.5"></i> Add New Building
        </button>
      </div>

      <div className="row g-4">
        {buildings.map(b => (
          <div className="col-md-6 col-lg-4" key={b.buildingId}>
            <div className="card glass-card border shadow-sm h-100 p-3 bg-white">
              <div className="card-body">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="rounded-3 bg-light border p-2.5 text-dark fs-4">
                    <i className="bi bi-building"></i>
                  </div>
                  <span className="badge bg-secondary-subtle text-dark border fw-bold">{b.buildingNumber}</span>
                </div>
                <h4 className="fw-bold text-dark mb-1">{b.buildingName}</h4>
                <p className="text-muted small mb-3"><i className="bi bi-geo-alt me-1"></i> {b.location}</p>
                <div className="border-top pt-3 d-flex justify-content-between text-muted fs-7">
                  <span>Total Flats: <strong className="text-dark">{b.totalFlats}</strong></span>
                  <span>Registered: <strong className="text-dark">{b.createdAt ? b.createdAt.substring(0, 10) : 'N/A'}</strong></span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Modal Add Building */}
      {showModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold text-dark">Register New Building</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleCreateBuilding}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Building Name *</label>
                    <input type="text" className="form-control" placeholder="Emerald Heights" value={formData.buildingName} onChange={e => setFormData({...formData, buildingName: e.target.value})} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Building Number/Code *</label>
                    <input type="text" className="form-control" placeholder="BLDG-02" value={formData.buildingNumber} onChange={e => setFormData({...formData, buildingNumber: e.target.value})} required />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Total Flats *</label>
                    <input type="number" className="form-control" value={formData.totalFlats} onChange={e => setFormData({...formData, totalFlats: parseInt(e.target.value)})} required min={1} />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Location Address *</label>
                    <input type="text" className="form-control" placeholder="742 Ocean View Blvd" value={formData.location} onChange={e => setFormData({...formData, location: e.target.value})} required />
                  </div>
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-dark px-4 fw-bold">Create Building</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
