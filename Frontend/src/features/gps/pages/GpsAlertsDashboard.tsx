import { useGpsWebSocket } from '../hooks/useGpsWebSocket'

export function GpsAlertsDashboard() {
  const { alerts } = useGpsWebSocket()

  const unackCount = alerts.filter((a) => !a.acknowledged).length

  return (
    <div className="dash-page gps-alerts-page">
      <h1>Alertes GPS</h1>
      <p className="dash-lead">
        Alertes temps réel (WebSocket). {unackCount} non acquittée(s).
      </p>

      {alerts.length === 0 ? (
        <p className="muted">Aucune alerte reçue pour l’instant.</p>
      ) : (
        <div className="card" style={{ padding: '1rem' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left' }}>
                <th>Type</th>
                <th>Véhicule</th>
                <th>Message</th>
                <th>Heure</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {alerts.slice(0, 50).map((a) => (
                <tr key={a.id} style={{ borderTop: '1px solid #e2e8f0' }}>
                  <td style={{ padding: '0.55rem 0.25rem' }}>
                    <span style={{ fontWeight: 600 }}>{a.type}</span>
                  </td>
                  <td style={{ padding: '0.55rem 0.25rem' }}>{a.vehicleId}</td>
                  <td style={{ padding: '0.55rem 0.25rem' }}>
                    {a.message}
                    {!a.acknowledged ? (
                      <span style={{ marginLeft: 8, color: '#b91c1c', fontWeight: 700 }}>
                        CRITIQUE
                      </span>
                    ) : null}
                  </td>
                  <td style={{ padding: '0.55rem 0.25rem' }}>
                    {new Date(a.createdAt).toLocaleString('fr-FR', {
                      dateStyle: 'short',
                      timeStyle: 'short',
                    })}
                  </td>
                  <td style={{ padding: '0.55rem 0.25rem' }}>
                    {a.acknowledged ? 'Acquittée' : 'Non acquittée'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

