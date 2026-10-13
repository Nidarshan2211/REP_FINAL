import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';
import API from '../../services/api';
import StatCard from '../../components/StatCard';
import WaterSavingTips from '../../components/WaterSavingTips';
import LoadingSpinner from '../../components/LoadingSpinner';

export default function ResidentDashboard() {
  const [stats, setStats] = useState(null);
  const [usageList, setUsageList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      API.get('/resident/dashboard'),
      API.get('/resident/usage')
    ]).then(([statRes, usageRes]) => {
      setStats(statRes.data);
      const rawUsage = usageRes.data || [];
      const sorted = [...rawUsage].sort((a, b) => (a.year - b.year) || (a.month - b.month));
      setUsageList(sorted.length ? sorted : rawUsage);
    }).catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner text="Loading Resident Dashboard..." />;

  // Prepare Recharts Data
  const chartData = usageList.map(u => ({
    label: `${u.month}/${u.year}`,
    consumption: u.unitsConsumed || 0,
    reading: u.currentReading || 0
  }));

  return (
    <div className="container-fluid py-4">
      {/* A1: Welcome & Current Billing Cycle Header Banner */}
      <div className="card glass-card border-0 shadow-sm p-4 mb-4 bg-white rounded-4">
        <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <div className="d-flex align-items-center gap-2 mb-1">
              <h2 className="fw-bold text-dark mb-0">Welcome, {stats?.residentName || 'Resident'}</h2>
              <span className="badge bg-primary-subtle text-primary border px-2.5 py-1 fs-8 fw-semibold rounded-pill">
                <i className="bi bi-person-check-fill me-1"></i> Active Resident
              </span>
            </div>
            <p className="text-muted small mb-0">
              <i className="bi bi-building me-1 text-primary"></i> {stats?.buildingName} • <i className="bi bi-door-open me-1 text-success"></i> Apt {stats?.apartmentNumber} • Meter: <code className="text-dark fw-bold">{stats?.meterNumber}</code>
            </p>
          </div>

          <div className="d-flex gap-2">
            <Link to="/resident/comparison" className="btn btn-outline-dark fw-semibold">
              <i className="bi bi-bar-chart-line me-1.5 text-primary"></i> Compare Usage
            </Link>
            <Link to="/resident/bills" className="btn btn-dark fw-bold">
              <i className="bi bi-wallet2 me-1.5 text-info"></i> View & Pay Bills
            </Link>
          </div>
        </div>
      </div>

      {/* A1: Summary Stat Cards */}
      <div className="row g-3 mb-4">
        <div className="col-md-6 col-lg-3">
          <StatCard title="Current Consumption" value={`${stats?.currentMonthUsage || 0} kL`} icon="bi-droplet-half" subtext={`Cycle: ${stats?.currentBillingCycle || 'Active'}`} />
        </div>
        <div className="col-md-6 col-lg-3">
          <StatCard title="Current Bill Amount" value={`₹${(stats?.activeBillAmount || 0).toFixed(2)}`} icon="bi-cash" subtext={`Due: ${stats?.activeBillDueDate || 'N/A'}`} />
        </div>
        <div className="col-md-6 col-lg-3">
          <StatCard title="Payment Status" value={stats?.activeBillStatus || 'N/A'} icon="bi-check-circle" />
        </div>
        <div className="col-md-6 col-lg-3">
          <StatCard title="Notifications" value={stats?.unreadNotificationsCount || 0} icon="bi-bell" subtext="Unread Alerts" />
        </div>
      </div>

      {/* A1 & A2: Main Grid - Recharts Consumption Trends & Cycle Summary */}
      <div className="row g-4 mb-4">
        
        {/* A2: Recharts Line Chart */}
        <div className="col-lg-8">
          <div className="card glass-card border shadow-sm p-4 h-100 bg-white rounded-4">
            <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
              <div>
                <h5 className="fw-bold mb-1 text-dark">Monthly Consumption Trend (Recharts)</h5>
                <p className="text-muted fs-7 mb-0">Historical water usage trend across billing cycles (kL)</p>
              </div>
              <span className="badge bg-light text-dark border px-3 py-1.5 fs-8 fw-semibold">
                <i className="bi bi-graph-up-arrow text-primary me-1.5"></i> Recharts Visualization
              </span>
            </div>

            {chartData.length ? (
              <div style={{ width: '100%', height: 280 }} className="pt-2">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
                    <XAxis dataKey="label" stroke="#64748B" fontSize={12} tickLine={false} />
                    <YAxis stroke="#64748B" fontSize={12} tickLine={false} unit=" kL" />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#003554', borderRadius: '8px', color: '#fff', border: 'none' }}
                      itemStyle={{ color: '#E3F6FF', fontWeight: 'bold' }}
                      formatter={(val) => [`${val} kL`, 'Consumption']}
                      labelFormatter={(lbl) => `Billing Month: ${lbl}`}
                    />
                    <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
                    <Line
                      type="monotone"
                      dataKey="consumption"
                      name="Water Consumption (kL)"
                      stroke="#005F73"
                      strokeWidth={3}
                      activeDot={{ r: 8, fill: '#0A9396' }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="p-5 text-center bg-light rounded-3 border my-auto">
                <i className="bi bi-droplet text-secondary display-4 d-block mb-2"></i>
                <h5 className="fw-bold text-dark">No Consumption Logs Available</h5>
                <small className="text-muted">Meter consumption readings will appear here after billing cycle logs.</small>
              </div>
            )}
          </div>
        </div>

        {/* A1: Detailed Current Billing Cycle Card */}
        <div className="col-lg-4">
          <div className="card glass-card border shadow-sm p-4 h-100 bg-white rounded-4">
            <h5 className="fw-bold mb-3 text-dark">Current Billing Cycle Details</h5>
            
            <div className="p-3 bg-light rounded-3 border mb-3">
              <div className="mb-2">
                <small className="text-muted fw-semibold text-uppercase fs-8 d-block">Billing Cycle</small>
                <strong className="text-dark fs-6">{stats?.currentBillingCycle || 'Active Cycle'}</strong>
              </div>

              <div className="row g-2 mb-2 pt-2 border-top">
                <div className="col-6">
                  <small className="text-muted fs-8 d-block">Start Date</small>
                  <span className="fw-semibold text-dark fs-7">{stats?.cycleStartDate || 'N/A'}</span>
                </div>
                <div className="col-6">
                  <small className="text-muted fs-8 d-block">End Date</small>
                  <span className="fw-semibold text-dark fs-7">{stats?.cycleEndDate || 'N/A'}</span>
                </div>
              </div>

              <div className="pt-2 border-top">
                <small className="text-muted fw-semibold text-uppercase fs-8 d-block">Current Consumption</small>
                <h3 className="fw-bold text-dark mb-2">{stats?.currentMonthUsage || 0} kL</h3>
                
                <small className="text-muted fw-semibold text-uppercase fs-8 d-block">Current Bill Amount</small>
                <h2 className="fw-extrabold text-dark mb-1">₹{(stats?.activeBillAmount || 0).toFixed(2)}</h2>
              </div>
              
              <div className="d-flex align-items-center justify-content-between mt-2 pt-2 border-top fs-8">
                <span className="text-muted">Due Date:</span>
                <strong className="text-dark">{stats?.activeBillDueDate || 'N/A'}</strong>
              </div>
              <div className="d-flex align-items-center justify-content-between mt-1 fs-8">
                <span className="text-muted">Payment Status:</span>
                <span className={`badge ${stats?.activeBillStatus === 'PAID' ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-warning-subtle text-dark border'}`}>
                  {stats?.activeBillStatus || 'UNPAID'}
                </span>
              </div>
            </div>

            <div className="d-grid gap-2">
              <Link to="/resident/bills" className="btn btn-dark fw-bold py-2">
                <i className="bi bi-receipt me-1.5"></i> Open Billing Statement
              </Link>
              <Link to="/resident/notifications" className="btn btn-outline-secondary py-2 fs-7 fw-semibold">
                <i className="bi bi-bell me-1.5"></i> Notifications ({stats?.unreadNotificationsCount || 0})
              </Link>
            </div>
          </div>
        </div>

      </div>

      {/* MODULE B: Water-Saving Tips Feed Component */}
      <div className="mb-4">
        <WaterSavingTips />
      </div>
    </div>
  );
}
