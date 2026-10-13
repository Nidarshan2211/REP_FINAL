import React, { useState, useEffect } from 'react';
import API from '../../services/api';
import LoadingSpinner from '../../components/LoadingSpinner';

export default function ResidentBillsPage() {
  const [bills, setBills] = useState([]);
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Active Razorpay Modal State
  const [payingBill, setPayingBill] = useState(null);
  const [razorpayMethod, setRazorpayMethod] = useState('UPI'); // 'UPI', 'CARD', 'NETBANKING', 'WALLET'
  const [processingPayment, setProcessingPayment] = useState(false);
  const [upiId, setUpiId] = useState('resident@okaxis');
  const [selectedApp, setSelectedApp] = useState('gpay');

  const fetchData = () => {
    Promise.all([
      API.get('/resident/bills'),
      API.get('/resident/payments')
    ]).then(([billRes, payRes]) => {
      setBills(billRes.data || []);
      setPayments(payRes.data || []);
    }).catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Authenticated PDF Download Helper (No HTTP 403 Forbidden errors)
  const handleDownloadPdf = async (billId, isPaid = false) => {
    try {
      const response = await API.get(`/bills/${billId}/pdf`, {
        responseType: 'blob'
      });
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', isPaid ? `Water_Bill_Paid_Receipt_SWB_${billId}.pdf` : `Water_Bill_SWB_${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      // Fallback with Token Parameter in URL
      const token = localStorage.getItem('token');
      if (token) {
        window.open(`/bills/${billId}/pdf?token=${encodeURIComponent(token)}`, '_blank');
      } else {
        alert('Failed to download PDF receipt. Please check your login session.');
      }
    }
  };

  // Execute Razorpay Payment Process cleanly
  const handleRazorpayPayment = async (bill) => {
    setProcessingPayment(true);
    const razorpayTxnRef = 'rzp_pay_' + Math.random().toString(36).substring(2, 14).toUpperCase();
    const fullMethodName = `RAZORPAY_${razorpayMethod}_${selectedApp.toUpperCase()}`;

    setTimeout(async () => {
      try {
        await API.post('/resident/pay', {
          billId: bill.billId,
          amount: bill.amount,
          paymentMethod: fullMethodName,
          transactionReference: razorpayTxnRef
        });
        setPayingBill(null);
        setProcessingPayment(false);
        fetchData();

        if (window.confirm(`✓ Razorpay Payment Successful!\n\nRazorpay Txn ID: ${razorpayTxnRef}\nAmount Paid: ₹${bill.amount?.toFixed(2)}\nPayment Method: Razorpay ${razorpayMethod} (${selectedApp.toUpperCase()})\nBill Status: PAID\n\nClick OK to download your official Paid Receipt PDF now!`)) {
          handleDownloadPdf(bill.billId, true);
        }
      } catch (err) {
        alert(err.response?.data?.message || 'Razorpay payment confirmation failed.');
        setProcessingPayment(false);
      }
    }, 1000);
  };

  // Helper to format 3-Tier Tariff Rate calculation breakdown
  const getTariffBreakdown = (bill) => {
    const t1Units = bill.tier1Units || 0;
    const t1Amount = bill.tier1Amount || 0;
    const t2Units = bill.tier2Units || 0;
    const t2Amount = bill.tier2Amount || 0;
    const t3Units = bill.tier3Units || 0;
    const t3Amount = bill.tier3Amount || 0;

    return (
      <div className="py-1">
        <div className="small text-muted fs-8">
          <div>• Tier 1 (0-20 kL @ ₹2.50): <strong className="text-dark">₹{t1Amount}</strong> ({t1Units} kL)</div>
          {t2Units > 0 && (
            <div>• Tier 2 (20-40 kL @ ₹4.00): <strong className="text-dark">₹{t2Amount}</strong> ({t2Units} kL)</div>
          )}
          {t3Units > 0 && (
            <div>• Tier 3 (&gt;40 kL @ ₹6.00): <strong className="text-dark">₹{t3Amount}</strong> ({t3Units} kL)</div>
          )}
        </div>
      </div>
    );
  };

  if (loading) return <LoadingSpinner text="Fetching billing statement..." />;

  return (
    <div className="container-fluid py-4">
      <div className="mb-4">
        <h2 className="fw-bold mb-1 text-dark">My Water Bills & Payments</h2>
        <p className="text-muted small mb-0">Pay active bills via Razorpay, download official PDF receipts, and inspect payment history</p>
      </div>

      <div className="card glass-card border mb-4 shadow-sm bg-white">
        <div className="card-header bg-transparent border-0 pt-4 px-4">
          <h5 className="fw-bold mb-0 text-dark">Billing Invoices & PDF Receipts</h5>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Bill ID</th>
                  <th>Billing Month</th>
                  <th>Units Consumed</th>
                  <th>Tariff Breakdown</th>
                  <th>Total Amount</th>
                  <th>Due Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bills.length ? (
                  bills.map(b => {
                    const isPaid = b.status === 'PAID';
                    return (
                      <tr key={b.billId}>
                        <td><code className="text-dark fw-bold">#SWB-{b.billId}</code></td>
                        <td>{b.billMonth}/{b.billYear}</td>
                        <td><span className="badge bg-secondary-subtle text-dark border">{b.units} kL</span></td>
                        <td>{getTariffBreakdown(b)}</td>
                        <td className="fw-bold text-dark fs-6">₹{b.amount?.toFixed(2)}</td>
                        <td>{b.dueDate}</td>
                        <td>
                          <span className={`badge badge-custom ${isPaid ? 'badge-paid' : 'badge-unpaid'}`}>
                            {b.status}
                          </span>
                        </td>
                        <td>
                          <div className="d-flex align-items-center gap-2">
                            {!isPaid ? (
                              <button className="btn btn-sm btn-dark fw-bold px-3 shadow-sm" onClick={() => setPayingBill(b)}>
                                <i className="bi bi-wallet2 me-1.5 text-info"></i> Pay Now
                              </button>
                            ) : (
                              <span className="badge bg-success-subtle text-success border border-success-subtle px-2.5 py-1 fw-bold fs-8">
                                <i className="bi bi-check-circle-fill me-1"></i> PAID
                              </span>
                            )}
                            <button
                              className={`btn btn-sm ${isPaid ? 'btn-success text-white fw-bold shadow-sm' : 'btn-outline-secondary fw-semibold'} px-3`}
                              onClick={() => handleDownloadPdf(b.billId, isPaid)}
                              title={isPaid ? "Download Official Paid Receipt PDF" : "Download PDF Invoice"}
                            >
                              <i className="bi bi-file-earmark-pdf me-1"></i> {isPaid ? 'PDF Receipt' : 'PDF Invoice'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-muted">No water bills issued yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Payment History */}
      <div className="card glass-card border shadow-sm bg-white">
        <div className="card-header bg-transparent border-0 pt-4 px-4 d-flex align-items-center justify-content-between">
          <h5 className="fw-bold mb-0 text-dark">Payment Transactions</h5>
          <span className="badge bg-light text-dark border px-3 py-1.5 fs-8 fw-semibold">
            <i className="bi bi-shield-check text-success me-1"></i> Razorpay Secured
          </span>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Ref / Razorpay ID</th>
                  <th>Bill ID</th>
                  <th>Amount</th>
                  <th>Payment Method</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {payments.length ? (
                  payments.map(p => (
                    <tr key={p.paymentId}>
                      <td><code className="text-dark fw-semibold">{p.transactionReference}</code></td>
                      <td>#SWB-{p.billId}</td>
                      <td className="fw-bold text-dark">₹{p.amount?.toFixed(2)}</td>
                      <td>
                        <span className="badge bg-light text-dark border fw-medium px-2.5 py-1">
                          <i className="bi bi-credit-card me-1 text-primary"></i> {p.paymentMethod}
                        </span>
                      </td>
                      <td className="text-muted small">{p.paymentDate ? p.paymentDate.substring(0, 10) : 'N/A'}</td>
                      <td><span className="badge badge-custom badge-approved">{p.status}</span></td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center py-4 text-muted">No payment history.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* RAZORPAY PAYMENT MODAL */}
      {payingBill && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '520px' }}>
            <div className="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
              
              {/* Razorpay Brand Header */}
              <div className="p-4 text-white d-flex align-items-center justify-content-between" style={{ backgroundColor: '#02042B' }}>
                <div className="d-flex align-items-center gap-2.5">
                  <div className="bg-primary rounded-3 p-2 d-flex align-items-center justify-content-center" style={{ width: 40, height: 40 }}>
                    <i className="bi bi-lightning-charge-fill text-white fs-5"></i>
                  </div>
                  <div>
                    <h5 className="fw-extrabold text-white mb-0">Razorpay</h5>
                    <small className="text-white-50 fs-8">AquaFlow Water Billing • Bill #SWB-{payingBill.billId}</small>
                  </div>
                </div>
                <button type="button" className="btn-close btn-close-white" onClick={() => setPayingBill(null)}></button>
              </div>

              {/* Amount Banner */}
              <div className="p-3 text-center border-bottom" style={{ backgroundColor: '#F8FAFC' }}>
                <small className="text-muted fw-semibold text-uppercase fs-8 d-block mb-1">Total Amount Payable</small>
                <h2 className="fw-extrabold text-dark mb-0">₹{payingBill.amount?.toFixed(2)}</h2>
                <small className="text-success fw-bold fs-8"><i className="bi bi-shield-lock-fill me-1"></i> 256-Bit SSL Encrypted Payment</small>
              </div>

              <div className="modal-body p-4">
                {/* Method Navigation Tabs */}
                <label className="form-label fw-bold text-dark fs-7 mb-2">Select Payment Method</label>
                <div className="row g-2 mb-3">
                  <div className="col-6">
                    <button
                      className={`btn w-100 p-2.5 text-start border rounded-3 d-flex align-items-center gap-2 ${razorpayMethod === 'UPI' ? 'border-primary bg-primary-subtle fw-bold' : 'bg-white'}`}
                      onClick={() => setRazorpayMethod('UPI')}
                    >
                      <i className="bi bi-qr-code-scan fs-5 text-primary"></i>
                      <div>
                        <div className="fs-7 leading-tight">UPI / QR</div>
                        <small className="text-muted fs-8 d-block">GPay, PhonePe, Paytm</small>
                      </div>
                    </button>
                  </div>

                  <div className="col-6">
                    <button
                      className={`btn w-100 p-2.5 text-start border rounded-3 d-flex align-items-center gap-2 ${razorpayMethod === 'CARD' ? 'border-primary bg-primary-subtle fw-bold' : 'bg-white'}`}
                      onClick={() => setRazorpayMethod('CARD')}
                    >
                      <i className="bi bi-credit-card-2-front fs-5 text-success"></i>
                      <div>
                        <div className="fs-7 leading-tight">Cards</div>
                        <small className="text-muted fs-8 d-block">RuPay, Visa, Master</small>
                      </div>
                    </button>
                  </div>

                  <div className="col-6">
                    <button
                      className={`btn w-100 p-2.5 text-start border rounded-3 d-flex align-items-center gap-2 ${razorpayMethod === 'NETBANKING' ? 'border-primary bg-primary-subtle fw-bold' : 'bg-white'}`}
                      onClick={() => setRazorpayMethod('NETBANKING')}
                    >
                      <i className="bi bi-bank fs-5 text-warning"></i>
                      <div>
                        <div className="fs-7 leading-tight">NetBanking</div>
                        <small className="text-muted fs-8 d-block">SBI, HDFC, ICICI</small>
                      </div>
                    </button>
                  </div>

                  <div className="col-6">
                    <button
                      className={`btn w-100 p-2.5 text-start border rounded-3 d-flex align-items-center gap-2 ${razorpayMethod === 'WALLET' ? 'border-primary bg-primary-subtle fw-bold' : 'bg-white'}`}
                      onClick={() => setRazorpayMethod('WALLET')}
                    >
                      <i className="bi bi-wallet2 fs-5 text-info"></i>
                      <div>
                        <div className="fs-7 leading-tight">Wallets</div>
                        <small className="text-muted fs-8 d-block">Airtel, Amazon Pay</small>
                      </div>
                    </button>
                  </div>
                </div>

                {/* Sub-options for UPI */}
                {razorpayMethod === 'UPI' && (
                  <div className="p-3 bg-light rounded-3 border mb-3">
                    <label className="form-label fs-8 fw-bold text-dark mb-2">Popular UPI Apps</label>
                    <div className="d-flex gap-2 mb-3">
                      {['gpay', 'phonepe', 'paytm', 'bhim'].map(app => (
                        <button
                          key={app}
                          type="button"
                          className={`btn btn-sm flex-grow-1 border fw-bold text-uppercase py-1.5 ${selectedApp === app ? 'btn-dark' : 'btn-white bg-white'}`}
                          onClick={() => setSelectedApp(app)}
                        >
                          {app}
                        </button>
                      ))}
                    </div>

                    <label className="form-label fs-8 fw-bold text-dark mb-1">UPI VPA Address</label>
                    <input
                      type="text"
                      className="form-control form-control-sm"
                      placeholder="e.g. mobile@upi or name@okaxis"
                      value={upiId}
                      onChange={e => setUpiId(e.target.value)}
                    />
                  </div>
                )}

                {/* Card Fields Preview */}
                {razorpayMethod === 'CARD' && (
                  <div className="p-3 bg-light rounded-3 border mb-3">
                    <label className="form-label fs-8 fw-bold text-dark mb-1">Card Number</label>
                    <input type="text" className="form-control form-control-sm mb-2" placeholder="4111 •••• •••• 1111 (Test Card)" defaultValue="4111 1111 1111 1111" />
                    <div className="row g-2">
                      <div className="col-6">
                        <label className="form-label fs-8 fw-bold text-dark mb-1">Expiry</label>
                        <input type="text" className="form-control form-control-sm" placeholder="MM/YY" defaultValue="12/28" />
                      </div>
                      <div className="col-6">
                        <label className="form-label fs-8 fw-bold text-dark mb-1">CVV</label>
                        <input type="password" className="form-control form-control-sm" placeholder="CVV" defaultValue="123" />
                      </div>
                    </div>
                  </div>
                )}

                {/* NetBanking Select */}
                {razorpayMethod === 'NETBANKING' && (
                  <div className="p-3 bg-light rounded-3 border mb-3">
                    <label className="form-label fs-8 fw-bold text-dark mb-1">Select Bank</label>
                    <select className="form-select form-select-sm">
                      <option value="SBI">State Bank of India (SBI)</option>
                      <option value="HDFC">HDFC Bank</option>
                      <option value="ICICI">ICICI Bank</option>
                      <option value="AXIS">Axis Bank</option>
                      <option value="KOTAK">Kotak Mahindra Bank</option>
                    </select>
                  </div>
                )}
              </div>

              {/* Modal Footer Button */}
              <div className="modal-footer border-top p-3 bg-white">
                <button type="button" className="btn btn-light fw-semibold" onClick={() => setPayingBill(null)}>Cancel</button>
                <button
                  type="button"
                  className="btn btn-primary px-4 fw-extrabold shadow-sm d-flex align-items-center gap-2"
                  style={{ backgroundColor: '#005F73', borderColor: '#005F73' }}
                  onClick={() => handleRazorpayPayment(payingBill)}
                  disabled={processingPayment}
                >
                  {processingPayment ? (
                    <>
                      <span className="spinner-border spinner-border-sm"></span> Processing Razorpay...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-shield-check fs-6"></i> Pay ₹{payingBill.amount?.toFixed(2)} via Razorpay
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
