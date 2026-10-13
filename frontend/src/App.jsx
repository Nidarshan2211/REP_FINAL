import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

import PublicLayout from './layouts/PublicLayout';
import DashboardLayout from './layouts/DashboardLayout';

import LandingPage from './pages/Public/LandingPage';
import LoginPage from './pages/Public/LoginPage';
import RegisterOwnerPage from './pages/Public/RegisterOwnerPage';
import AcceptInvitationPage from './pages/Public/AcceptInvitationPage';

import AdminDashboard from './pages/Admin/AdminDashboard';
import AdminOwnersPage from './pages/Admin/AdminOwnersPage';
import AdminPendingOwnersPage from './pages/Admin/AdminPendingOwnersPage';

import OwnerDashboard from './pages/Owner/OwnerDashboard';
import OwnerBuildingsPage from './pages/Owner/OwnerBuildingsPage';
import OwnerResidentsPage from './pages/Owner/OwnerResidentsPage';
import OwnerUsagePage from './pages/Owner/OwnerUsagePage';
import OwnerUsageComparisonPage from './pages/Owner/OwnerUsageComparisonPage';
import OwnerBillingPage from './pages/Owner/OwnerBillingPage';

import ResidentDashboard from './pages/Resident/ResidentDashboard';
import ResidentBillsPage from './pages/Resident/ResidentBillsPage';
import ResidentComparisonPage from './pages/Resident/ResidentComparisonPage';
import ResidentNotificationsPage from './pages/Resident/ResidentNotificationsPage';

import ProfilePage from './pages/Common/ProfilePage';
import InvoicesPage from './pages/Common/InvoicesPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register-owner" element={<RegisterOwnerPage />} />
            <Route path="/accept-invitation" element={<AcceptInvitationPage />} />
          </Route>

          {/* Super Admin Routes */}
          <Route element={<DashboardLayout allowedRole="SUPER_ADMIN" />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/owners" element={<AdminOwnersPage />} />
            <Route path="/admin/pending-owners" element={<AdminPendingOwnersPage />} />
            <Route path="/admin/profile" element={<ProfilePage />} />
          </Route>

          {/* Building Owner Routes */}
          <Route element={<DashboardLayout allowedRole="BUILDING_OWNER" />}>
            <Route path="/owner/dashboard" element={<OwnerDashboard />} />
            <Route path="/owner/buildings" element={<OwnerBuildingsPage />} />
            <Route path="/owner/residents" element={<OwnerResidentsPage />} />
            <Route path="/owner/usage" element={<OwnerUsagePage />} />
            <Route path="/owner/usage-comparison" element={<OwnerUsageComparisonPage />} />
            <Route path="/owner/billing" element={<OwnerBillingPage />} />
            <Route path="/owner/invoices" element={<InvoicesPage />} />
            <Route path="/owner/profile" element={<ProfilePage />} />
          </Route>

          {/* Resident Routes */}
          <Route element={<DashboardLayout allowedRole="RESIDENT" />}>
            <Route path="/resident/dashboard" element={<ResidentDashboard />} />
            <Route path="/resident/bills" element={<ResidentBillsPage />} />
            <Route path="/resident/comparison" element={<ResidentComparisonPage />} />
            <Route path="/resident/notifications" element={<ResidentNotificationsPage />} />
            <Route path="/resident/profile" element={<ProfilePage />} />
          </Route>

          {/* Fallback Catch-All */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
