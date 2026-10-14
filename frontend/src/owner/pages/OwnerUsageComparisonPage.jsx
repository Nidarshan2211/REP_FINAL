import React, { useState, useEffect } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  Cell
} from 'recharts';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function OwnerUsageComparisonPage() {
  const [data, setData] = useState(null);
  const [buildings, setBuildings] = useState([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState('');
  const [loading, setLoading] = useState(true);
  const [sortBy, setSortBy] = useState('apartment'); // 'apartment', 'usage'

  useEffect(() => {
    API.get('/owners/buildings')
      .then(res => {
        const bList = res.data || [];
        setBuildings(bList);
        if (bList.length) {
          setSelectedBuildingId(bList[0].buildingId);
        }
      })
      .catch(err => console.error(err));
  }, []);

  const fetchComparison = (bId) => {
    setLoading(true);
    const url = bId ? `/owners/usage-comparison?buildingId=${bId}` : '/owners/usage-comparison';
    API.get(url)
      .then(res => setData(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchComparison(selectedBuildingId);
  }, [selectedBuildingId]);

  if (loading && !data) return <LoadingSpinner text="Loading Building Usage Comparison..." />;

  const households = data?.households || [];
  const bAvg = data?.buildingAverage || 0;

  // Sorting
  const sortedHouseholds = [...households].sort((a, b) => {
    if (sortBy === 'usage') {
      return b.unitsConsumed - a.unitsConsumed;
    }
    return a.apartmentNumber.localeCompare(b.apartmentNumber, undefined, { numeric: true });
  });

  const chartData = sortedHouseholds.map(h => ({
    apartment: `Apt ${h.apartmentNumber}`,
    consumption: h.unitsConsumed,
    resident: h.residentName,
    isAbove: h.unitsConsumed > bAvg
  }));

  return (
    <div className="container-fluid py-4">
      {/* Header Banner & Building Selector */}
      <div className="card glass-card border-0 shadow-sm p-4 mb-4 bg-white rounded-4">
        <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <h2 className="fw-bold mb-1 text-dark">Household Usage Comparison</h2>
            <p className="text-muted small mb-0">Compare water consumption across all apartments in your building</p>
          </div>

          <div className="d-flex align-items-center gap-2">
            {buildings.length > 0 && (
              <select
                className="form-select font-semibold"
                value={selectedBuildingId}
                onChange={e => setSelectedBuildingId(e.target.value)}
                style={{ minWidth: '220px' }}
              >
                {buildings.map(b => (
                  <option key={b.buildingId} value={b.buildingId}>
                    {b.buildingName} ({b.buildingNumber})
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>
      </div>

      {/* Summary Metrics Banner */}
      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card glass-card border shadow-sm p-3 bg-white rounded-4">
            <small className="text-muted fw-semibold text-uppercase fs-8 d-block mb-1">Building Average Consumption</small>
            <h3 className="fw-extrabold text-dark mb-0">{bAvg} kL / Household</h3>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card glass-card border shadow-sm p-3 bg-white rounded-4">
            <small className="text-muted fw-semibold text-uppercase fs-8 d-block mb-1">Total Households Tracked</small>
            <h3 className="fw-extrabold text-dark mb-0">{households.length} Apartments</h3>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card glass-card border shadow-sm p-3 bg-white rounded-4">
            <small className="text-muted fw-semibold text-uppercase fs-8 d-block mb-1">Above Average Households</small>
            <h3 className="fw-extrabold mb-0" style={{ color: '#5A738E' }}>
              {households.filter(h => h.unitsConsumed > bAvg).length} Households
            </h3>
          </div>
        </div>
      </div>

      {/* Recharts BarChart Visualization */}
      <div className="card glass-card border shadow-sm p-4 mb-4 bg-white rounded-4">
        <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
          <h5 className="fw-bold mb-0 text-dark">Apartment Consumption Chart</h5>
          <div className="d-flex align-items-center gap-2">
            <span className="text-muted fs-8">Sort By:</span>
            <button
              className={`btn btn-sm ${sortBy === 'apartment' ? 'btn-dark' : 'btn-outline-secondary'}`}
              onClick={() => setSortBy('apartment')}
            >
              Apartment #
            </button>
            <button
              className={`btn btn-sm ${sortBy === 'usage' ? 'btn-dark' : 'btn-outline-secondary'}`}
              onClick={() => setSortBy('usage')}
            >
              Highest Consumption
            </button>
          </div>
        </div>

        {chartData.length ? (
          <div style={{ width: '100%', height: 320 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
                <XAxis dataKey="apartment" stroke="#475569" fontSize={12} tickLine={false} />
                <YAxis stroke="#475569" fontSize={12} tickLine={false} unit=" kL" />
                <Tooltip
                  contentStyle={{ backgroundColor: '#003554', borderRadius: '8px', color: '#fff', border: 'none' }}
                  itemStyle={{ color: '#E3F6FF', fontWeight: 'bold' }}
                  formatter={(val, name, props) => [`${val} kL (${props.payload.resident})`, 'Usage']}
                />
                <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
                <Bar dataKey="consumption" name="Household Usage (kL)" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.isAbove ? '#5A738E' : '#005F73'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="p-4 text-center text-muted bg-light rounded-3">No apartment consumption records found.</div>
        )}
      </div>

      {/* Household Table List */}
      <div className="card glass-card border shadow-sm bg-white rounded-4 overflow-hidden">
        <div className="card-header bg-transparent border-0 pt-4 px-4">
          <h5 className="fw-bold mb-0 text-dark">Household Usage Breakdown Table</h5>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table custom-table mb-0 align-middle">
              <thead>
                <tr>
                  <th>Apartment #</th>
                  <th>Resident Name</th>
                  <th>Meter Number</th>
                  <th>Consumption (kL)</th>
                  <th>Vs Building Average</th>
                  <th>Status Indicator</th>
                </tr>
              </thead>
              <tbody>
                {sortedHouseholds.length ? (
                  sortedHouseholds.map(h => {
                    const isAbove = h.unitsConsumed > bAvg;
                    const diff = Math.round((h.unitsConsumed - bAvg) * 100.0) / 100.0;
                    return (
                      <tr key={h.residentId}>
                        <td><strong className="text-dark">Apt {h.apartmentNumber}</strong></td>
                        <td>{h.residentName}</td>
                        <td><code className="text-dark">{h.meterNumber}</code></td>
                        <td className="fw-bold text-dark fs-6">{h.unitsConsumed} kL</td>
                        <td className="small">
                          {diff > 0 ? (
                            <span className="fw-bold" style={{ color: '#5A738E' }}>+{diff} kL above avg</span>
                          ) : (
                            <span className="text-success fw-bold">{diff} kL below avg</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${isAbove ? 'badge-slate-blue' : 'bg-success-subtle text-success border border-success-subtle'} px-3 py-1.5 fw-bold fs-8`}>
                            {isAbove ? 'HIGH (Above Avg)' : 'NORMAL'}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center py-4 text-muted">No households found for selected building.</td>
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
