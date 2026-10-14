import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

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

  // CSV Import Modal State
  const [showCsvModal, setShowCsvModal] = useState(false);
  const [csvCycleId, setCsvCycleId] = useState('');
  const [csvFile, setCsvFile] = useState(null);
  const [validatingCsv, setValidatingCsv] = useState(false);
  const [csvValidation, setCsvValidation] = useState(null);
  const [csvErrorBanner, setCsvErrorBanner] = useState('');
  const [submittingCsv, setSubmittingCsv] = useState(false);
  const [downloadingTemplate, setDownloadingTemplate] = useState(false);

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

  const handleOpenCsvModal = () => {
    if (cycles.length > 0) {
      const openCycle = cycles.find(c => c.status === 'OPEN') || cycles[0];
      setCsvCycleId(openCycle.id);
    }
    setCsvFile(null);
    setCsvValidation(null);
    setCsvErrorBanner('');
    setShowCsvModal(true);
  };

  const handleDownloadTemplate = async () => {
    if (!selectedBuildingId || !csvCycleId) {
      alert('Please select both a building and a target billing cycle first.');
      return;
    }
    setDownloadingTemplate(true);
    try {
      const response = await API.get(
        `/api/bulk-billing/template?buildingId=${selectedBuildingId}&billingCycleId=${csvCycleId}`,
        { responseType: 'blob' }
      );
      const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      const bObj = buildings.find(b => String(b.buildingId) === String(selectedBuildingId));
      const bName = bObj ? bObj.buildingName.replace(/[^a-zA-Z0-9]/g, '_') : 'Building';
      link.setAttribute('download', `Meter_Readings_Template_${bName}_Cycle_${csvCycleId}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to download CSV template.');
    } finally {
      setDownloadingTemplate(false);
    }
  };

  const handleCsvFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.name.toLowerCase().endsWith('.csv')) {
        alert('Please select a valid CSV file (.csv).');
        return;
      }
      setCsvFile(file);
      setCsvValidation(null);
      setCsvErrorBanner('');
    }
  };

  const handleValidateCsv = async () => {
    if (!csvFile) {
      alert('Please select a CSV file to validate.');
      return;
    }
    if (!selectedBuildingId || !csvCycleId) {
      alert('Please select a building and target billing cycle.');
      return;
    }

    setValidatingCsv(true);
    setCsvErrorBanner('');

    const formDataUpload = new FormData();
    formDataUpload.append('file', csvFile);
    formDataUpload.append('buildingId', selectedBuildingId);
    formDataUpload.append('billingCycleId', csvCycleId);

    try {
      const res = await API.post('/api/bulk-billing/validate', formDataUpload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setCsvValidation(res.data);
    } catch (err) {
      console.error(err);
      setCsvErrorBanner(err.response?.data?.message || 'Failed to validate uploaded CSV file. Please verify file format.');
      setCsvValidation(null);
    } finally {
      setValidatingCsv(false);
    }
  };

  const handleSubmitCsvReadings = async () => {
    if (!csvFile || !csvValidation) {
      alert('Please validate the CSV file before submitting.');
      return;
    }
    if (csvValidation.errorCount > 0) {
      alert('Cannot submit readings while validation errors exist. Please fix all errors before submitting.');
      return;
    }

    setSubmittingCsv(true);
    const formDataUpload = new FormData();
    formDataUpload.append('file', csvFile);
    formDataUpload.append('buildingId', selectedBuildingId);
    formDataUpload.append('billingCycleId', csvCycleId);

    try {
      const res = await API.post('/api/bulk-billing/import-readings', formDataUpload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      alert(res.data?.message || 'Meter readings successfully imported!');
      setShowCsvModal(false);
      fetchData(selectedBuildingId);
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || 'Failed to import meter readings from CSV.');
    } finally {
      setSubmittingCsv(false);
    }
  };

  const handleDownloadErrorReport = async () => {
    if (!csvValidation) return;
    try {
      const res = await API.post('/api/bulk-billing/error-report', csvValidation, {
        responseType: 'blob'
      });
      const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Meter_Readings_Errors_Cycle_${csvCycleId}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to download error report.');
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

          <button className="btn btn-outline-dark fw-bold py-2.5 px-3 shadow-sm" onClick={handleOpenCsvModal} id="upload-csv-btn">
            <i className="bi bi-file-earmark-spreadsheet me-1.5"></i> Upload CSV Readings
          </button>

          <button className="btn btn-dark fw-bold py-2.5 px-4 shadow-sm" onClick={handleOpenModal} id="record-manual-btn">
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

      {/* Modal Upload CSV Readings */}
      {showCsvModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1055 }} tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg">
              <div className="modal-header border-bottom pb-3">
                <div>
                  <h5 className="modal-title fw-bold text-dark">
                    <i className="bi bi-file-earmark-spreadsheet text-dark me-2"></i>
                    Upload Meter Readings via CSV
                  </h5>
                  <p className="text-muted small mb-0">
                    Import water meter readings for households in an open billing cycle.
                  </p>
                </div>
                <button type="button" className="btn-close" onClick={() => setShowCsvModal(false)}></button>
              </div>

              <div className="modal-body p-4">
                {/* 1. Target Cycle & Template Download */}
                <div className="row g-3 mb-3">
                  <div className="col-md-7">
                    <label className="form-label fw-semibold fs-7 text-dark">Target Billing Cycle *</label>
                    <select
                      className="form-select fw-bold border"
                      value={csvCycleId}
                      onChange={e => {
                        setCsvCycleId(e.target.value);
                        setCsvValidation(null);
                        setCsvErrorBanner('');
                      }}
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
                  <div className="col-md-5 d-flex align-items-end">
                    <button
                      type="button"
                      className="btn btn-outline-secondary w-100 fw-semibold"
                      onClick={handleDownloadTemplate}
                      disabled={downloadingTemplate || !csvCycleId}
                    >
                      {downloadingTemplate ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                          Downloading...
                        </>
                      ) : (
                        <>
                          <i className="bi bi-download me-1.5"></i> Download CSV Template
                        </>
                      )}
                    </button>
                  </div>
                </div>

                {/* 2. File Selector */}
                <div className="mb-3">
                  <label className="form-label fw-semibold fs-7 text-dark">Select CSV File (.csv) *</label>
                  <div className="input-group">
                    <input
                      type="file"
                      className="form-control border"
                      accept=".csv,text/csv"
                      onChange={handleCsvFileChange}
                    />
                    <button
                      className="btn btn-dark fw-bold px-3"
                      type="button"
                      onClick={handleValidateCsv}
                      disabled={validatingCsv || !csvFile || !csvCycleId}
                    >
                      {validatingCsv ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-1.5" role="status"></span>
                          Validating...
                        </>
                      ) : (
                        <>
                          <i className="bi bi-shield-check me-1.5"></i> Validate & Preview
                        </>
                      )}
                    </button>
                  </div>
                  <div className="form-text fs-8 text-muted">
                    Template columns: <code>resident_id, apartment_number, resident_name, meter_id, meter_number, previous_reading, current_reading</code>.
                    Required to import: <code>resident_id, current_reading</code>.
                  </div>
                </div>

                {/* Error Banner */}
                {csvErrorBanner && (
                  <div className="alert alert-danger d-flex align-items-center mb-3 py-2 px-3 rounded-2" role="alert">
                    <i className="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
                    <div className="small fw-semibold">{csvErrorBanner}</div>
                  </div>
                )}

                {/* 3. Validation Summary Chips */}
                {csvValidation && (
                  <div className="mb-3">
                    <div className="d-flex flex-wrap gap-2 mb-3">
                      <span className="badge bg-light text-dark border px-3 py-2">
                        Total Rows: <strong>{csvValidation.totalRowsInCsv}</strong>
                      </span>
                      <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-2">
                        <i className="bi bi-check-circle me-1"></i> Valid: <strong>{csvValidation.validCount}</strong>
                      </span>
                      {csvValidation.errorCount > 0 && (
                        <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2">
                          <i className="bi bi-x-circle me-1"></i> Errors: <strong>{csvValidation.errorCount}</strong>
                        </span>
                      )}
                      <span className="badge bg-info-subtle text-info-emphasis border border-info-subtle px-3 py-2">
                        Total Consumption: <strong>{csvValidation.totalEstimatedUnits} kL</strong>
                      </span>
                    </div>

                    {/* Preview Table */}
                    <div className="border rounded-2 overflow-hidden" style={{ maxHeight: '240px', overflowY: 'auto' }}>
                      <table className="table table-sm table-hover mb-0 align-middle fs-8">
                        <thead className="table-light sticky-top">
                          <tr>
                            <th>#</th>
                            <th>Resident / Apt</th>
                            <th>Meter #</th>
                            <th>Prev (m³)</th>
                            <th>Current (m³)</th>
                            <th>Units (kL)</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {csvValidation.rows && csvValidation.rows.length > 0 ? (
                            csvValidation.rows.map(row => (
                              <tr key={row.rowNumber} className={row.status !== 'VALID' ? 'table-danger' : ''}>
                                <td className="text-muted">{row.rowNumber}</td>
                                <td className="fw-semibold text-dark">{row.residentName || 'N/A'} ({row.apartmentNumber || 'N/A'})</td>
                                <td className="font-monospace text-muted">{row.meterNumber || 'N/A'}</td>
                                <td>{row.previousReading != null ? row.previousReading : 'N/A'}</td>
                                <td className="fw-bold">{row.currentReading != null ? row.currentReading : 'N/A'}</td>
                                <td><span className="badge bg-light text-dark border">{row.unitsConsumed != null ? row.unitsConsumed : '0'}</span></td>
                                <td>
                                  {row.status === 'VALID' ? (
                                    <span className="badge bg-success-subtle text-success">Valid</span>
                                  ) : (
                                    <span className="badge bg-danger text-white" title={row.errorMessage}>
                                      {row.status}
                                    </span>
                                  )}
                                  {row.errorMessage && (
                                    <div className="text-danger small mt-0.5" style={{ fontSize: '0.72rem' }}>
                                      {row.errorMessage}
                                    </div>
                                  )}
                                </td>
                              </tr>
                            ))
                          ) : (
                            <tr>
                              <td colSpan="7" className="text-center py-3 text-muted">No rows previewed</td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>

                    {csvValidation.errorCount > 0 && (
                      <div className="d-flex justify-content-between align-items-center mt-2">
                        <span className="text-danger small">
                          <i className="bi bi-info-circle me-1"></i> Fix row errors or download error report to inspect details.
                        </span>
                        <button
                          type="button"
                          className="btn btn-outline-danger btn-sm"
                          onClick={handleDownloadErrorReport}
                        >
                          <i className="bi bi-file-earmark-arrow-down me-1"></i> Download Error Report
                        </button>
                      </div>
                    )}
                  </div>
                )}

                {/* Safe Safeguard Notice */}
                <div className="p-2.5 bg-light rounded-2 border text-muted small d-flex align-items-center gap-2">
                  <i className="bi bi-shield-check text-success fs-5"></i>
                  <div>
                    <strong>Safeguard:</strong> Submitting readings saves water consumption records only. The billing cycle will <strong>remain OPEN</strong> and <strong>no bills will be generated</strong>.
                  </div>
                </div>
              </div>

              <div className="modal-footer border-top bg-light">
                <button type="button" className="btn btn-light" onClick={() => setShowCsvModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-dark fw-bold px-4"
                  onClick={handleSubmitCsvReadings}
                  disabled={
                    submittingCsv ||
                    !csvValidation ||
                    csvValidation.errorCount > 0 ||
                    csvValidation.validCount === 0
                  }
                >
                  {submittingCsv ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Submitting Readings...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-check2-circle me-1.5"></i> Submit Readings
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
