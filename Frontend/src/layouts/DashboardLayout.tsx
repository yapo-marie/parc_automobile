import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { NotificationBell } from '../components/navbar/NotificationBell'
import { UserDropdown } from '../components/navbar/UserDropdown'

import {
  LayoutDashboard,
  Globe,
  CarFront,
  MapPin,
  CalendarCheck,
  UserRoundCheck,
  BellRing,
  Wrench,
  Users,
  TriangleAlert,
  Fuel,
  History,
  Map,
  Activity,
  PanelLeftClose,
  Search
} from 'lucide-react'

const nav = [
  { to: '/dashboard', end: true, label: 'Dashboard', icon: <LayoutDashboard size={16} /> },
  { to: '/dashboard/fleet', label: 'Flotte Globale', icon: <Globe size={16} /> },
  { to: '/dashboard/vehicles', label: 'Parc Automobile', icon: <CarFront size={16} /> },
  { to: '/gps/tracking', label: 'Géolocalisation', icon: <MapPin size={16} /> },
  { to: '/dashboard/reservations', label: 'Réservations', icon: <CalendarCheck size={16} /> },
  { to: '/dashboard/assignments', label: 'Attributions', icon: <UserRoundCheck size={16} /> },
  { to: '/gps/alerts', label: 'Alertes GPS', icon: <BellRing size={16} /> },
  { to: '/dashboard/technical-visits', label: 'Visites techniques', icon: <Wrench size={16} /> },
  { to: '/dashboard/drivers', label: 'Chauffeurs', icon: <Users size={16} /> },
  { to: '/dashboard/breakdowns', label: 'Pannes', icon: <TriangleAlert size={16} /> },
  { to: '/dashboard/fuel-records', label: 'Carburant', icon: <Fuel size={16} /> },
  { to: '/gps/history', label: 'Historique GPS', icon: <History size={16} /> },
  { to: '/gps/geofences', label: 'Géo-clôtures', icon: <Map size={16} /> },
  { to: '/dashboard/zones', label: 'Zones', icon: <Activity size={16} /> },
]

const adminNav = [
  { to: '/dashboard/users', label: 'Utilisateurs' },
  { to: '/dashboard/roles', label: 'Rôles' },
]

export function DashboardLayout() {
  const location = useLocation()
  
  // Extraire le titre de la page actuelle
  const currentNav = [...nav, ...adminNav].find((n) => {
    if ('end' in n && n.end) {
      return location.pathname === n.to
    }
    return location.pathname.startsWith(n.to)
  })
  const pageTitle = currentNav ? currentNav.label : 'Tableau de bord'

  return (
    <div className="shell">
      <aside className="shell__nav">
        <div className="shell__brand">
          <div className="brand-logo">
            <CarFront size={24} strokeWidth={2.5} />
          </div>
          <div className="brand-text">
            <strong>AUTOMOBILIER</strong>
            <span>Gestion de Flotte</span>
          </div>
        </div>
        
        <div className="nav-group-label">NAVIGATION</div>
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
              <span className="link-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="nav-group-label" style={{ marginTop: '1rem' }}>SYSTÈME</div>
        <nav className="shell__links">
          {adminNav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                'shell__link' + (isActive || location.pathname.startsWith('/dashboard/users') || location.pathname.startsWith('/dashboard/roles') ? ' shell__link--active' : '')
              }
            >
              <span className="link-icon">
                <LayoutDashboard size={16} />
              </span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="shell__main">
        <div className="shell__topbar-wrap">
          <header className="shell__topbar">
            {/* Gauche : toggle + titre */}
            <div className="shell__topbar-left">
              <button className="sidebar-toggle" aria-label="Toggle sidebar">
                <PanelLeftClose size={20} />
              </button>
              <span className="topbar-title">{pageTitle}</span>
            </div>

            {/* Centre : barre de recherche */}
            <div className="topbar-search">
              <Search size={15} />
              <input type="text" placeholder="Rechercher un véhicule, chauffeur..." />
            </div>

            {/* Droite : actions */}
            <div className="shell__topbar-actions">
              <NotificationBell />
              <UserDropdown />
            </div>
          </header>
        </div>
        <div className="shell__content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

