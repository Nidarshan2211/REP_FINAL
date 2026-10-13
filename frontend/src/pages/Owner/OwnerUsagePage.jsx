import React, { useState, useEffect } from 'react';
import API from '../../services/api';
import LoadingSpinner from '../../components/LoadingSpinner';

export default function OwnerUsagePage() {
  const [usageRecords, setUsageRecords] = useState([]);
  const [meters, setMeters] = useState([]);
  const [cycles, setCycles] = useState([]);
  const [buildings, setBuildings] = useState([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState('');
  const [loading, setLoading] = useState(true);

  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    meterId: '',
    billingCycleId: '',
    previousReading: 0,
    currentReading: 0
  });

  // Fetch Buildings on mount
  useEffect(() => {
    API.get('/owners/buildings')
      .then(res => {
        const bList = res.data || [];
        setBuildings(bList);
        if (bList.length > 0) {
          setSelectedBuildingId(bList[0].buildingId);
        }
      })
      .catch(err => console.error('Failed to load buildings', err));
  }, []);

  const fetchData = (bId) => {
    setLoading(true);
    Promise.all([
      API.get('/usage/records'),
      API.get('/usage/meters').catch(() => API.get('/owners/residents')),
      bId ? API.get(`/api/owner/billing-cycles?buildingId=${bId}`).catch(() => API.get('/api/billing-cycles')) : Promise.resolve({ data: [] })
    ]).then(([usageRes, meterRes, cycleRes]) => {
      setUsageRecords(usageRes.data || []);
      const mList = meterRes.data || [];
      setMeters(mList);
      const cList = cycleRes.data || [];
      setCycles(cList);

      if (mList.length > 0 && !formData.meterId) {
        const firstM = mList[0];
        setFormData(prev => ({
          ...prev,
          meterId: firstM.meterId || firstM.residentId,
          previousReading: firstM.autoPreviousReading || 0
        }));
      }
      if (cList.length > 0 && !formData.billingCycleId) {
        setFormData(prev => ({ ...prev, billingCycleId: cList[0].id }));
      }
    }).catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData(selectedBuildingId);
  }, [selectedBuildingId]);

  const handleMeterChange = (e) => {
    const selectedId = e.target.value;
    const m = meters.find(item => String(item.meterId || item.residentId) === String(selectedId));
    setFormData(prev => ({
      ...prev,
      meterId: selectedId,
      previousReading: m ? (m.autoPreviousReading || 0) : prev.previousReading
    }));
  };

  const handleOpenModal = () => {
    if (meters.length > 0) {
      const firstM = meters[0];
      setFormData(prev => ({
        ...prev,
        meterId: firstM.meterId || firstM.residentId,
        previousReading: firstM.autoPreviousReading || 0,
        billingCycleId: cycles.length > 0 ? cycles[0].id : ''
      }));
    }
    setShowModal(true);
  };

  const handleRecordSubmit = async (e) => {
    e.preventDefault();
    if (!formData.meterId) {
      alert('Please select a resident meter!');
      return;
    }
    if (formData.currentReading < formData.previousReading) {
      alert('Validation Error: Current reading cannot be less than previous reading!');
      return;
    }

    try {
      await API.post('/usage/record', formData);
      setShowModal(false);
      fetchData(selectedBuildingId);
      alert('Water usage reading logged successfully!');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to record usage');
    }
  };

  if (loading) return <LoadingSpinner text="Fetching water meter readings..." />;

  const calculatedUnits = Math.max(0, (formData.currentReading || 0) - (formData.previousReading || 0));

  return (
    <div className="container-fluid py-4">
      <div className="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
        <div>
          <h2 className="fw-bold mb-1 text-dark">Water Meter Consumption Logs</h2>
          <p className="text-muted small mb-0">Digital logbook to record household readings per billing cycle</p>
        </div>

        <div className="d-flex align-items-center gap-3">
          {buildings.length > 0 && (
            <div className="d-flex align-items-center bg-white p-2 rounded-3 shadow-sm border">
              <i className="bi bi-building text-dark me-2 ms-2 fs-5"></i>
              <select
                className="form-select border-0 bg-light fw-bold"
                value={selectedBuildingId}
                onChange={e => setSelectedBuildingId(e.target.value)}
              >
                {buildings.map(b => (
                  <option key={b.buildingId} value={b.buildingId}>
                    {b.buildingName} ({b.buildingNumber})
                  </option>
                ))}
              </select>
            </div>
          )}

          <button className="btn btn-dark fw-bold py-2.5 px-4" onClick={handleOpenModal}>
            <i className="bi bi-plus-circle me-1.5"></i> Record Meter Reading
          </button>
        </div>
      </div>

      <div className="card glass-card border shadow-sm bg-white">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Meter Number</th>
                  <th>Resident</th>
                  <th>Month / Cycle</th>
                  <th>Previous Reading</th>
                  <th>Current Reading</th>
                  <th>Units Consumed</th>
                  <th>Leak Status</th>
                  <th>Recorded At</th>
                </tr>
              </thead>
              <tbody>
                {usageRecords.length ? (
                  usageRecords.map(u => (
                    <tr key={u.usageId}>
                      <td><span className="font-monospace text-dark fw-bold">{u.meterNumber}</span></td>
                      <td className="fw-bold text-dark">{u.residentName}</td>
                      <td><span className="text-muted fw-semibold">{u.month}/{u.year}</span></td>
                      <td className="text-muted">{u.previousReading} m³</td>
                      <td className="text-dark fw-semibold">{u.currentReading} m³</td>
                      <td><span className="fw-bold text-dark fs-6">{u.unitsConsumed} kL</span></td>
                      <td>
                        {u.anomalyStatus === 'POTENTIAL_LEAK' ? (
                          <span className="badge px-2.5 py-1 rounded-2 fw-semibold fs-8" style={{ backgroundColor: '#FEF2F2', color: '#991B1B', border: '1px solid #FCA5A5' }}>
                            <i className="bi bi-exclamation-triangle-fill me-1" style={{ color: '#DC2626' }}></i> Potential Leak
                          </span>
                        ) : u.anomalyStatus === 'INSUFFICIENT_HISTORY' ? (
                          <span className="badge px-2.5 py-1 rounded-2 fw-semibold fs-8" style={{ backgroundColor: '#F8FAFC', color: '#475569', border: '1px solid #CBD5E1' }}>
                            <i className="bi bi-info-circle-fill me-1" style={{ color: '#64748B' }}></i> Insufficient History
                          </span>
                        ) : (
                          <span className="badge px-2.5 py-1 rounded-2 fw-semibold fs-8" style={{ backgroundColor: '#F0FDF4', color: '#166534', border: '1px solid #86EFAC' }}>
                            <i className="bi bi-check-circle-fill me-1" style={{ color: '#16A34A' }}></i> Normal
                          </span>
                        )}
                      </td>
                      <td className="text-muted small">{u.recordedAt ? u.recordedAt.substring(0, 10) : 'N/A'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">No meter reading logs recorded yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Modal Record Reading */}
      {showModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg">
              <div className="modal-header border-0 pb-0">
                <h5 className="modal-title fw-bold text-dark">Record Water Meter Reading</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <form onSubmit={handleRecordSubmit}>
                <div className="modal-body">
                  {/* Select Billing Cycle */}
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Target Billing Cycle *</label>
                    <select
                      className="form-select fw-bold border"
                      value={formData.billingCycleId}
                      onChange={e => setFormData({...formData, billingCycleId: e.target.value})}
                      required
                    >
                      <option value="">-- Choose Billing Cycle --</option>
                      {cycles.map(c => (
                        <option key={c.id} value={c.id}>
                          Cycle #{c.id}: {c.startDate} to {c.endDate} ({c.status})
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Select Resident / Meter */}
                  <div className="mb-3">
                    <label className="form-label fw-semibold fs-7 text-dark">Select Resident / Household Meter *</label>
                    <select
                      className="form-select fw-bold"
                      value={formData.meterId}
                      onChange={handleMeterChange}
                      required
                    >
                      <option value="">-- Choose Household Meter --</option>
                      {meters.map(m => (
                        <option key={m.meterId || m.residentId} value={m.meterId || m.residentId}>
                          {m.residentName || m.name} - Apt {m.apartmentNumber} ({m.meterNumber || 'WM-' + m.apartmentNumber})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label fw-semibold fs-7 text-dark">Previous Reading (m³) *</label>
                      <input
                        type="number"
                        step="0.1"
                        min="0"
                        className="form-control fw-bold"
                        value={formData.previousReading}
                        onChange={e => setFormData({...formData, previousReading: parseFloat(e.target.value || 0)})}
                        required
                      />
                      <small className="text-muted fs-8">Auto-filled from last reading</small>
                    </div>
                    <div className="col-6">
                      <label className="form-label fw-semibold fs-7 text-dark">Current Reading (m³) *</label>
                      <input
                        type="number"
                        step="0.1"
                        min="0"
                        className="form-control fw-bold border"
                        value={formData.currentReading}
                        onChange={e => setFormData({...formData, currentReading: parseFloat(e.target.value || 0)})}
                        required
                      />
                    </div>
                  </div>

                  {/* Immediate Live Unit Display */}
                  <div className="p-3 bg-light rounded text-center border">
                    <small className="text-muted fw-semibold">Units Consumed (Calculated):</small>
                    <div className="fw-extrabold fs-3 text-dark mt-1">
                      {calculatedUnits.toFixed(1)} kL
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-dark px-4 fw-bold">Save Usage Log</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
