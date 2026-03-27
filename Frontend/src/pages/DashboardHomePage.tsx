import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { useAuthStore } from '../store/authStore'
import type { MeResponse } from '../types/auth'

export function DashboardHomePage() {
  const mustChangePassword = useAuthStore((s) => s.mustChangePassword)
  const [me, setMe] = useState<MeResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setError(null)
      const res = await apiFetch('/api/me')
      if (cancelled) return
      if (!res.ok) {
        setError(`Impossible de charger le profil (${res.status})`)
        setMe(null)
        return
      }
      setMe((await res.json()) as MeResponse)
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const permCount = me?.authorities?.length ?? 0

  return (
    <div className="dash-page">
      <h1>Tableau de bord</h1>
      <p className="dash-lead">
        Bienvenue{me ? `, ${me.email}` : ''}. Vue d’ensemble du MVP FleetPro (prochaines
        livraisons : KPI, graphiques, alertes).
      </p>

      {mustChangePassword ? (
        <div className="alert alert--warn">
          <strong>Mot de passe à changer</strong> — Le CDC prévoit un changement obligatoire à la
          première connexion ; l’écran dédié arrive au sprint « profil / sécurité ».
        </div>
      ) : null}

      {error ? <p className="alert alert--error">{error}</p> : null}

      <section className="grid-kpi">
        <article className="kpi">
          <span className="kpi__label">Permissions effectives</span>
          <span className="kpi__value">{permCount}</span>
        </article>
        <article className="kpi">
          <span className="kpi__label">Modules MVP</span>
          <span className="kpi__value kpi__value--sm">En cours</span>
        </article>
        <article className="kpi">
          <span className="kpi__label">Raccourcis</span>
          <span className="kpi__value kpi__value--sm">
            <Link to="/dashboard/users">Utilisateurs</Link>
            {' · '}
            <Link to="/dashboard/roles">Rôles</Link>
            {' · '}
            <Link to="/dashboard/fleet">Flotte</Link>
            {' · '}
            <Link to="/dashboard/vehicles">Véhicules</Link>
            {' · '}
            <Link to="/dashboard/reservations">Réservations</Link>
            {' · '}
            <Link to="/dashboard/fuel-records">Carburant</Link>
          </span>
        </article>
      </section>

      {me ? (
        <details className="card card--flat">
          <summary>Détail technique (GET /api/me)</summary>
          <pre className="pre pre--tight">{JSON.stringify(me, null, 2)}</pre>
        </details>
      ) : null}
    </div>
  )
}
