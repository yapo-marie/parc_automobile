import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import { DashboardLayout } from './layouts/DashboardLayout'
import { DashboardHomePage } from './pages/DashboardHomePage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { LoginPage } from './pages/LoginPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { ProfilePage } from './pages/ProfilePage'
import { ReservationsPage } from './pages/ReservationsPage'
import { AssignmentsPage } from './pages/AssignmentsPage'
import { TechnicalVisitsPage } from './pages/TechnicalVisitsPage'
import { BreakdownsPage } from './pages/BreakdownsPage'
import { FuelRecordsPage } from './pages/FuelRecordsPage'
import { FleetPage } from './pages/FleetPage'
import { RolesPage } from './pages/RolesPage'
import { UsersPage } from './pages/UsersPage'
import { VehicleDetailPage } from './pages/VehicleDetailPage'
import { VehiclesPage } from './pages/VehiclesPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { ZonesPage } from './pages/ZonesPage'
import { PlaceholderPage } from './pages/PlaceholderPage'
import { GpsTrackingPage } from './features/gps/pages/GpsTrackingPage'
import { GpsAlertsDashboard } from './features/gps/pages/GpsAlertsDashboard'
import { VehicleHistoryPage } from './features/gps/pages/VehicleHistoryPage'
import { GeofencePage } from './features/gps/pages/GeofencePage'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardLayout />}>
            <Route index element={<DashboardHomePage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="roles" element={<RolesPage />} />
            <Route path="fleet" element={<FleetPage />} />
            <Route path="vehicles" element={<VehiclesPage />} />
            <Route path="vehicles/:id" element={<VehicleDetailPage />} />
            <Route path="reservations" element={<ReservationsPage />} />
            <Route path="assignments" element={<AssignmentsPage />} />
            <Route path="technical-visits" element={<TechnicalVisitsPage />} />
            <Route path="breakdowns" element={<BreakdownsPage />} />
            <Route path="fuel-records" element={<FuelRecordsPage />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="settings" element={<PlaceholderPage title="Paramètres" description="Page paramètres en cours." />} />
            <Route path="notifications" element={<NotificationsPage />} />
            <Route path="zones" element={<ZonesPage />} />
          </Route>

          <Route path="/gps" element={<DashboardLayout />}>
            <Route path="tracking" element={<GpsTrackingPage />} />
            <Route path="alerts" element={<GpsAlertsDashboard />} />
            <Route path="history" element={<VehicleHistoryPage />} />
            <Route path="geofences" element={<GeofencePage />} />
          </Route>
        </Route>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

