import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { NotificationBell } from '../components/navbar/NotificationBell'
import { UserDropdown } from '../components/navbar/UserDropdown'

const nav = [
  { to: '/dashboard', end: true, label: 'Tableau de bord' },
  { to: '/dashboard/users', label: 'Utilisateurs' },
  { to: '/dashboard/roles', label: 'Rôles' },
  { to: '/dashboard/fleet', label: 'Flotte' },
  { to: '/dashboard/vehicles', label: 'Véhicules' },
  { to: '/dashboard/reservations', label: 'Réservations' },
  { to: '/dashboard/assignments', label: 'Attributions' },
  { to: '/dashboard/technical-visits', label: 'Visites techniques' },
  { to: '/dashboard/breakdowns', label: 'Pannes' },
  { to: '/dashboard/fuel-records', label: 'Carburant' },
  { to: '/gps/tracking', end: true, label: 'GPS & Tracking' },
  { to: '/gps/alerts', end: true, label: 'Alertes GPS' },
  { to: '/gps/history', end: true, label: 'Historique GPS' },
  { to: '/gps/geofences', end: true, label: 'Géo-clôtures' },
  { to: '/dashboard/zones', label: 'Zones' },
]

export function DashboardLayout() {
  const location = useLocation()

  return (
    <div className="shell">
      <aside className="shell__nav">
        <div className="shell__brand">FleetPro</div>
        <nav className="shell__links">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => {
                const fleetNested =
                  item.to === '/dashboard/fleet' &&
                  location.pathname.startsWith('/dashboard/fleet')
                const vehiclesNested =
                  item.to === '/dashboard/vehicles' &&
                  location.pathname.startsWith('/dashboard/vehicles')
                const reservationsNested =
                  item.to === '/dashboard/reservations' &&
                  location.pathname.startsWith('/dashboard/reservations')
                const assignmentsNested =
                  item.to === '/dashboard/assignments' &&
                  location.pathname.startsWith('/dashboard/assignments')
                const technicalVisitsNested =
                  item.to === '/dashboard/technical-visits' &&
                  location.pathname.startsWith('/dashboard/technical-visits')
                const breakdownsNested =
                  item.to === '/dashboard/breakdowns' &&
                  location.pathname.startsWith('/dashboard/breakdowns')
                const fuelRecordsNested =
                  item.to === '/dashboard/fuel-records' &&
                  location.pathname.startsWith('/dashboard/fuel-records')
                const rolesNested =
                  item.to === '/dashboard/roles' &&
                  location.pathname.startsWith('/dashboard/roles')
                return (
                  'shell__link' +
                  (isActive ||
                  fleetNested ||
                  vehiclesNested ||
                  reservationsNested ||
                  assignmentsNested ||
                  technicalVisitsNested ||
                  breakdownsNested ||
                  fuelRecordsNested ||
                  rolesNested
                    ? ' shell__link--active'
                    : '')
                )
              }}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="shell__main">
        <div className="shell__topbar">
          <div className="shell__search">
            <input type="search" placeholder="Recherche globale..." />
          </div>
          <div className="shell__topbar-actions">
            <NotificationBell />
            <UserDropdown />
          </div>
        </div>
        <Outlet />
      </div>
    </div>
  )
}

