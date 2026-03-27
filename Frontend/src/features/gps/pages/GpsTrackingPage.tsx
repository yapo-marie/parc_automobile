import { FleetMap } from '../components/FleetMap'
import { useGpsWebSocket } from '../hooks/useGpsWebSocket'

export function GpsTrackingPage() {
  const { positions } = useGpsWebSocket()

  const onlineCount = Array.from(positions.values()).filter((p) => p.recordedAt).length

  return (
    <div className="dash-page gps-page">
      <h1>GPS & Tracking</h1>
      <p className="dash-lead">
        Positions temps réel (markers). Alerte via WebSocket + dashboard.
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: '18rem 1fr', gap: '1rem' }}>
        <aside className="card" style={{ padding: '1rem' }}>
          <h2 style={{ margin: 0, fontSize: '1rem' }}>Véhicules</h2>
          <p className="muted" style={{ marginTop: '0.35rem' }}>
            {onlineCount} position(s) en mémoire
          </p>
          <div style={{ marginTop: '0.75rem' }}>
            {Array.from(positions.values())
              .sort((a, b) => b.recordedAt.localeCompare(a.recordedAt))
              .slice(0, 20)
              .map((p) => (
                <div key={p.vehicleId} style={{ marginBottom: '0.6rem' }}>
                  <strong>{p.vehiclePlate}</strong>
                  <div className="muted" style={{ fontSize: '0.8rem' }}>
                    {Math.round(p.speed)} km/h · {p.ignitionOn ? 'ACC ON' : 'ACC OFF'}
                  </div>
                </div>
              ))}
          </div>
        </aside>

        <main>
          <FleetMap />
        </main>
      </div>
    </div>
  )
}

