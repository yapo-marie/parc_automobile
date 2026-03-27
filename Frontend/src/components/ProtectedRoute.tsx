import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useHydratedAuth } from '../hooks/useHydratedAuth'

const PROFILE_PATH = '/dashboard/profile'

export function ProtectedRoute() {
  const hydrated = useHydratedAuth()
  const location = useLocation()
  const accessToken = useAuthStore((s) => s.accessToken)
  const mustChangePassword = useAuthStore((s) => s.mustChangePassword)

  if (!hydrated) {
    return (
      <div className="app app--center">
        <p className="muted">Chargement de la session…</p>
      </div>
    )
  }

  if (!accessToken) {
    return <Navigate to="/login" replace />
  }

  if (mustChangePassword && location.pathname !== PROFILE_PATH) {
    return <Navigate to={PROFILE_PATH} replace />
  }

  return <Outlet />
}
