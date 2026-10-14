import React, { useContext } from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import { AuthContext } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

export default function DashboardLayout({ allowedRole }) {
  const { user, loading } = useContext(AuthContext);

  if (loading) return <LoadingSpinner text="Checking authentication context..." />;

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRole && user.role !== allowedRole) {
    // Role guard redirect
    if (user.role === 'SUPER_ADMIN') return <Navigate to="/admin/dashboard" replace />;
    if (user.role === 'BUILDING_OWNER') return <Navigate to="/owner/dashboard" replace />;
    if (user.role === 'RESIDENT') return <Navigate to="/resident/dashboard" replace />;
  }

  return (
    <div className="d-flex flex-column min-vh-100 bg-light">
      <Navbar />
      <div className="d-flex flex-grow-1 w-100">
        <Sidebar />
        <main className="flex-grow-1 p-3 p-md-4 overflow-auto" style={{ minWidth: 0 }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
