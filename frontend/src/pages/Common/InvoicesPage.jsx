import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../../services/api';
import LoadingSpinner from '../../components/LoadingSpinner';
import { AuthContext } from '../../context/AuthContext';

export default function InvoicesPage() {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [buildings, setBuildings] = useState([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState('');
  const [cycles, setCycles] = useState([]);
  const [selectedCycleId, setSelectedCycleId] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  const [invoicesSummary, setInvoicesSummary] = useState({
    totalBilled: 0,
    totalPaid: 0,
    totalUnpaid: 0,
    averageBilled: 0,
    totalInvoices: 0,
    paidInvoices: 0,
    unpaidInvoices: 0,
    invoices: []
  });

  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);
  const [selectedInvoice, setSelectedInvoice] = useState(null); // For Itemized Drawer/Modal

  // 1. Fetch buildings list on mount
  useEffect(() => {
    API.get('/owners/buildings')
      .then(res => {
        const bList = res.data || [];
        setBuildings(bList);
        if (bList.length > 0) {
          setSelectedBuildingId(String(bList[0].buildingId));
        }
      })
      .catch(err => {
        console.error('Failed to load buildings', err);
      });
  }, []);

  // 2. Fetch billing cycles when building changes
  useEffect(() => {
    if (selectedBuildingId) {
      API.get(`/billing-cycles?buildingId=${selectedBuildingId}`)
        .then(res => {
          const cList = res.data || [];
          setCycles(cList);
          // Default to All Cycles initially or first cycle
          setSelectedCycleId('');
        })
        .catch(() => {
          setCycles([]);
          setSelectedCycleId('');
        });
    } else {
      setCycles([]);
      setSelectedCycleId('');
    }
  }, [selectedBuildingId]);

  // 3. Fetch Invoices summary & list whenever filters change
  const fetchInvoices = () => {
    setLoading(true);
    const params = new URLSearchParams();
    if (selectedBuildingId) params.append('buildingId', selectedBuildingId);
    if (selectedCycleId) params.append('billingCycleId', selectedCycleId);
    if (selectedStatus && selectedStatus !== 'ALL') params.append('status', selectedStatus);
    if (searchTerm) params.append('search', searchTerm);

    API.get(`/invoices?${params.toString()}`)
      .then(res => {
        setInvoicesSummary(res.data || {
          totalBilled: 0,
          totalPaid: 0,
          totalUnpaid: 0,
          averageBilled: 0,
          totalInvoices: 0,
          paidInvoices: 0,
          unpaidInvoices: 0,
          invoices: []
        });
      })
      .catch(err => {
        console.error('Error fetching invoices:', err);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchInvoices();
  }, [selectedBuildingId, selectedCycleId, selectedStatus, searchTerm]);

  // Toggle invoice status (PAID <-> UNPAID)
  const handleToggleStatus = async (invoice) => {
    const newStatus = invoice.status === 'PAID' ? 'UNPAID' : 'PAID';
    setUpdatingId(invoice.billId);
    try {
      await API.put(`/bills/${invoice.billId}/status`, { status: newStatus });
      fetchInvoices();
      if (selectedInvoice && selectedInvoice.billId === invoice.billId) {
        setSelectedInvoice({ ...selectedInvoice, status: newStatus });
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update invoice payment status.');
    } finally {
      setUpdatingId(null);
    }
  };

  // Download PDF statement
  const handleDownloadPdf = async (billId) => {
    setDownloadingId(billId);
    try {
      const response = await API.get(`/bills/${billId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Water_Invoice_Statement_SWB_${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to download PDF statement.');
    } finally {
      setDownloadingId(null);
    }
  };

  const invoices = invoicesSummary?.invoices || [];

  return (
    <div className="container-fluid py-4">
      {/* Header Bar */}
      <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-3">
        <div>
          <button
            onClick={() => navigate(-1)}
            className="btn btn-outline-secondary btn-sm rounded-pill px-3 py-1 mb-2 d-inline-flex align-items-center gap-1 shadow-sm bg-white"
          >
            <i className="bi bi-arrow-left"></i> Back
          </button>
          <h2 className="fw-bold mb-1 text-dark">Invoices</h2>
          <p className="text-muted small mb-0">View, filter, and adjust household water bills</p>
        </div>
      </div>

      {/* Top Banner Card */}
      <div
        className="card border-0 shadow-sm mb-4 rounded-4 text-white overflow-hidden"
        style={{
          background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #6366f1 100%)',
          padding: '1.25rem 1.5rem'
        }}
      >
        <div className="d-flex align-items-center gap-3">
          <div
            className="rounded-4 d-flex align-items-center justify-content-center shadow"
            style={{
              width: '54px',
              height: '54px',
              backgroundColor: 'rgba(255, 255, 255, 0.2)',
              backdropFilter: 'blur(8px)',
              fontSize: '1.75rem'
            }}
          >
            <i className="bi bi-receipt-cutoff text-white"></i>
          </div>
          <div>
            <h4 className="fw-bold mb-0 text-white">Invoices</h4>
            <p className="mb-0 text-white-50 small">Track payment statuses and download PDF statements</p>
          </div>
        </div>
      </div>

      {/* Filter Card */}
      <div className="card border-0 shadow-sm mb-4 rounded-4 bg-white p-3">
        <div className="row g-3 align-items-center">
          {/* Apartment Selector */}
          <div className="col-12 col-md-3">
            <label className="form-label text-muted text-uppercase fw-bold fs-8 mb-1">
              <i className="bi bi-building me-1"></i> Apartment
            </label>
            <select
              className="form-select border rounded-3 fw-medium text-dark"
              value={selectedBuildingId}
              onChange={(e) => setSelectedBuildingId(e.target.value)}
            >
              <option value="">All Apartments</option>
              {buildings.map((b) => (
                <option key={b.buildingId} value={b.buildingId}>
                  {b.buildingName} ({b.buildingNumber})
                </option>
              ))}
            </select>
          </div>

          {/* Billing Cycle Selector */}
          <div className="col-12 col-md-3">
            <label className="form-label text-muted text-uppercase fw-bold fs-8 mb-1">
              <i className="bi bi-calendar-event me-1"></i> Billing Cycle
            </label>
            <select
              className="form-select border rounded-3 fw-medium text-dark"
              value={selectedCycleId}
              onChange={(e) => setSelectedCycleId(e.target.value)}
            >
              <option value="">All Billing Cycles</option>
              {cycles.length > 0 ? (
                cycles.map((c) => (
                  <option key={c.id} value={c.id}>
                    Cycle #{c.id}: {c.startDate} to {c.endDate} ({c.status})
                  </option>
                ))
              ) : (
                <option value="" disabled>No billing cycles found</option>
              )}
            </select>
          </div>

          {/* Search Box */}
          <div className="col-12 col-md-4">
            <label className="form-label text-muted text-uppercase fw-bold fs-8 mb-1">
              <i className="bi bi-search me-1"></i> Search
            </label>
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control border-start-0 ps-0"
                placeholder="Search resident, apartment, or invoice..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          {/* Status Filter */}
          <div className="col-12 col-md-2">
            <label className="form-label text-muted text-uppercase fw-bold fs-8 mb-1">
              <i className="bi bi-funnel me-1"></i> Status
            </label>
            <select
              className="form-select border rounded-3 fw-medium text-dark"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="PAID">Paid</option>
              <option value="UNPAID">Unpaid</option>
            </select>
          </div>
        </div>
      </div>

      {/* 4 Summary KPI Cards */}
      <div className="row g-3 mb-4">
        {/* TOTAL BILLED */}
        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 p-3 bg-white h-100 position-relative overflow-hidden">
            <div className="d-flex align-items-center justify-content-between mb-2">
              <span className="text-muted fw-bold text-uppercase fs-8">Total Billed</span>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: '38px', height: '38px', backgroundColor: '#e0f2fe', color: '#0284c7' }}
              >
                <i className="bi bi-receipt fs-6"></i>
              </div>
            </div>
            <h3 className="fw-extrabold mb-1" style={{ color: '#0284c7' }}>
              ₹{(invoicesSummary.totalBilled || 0).toFixed(2)}
            </h3>
            <span className="text-muted small">{invoicesSummary.totalInvoices || 0} invoices</span>
          </div>
        </div>

        {/* PAID */}
        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 p-3 bg-white h-100 position-relative overflow-hidden">
            <div className="d-flex align-items-center justify-content-between mb-2">
              <span className="text-muted fw-bold text-uppercase fs-8">Paid</span>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: '38px', height: '38px', backgroundColor: '#dcfce7', color: '#16a34a' }}
              >
                <i className="bi bi-check-circle-fill fs-6"></i>
              </div>
            </div>
            <h3 className="fw-extrabold mb-1 text-success">
              ₹{(invoicesSummary.totalPaid || 0).toFixed(2)}
            </h3>
            <span className="text-muted small">{invoicesSummary.paidInvoices || 0} Cleared</span>
          </div>
        </div>

        {/* UNPAID */}
        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 p-3 bg-white h-100 position-relative overflow-hidden">
            <div className="d-flex align-items-center justify-content-between mb-2">
              <span className="text-muted fw-bold text-uppercase fs-8">Unpaid</span>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: '38px', height: '38px', backgroundColor: '#fef3c7', color: '#d97706' }}
              >
                <i className="bi bi-clock-history fs-6"></i>
              </div>
            </div>
            <h3 className="fw-extrabold mb-1" style={{ color: '#d97706' }}>
              ₹{(invoicesSummary.totalUnpaid || 0).toFixed(2)}
            </h3>
            <span className="text-muted small">{invoicesSummary.unpaidInvoices || 0} Pending</span>
          </div>
        </div>

        {/* TOTAL (₹) / AVERAGE */}
        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 p-3 bg-white h-100 position-relative overflow-hidden">
            <div className="d-flex align-items-center justify-content-between mb-2">
              <span className="text-muted fw-bold text-uppercase fs-8">Total (₹)</span>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: '38px', height: '38px', backgroundColor: '#f3e8ff', color: '#9333ea' }}
              >
                <i className="bi bi-graph-up-arrow fs-6"></i>
              </div>
            </div>
            <h3 className="fw-extrabold mb-1" style={{ color: '#9333ea' }}>
              ₹{(invoicesSummary.averageBilled || 0).toFixed(2)}
            </h3>
            <span className="text-muted small">Average per invoice</span>
          </div>
        </div>
      </div>

      {/* Invoices List Table */}
      <div className="card border-0 shadow-sm rounded-4 bg-white overflow-hidden mb-4">
        <div className="card-header bg-white border-0 pt-3 pb-2 px-4 d-flex justify-content-between align-items-center flex-wrap gap-2">
          <h5 className="fw-bold mb-0 text-dark">
            <i className="bi bi-receipt me-2 text-primary"></i> Generated Invoices & Statements
          </h5>
          <span className="badge bg-light text-secondary border px-3 py-1.5 fw-semibold fs-7">
            {invoices.length} Records Found
          </span>
        </div>

        <div className="card-body p-0">
          {loading ? (
            <div className="py-5">
              <LoadingSpinner text="Loading invoices and payment ledger..." />
            </div>
          ) : invoices.length === 0 ? (
            <div className="text-center py-5 my-3">
              <div
                className="rounded-circle bg-light d-inline-flex align-items-center justify-content-center mb-3 shadow-sm"
                style={{ width: '70px', height: '70px', color: '#6366f1' }}
              >
                <i className="bi bi-receipt-cutoff fs-2"></i>
              </div>
              <h5 className="fw-bold text-dark mb-1">No Invoices Found</h5>
              <p className="text-muted small mb-0">
                {selectedBuildingId || selectedCycleId
                  ? 'No invoices generated for the selected filters. Finalize a billing cycle to generate resident invoices.'
                  : 'Please select an apartment and billing cycle above.'}
              </p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table custom-table mb-0 align-middle">
                <thead className="bg-light text-muted text-uppercase fs-8">
                  <tr>
                    <th className="ps-4">Invoice #</th>
                    <th>Apartment / Complex</th>
                    <th>Household & Resident</th>
                    <th>Period</th>
                    <th>Water Usage</th>
                    <th>Amount</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    <th className="text-end pe-4">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((inv) => (
                    <tr key={inv.billId} className="border-bottom">
                      <td className="ps-4">
                        <span className="badge bg-light text-dark border fw-bold px-2.5 py-1">
                          #INV-{inv.billId}
                        </span>
                      </td>

                      <td>
                        <div className="fw-semibold text-dark">{inv.buildingName}</div>
                        <small className="text-muted">{inv.buildingNumber || 'Building'}</small>
                      </td>

                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <span className="badge bg-primary-subtle text-primary border border-primary-subtle fw-semibold">
                            {inv.apartmentNumber || 'Apt'}
                          </span>
                          <div>
                            <div className="fw-bold text-dark">{inv.residentName}</div>
                            <small className="text-muted">{inv.residentEmail}</small>
                          </div>
                        </div>
                      </td>

                      <td>
                        <span className="text-dark fw-medium">
                          {inv.billMonth ? `${inv.billMonth}/${inv.billYear}` : 'Monthly'}
                        </span>
                      </td>

                      <td>
                        <span className="badge bg-secondary-subtle text-dark border px-2.5 py-1">
                          {inv.units || 0} kL
                        </span>
                      </td>

                      <td>
                        <div className="fw-extrabold text-dark fs-6">
                          ₹{(inv.amount || 0).toFixed(2)}
                        </div>
                      </td>

                      <td>
                        <small className="text-muted fw-medium">{inv.dueDate || 'N/A'}</small>
                      </td>

                      <td>
                        <span
                          className={`badge ${
                            inv.status === 'PAID'
                              ? 'bg-success-subtle text-success border border-success-subtle'
                              : 'bg-warning-subtle text-warning-emphasis border border-warning-subtle'
                          } px-3 py-1.5 fw-bold`}
                        >
                          {inv.status}
                        </span>
                      </td>

                      <td className="text-end pe-4">
                        <div className="d-flex justify-content-end gap-1.5 flex-nowrap">
                          {/* View Details Drawer */}
                          <button
                            className="btn btn-sm btn-light border text-dark"
                            title="View Itemized Breakdown"
                            onClick={() => setSelectedInvoice(inv)}
                          >
                            <i className="bi bi-eye"></i> Details
                          </button>

                          {/* Download PDF Statement */}
                          <button
                            className="btn btn-sm btn-outline-primary"
                            title="Download PDF Statement"
                            disabled={downloadingId === inv.billId}
                            onClick={() => handleDownloadPdf(inv.billId)}
                          >
                            {downloadingId === inv.billId ? (
                              <span className="spinner-border spinner-border-sm"></span>
                            ) : (
                              <i className="bi bi-file-earmark-pdf"></i>
                            )} PDF
                          </button>

                          {/* Quick Toggle Paid / Unpaid */}
                          <button
                            className={`btn btn-sm ${
                              inv.status === 'PAID' ? 'btn-outline-warning' : 'btn-outline-success'
                            }`}
                            title={inv.status === 'PAID' ? 'Mark as Unpaid' : 'Mark as Paid'}
                            disabled={updatingId === inv.billId}
                            onClick={() => handleToggleStatus(inv)}
                          >
                            {updatingId === inv.billId ? (
                              <span className="spinner-border spinner-border-sm"></span>
                            ) : inv.status === 'PAID' ? (
                              <i className="bi bi-arrow-counterclockwise"></i>
                            ) : (
                              <i className="bi bi-check2"></i>
                            )}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Itemized Invoice Detail Modal / Drawer */}
      {selectedInvoice && (
        <div
          className="modal fade show d-block"
          tabIndex="-1"
          style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)', zIndex: 1050 }}
          onClick={() => setSelectedInvoice(null)}
        >
          <div
            className="modal-dialog modal-dialog-centered modal-lg"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
              <div className="modal-header bg-light border-0 py-3 px-4">
                <div className="d-flex align-items-center gap-2">
                  <h5 className="modal-title fw-bold text-dark mb-0">
                    Itemized Invoice Statement #INV-{selectedInvoice.billId}
                  </h5>
                  <span
                    className={`badge ${
                      selectedInvoice.status === 'PAID'
                        ? 'bg-success-subtle text-success border border-success-subtle'
                        : 'bg-warning-subtle text-warning-emphasis border border-warning-subtle'
                    } fw-bold`}
                  >
                    {selectedInvoice.status}
                  </span>
                </div>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setSelectedInvoice(null)}
                ></button>
              </div>

              <div className="modal-body p-4">
                {/* Complex & Resident Header Info */}
                <div className="row g-3 mb-4 p-3 bg-light rounded-3 border">
                  <div className="col-md-6">
                    <small className="text-muted text-uppercase fw-bold fs-8 d-block">
                      Apartment / Property
                    </small>
                    <div className="fw-bold text-dark fs-6 mt-1">
                      {selectedInvoice.buildingName} ({selectedInvoice.buildingNumber})
                    </div>
                    <small className="text-muted">Unit / Flat: {selectedInvoice.apartmentNumber}</small>
                  </div>
                  <div className="col-md-6 text-md-end">
                    <small className="text-muted text-uppercase fw-bold fs-8 d-block">Resident</small>
                    <div className="fw-bold text-dark fs-6 mt-1">{selectedInvoice.residentName}</div>
                    <small className="text-muted">{selectedInvoice.residentEmail}</small>
                  </div>
                </div>

                {/* 3-Tier Tariff Calculation Breakdown */}
                <h6 className="fw-bold text-dark mb-2">Itemized Charges Breakdown</h6>
                <div className="table-responsive mb-4">
                  <table className="table table-bordered mb-0 align-middle">
                    <thead className="bg-light text-muted fs-8">
                      <tr>
                        <th>Tariff Bracket</th>
                        <th>Consumption Units</th>
                        <th>Rate (₹/kL)</th>
                        <th className="text-end">Subtotal (₹)</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>
                          <span className="badge bg-light text-dark border me-1">Tier 1</span>
                          Base Slab (0 – 20 kL)
                        </td>
                        <td>{selectedInvoice.tier1Units || 0} kL</td>
                        <td>₹{(selectedInvoice.tier1Rate || 0).toFixed(2)}</td>
                        <td className="text-end fw-bold">₹{(selectedInvoice.tier1Amount || 0).toFixed(2)}</td>
                      </tr>
                      <tr>
                        <td>
                          <span className="badge bg-light text-dark border me-1">Tier 2</span>
                          Moderate Slab (20 – 40 kL)
                        </td>
                        <td>{selectedInvoice.tier2Units || 0} kL</td>
                        <td>₹{(selectedInvoice.tier2Rate || 0).toFixed(2)}</td>
                        <td className="text-end fw-bold">₹{(selectedInvoice.tier2Amount || 0).toFixed(2)}</td>
                      </tr>
                      <tr>
                        <td>
                          <span className="badge bg-light text-dark border me-1">Tier 3</span>
                          Excess Slab (Above 40 kL)
                        </td>
                        <td>{selectedInvoice.tier3Units || 0} kL</td>
                        <td>₹{(selectedInvoice.tier3Rate || 0).toFixed(2)}</td>
                        <td className="text-end fw-bold">₹{(selectedInvoice.tier3Amount || 0).toFixed(2)}</td>
                      </tr>
                      {selectedInvoice.baseCharge > 0 && (
                        <tr>
                          <td colSpan="3" className="text-muted">Base / Service Charge</td>
                          <td className="text-end fw-bold">₹{(selectedInvoice.baseCharge || 0).toFixed(2)}</td>
                        </tr>
                      )}
                      <tr className="table-active">
                        <td colSpan="3" className="fw-bold text-dark">
                          Total Invoice Amount Due
                        </td>
                        <td className="text-end fw-extrabold text-primary fs-5">
                          ₹{(selectedInvoice.amount || 0).toFixed(2)}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div className="d-flex justify-content-between align-items-center text-muted small">
                  <div>
                    <strong>Due Date:</strong> {selectedInvoice.dueDate || 'N/A'}
                  </div>
                  <div>
                    <strong>Bill Month:</strong> {selectedInvoice.billMonth}/{selectedInvoice.billYear}
                  </div>
                </div>
              </div>

              <div className="modal-footer bg-light border-0 py-3 px-4 d-flex justify-content-between">
                <div>
                  <button
                    className={`btn btn-sm ${
                      selectedInvoice.status === 'PAID' ? 'btn-outline-warning' : 'btn-success'
                    } fw-bold`}
                    disabled={updatingId === selectedInvoice.billId}
                    onClick={() => handleToggleStatus(selectedInvoice)}
                  >
                    {updatingId === selectedInvoice.billId ? (
                      <span className="spinner-border spinner-border-sm me-1"></span>
                    ) : selectedInvoice.status === 'PAID' ? (
                      <i className="bi bi-arrow-counterclockwise me-1"></i>
                    ) : (
                      <i className="bi bi-check2-circle me-1"></i>
                    )}
                    {selectedInvoice.status === 'PAID' ? 'Mark as Unpaid' : 'Mark as Paid'}
                  </button>
                </div>

                <div className="d-flex gap-2">
                  <button
                    type="button"
                    className="btn btn-outline-primary btn-sm fw-bold"
                    disabled={downloadingId === selectedInvoice.billId}
                    onClick={() => handleDownloadPdf(selectedInvoice.billId)}
                  >
                    {downloadingId === selectedInvoice.billId ? (
                      <span className="spinner-border spinner-border-sm me-1"></span>
                    ) : (
                      <i className="bi bi-file-earmark-pdf me-1"></i>
                    )}
                    Download PDF
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm fw-bold"
                    onClick={() => setSelectedInvoice(null)}
                  >
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
