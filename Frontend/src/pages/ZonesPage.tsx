import { useEffect, useState } from 'react'
import { fetchZones, type ZoneDto } from '../api/zones'

export function ZonesPage() {
  const [zones, setZones] = useState<ZoneDto[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void (async () => {
      try {
        setZones(await fetchZones())
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erreur')
      }
    })()
  }, [])

  return (
    <div className="dash-page">
      <h1>Gestion des zones</h1>
      {error ? <p className="alert alert--error">{error}</p> : null}
      <div className="card card--flat">
        {zones.map((z) => (
          <div key={z.id} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '0.5rem 0' }}>
            <span style={{ width: 12, height: 12, borderRadius: '50%', background: z.color, display: 'inline-block' }} />
            <strong>{z.name}</strong>
            <span className="muted">· {z.vehicleIds.length} véhicule(s) · max {z.maxSpeedKmh || 0} km/h</span>
          </div>
        ))}
        {!zones.length ? <p className="muted">Aucune zone active.</p> : null}
      </div>
    </div>
  )
}

