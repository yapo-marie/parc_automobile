import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { FuelLevelBar } from '../components/fuel/FuelLevelBar'

type FuelLive = {
  vehicleId: string
  plateNumber: string
  vehicleLabel: string
  photoUrl: string | null
  fuelLevel: number | null
  lastSeen: string | null
}

type FuelHistory = {
  recordedAt: string
  fuelLevel: number | null
  speed: number | null
}

type FuelAlert = {
  id: string
  vehicleId: string
  message: string
  createdAt: string
}

export function FuelRecordsPage() {
  const [rows, setRows] = useState<FuelLive[]>([])
  const [alerts, setAlerts] = useState<FuelAlert[]>([])
  const [vehicleId, setVehicleId] = useState('')
  const [history, setHistory] = useState<FuelHistory[]>([])
  const [error, setError] = useState<string | null>(null)

  async function load() {
    try {
      const liveRes = await apiFetch('/api/fuel/live')
      const alertsRes = await apiFetch('/api/fuel/alerts')
      if (!liveRes.ok || !alertsRes.ok) throw new Error('Chargement impossible')
      setRows((await liveRes.json()) as FuelLive[])
      setAlerts((await alertsRes.json()) as FuelAlert[])
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function loadHistory(id: string) {
    setVehicleId(id)
    if (!id) {
      setHistory([])
      return
    }
    const res = await apiFetch(`/api/fuel/history/${encodeURIComponent(id)}`)
    if (!res.ok) return
    setHistory((await res.json()) as FuelHistory[])
  }

  useEffect(() => {
    void load()
  }, [])

  const lowCount = rows.filter((r) => (r.fuelLevel ?? 0) < 20).length
  const avg = rows.length
    ? Math.round(rows.reduce((acc, r) => acc + (r.fuelLevel ?? 0), 0) / rows.length)
    : 0

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Carburant GPS</h1>
      <p className="dash-lead">Lecture seule depuis le boîtier GPS GT903K.</p>
      {error ? <p className="alert alert--error">{error}</p> : null}

      <section className="grid-kpi">
        <article className="kpi">
          <span className="kpi__label">Véhicules niveau bas</span>
          <span className="kpi__value">{lowCount}</span>
        </article>
        <article className="kpi">
          <span className="kpi__label">Alertes actives</span>
          <span className="kpi__value">{alerts.length}</span>
        </article>
        <article className="kpi">
          <span className="kpi__label">Moyenne flotte</span>
          <span className="kpi__value">{avg}%</span>
        </article>
      </section>

      <section className="card card--flat">
        <h2 className="users-h2">Niveau carburant temps réel</h2>
        {rows.map((r) => (
          <div key={r.vehicleId} style={{ display: 'grid', gridTemplateColumns: '60px 1fr 180px', alignItems: 'center', gap: 12, padding: '0.55rem 0', borderBottom: '1px solid #e2e8f0' }}>
            {r.photoUrl ? (
              <img src={r.photoUrl} alt={r.plateNumber} style={{ width: 48, height: 36, objectFit: 'cover', borderRadius: 6 }} />
            ) : (
              <div style={{ width: 48, textAlign: 'center' }}>🚗</div>
            )}
            <div>
              <strong>{r.vehicleLabel}</strong> <code>{r.plateNumber}</code>
            </div>
            <FuelLevelBar value={r.fuelLevel} />
          </div>
        ))}
      </section>

      <section className="card card--flat">
        <h2 className="users-h2">Historique GPS par véhicule</h2>
        <select value={vehicleId} onChange={(e) => void loadHistory(e.target.value)}>
          <option value="">Choisir un véhicule</option>
          {rows.map((r) => (
            <option key={r.vehicleId} value={r.vehicleId}>
              {r.plateNumber} — {r.vehicleLabel}
            </option>
          ))}
        </select>
        <div style={{ marginTop: 10 }}>
          {history.slice(0, 20).map((p, i) => (
            <div key={i} className="muted">
              {new Date(p.recordedAt).toLocaleString('fr-FR')} — {p.fuelLevel ?? '—'}%
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

