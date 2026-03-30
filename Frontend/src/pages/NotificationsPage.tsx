import { useEffect, useState } from 'react'
import {
  deleteNotification,
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  type NotificationDto,
} from '../api/notifications'

export function NotificationsPage() {
  const [rows, setRows] = useState<NotificationDto[]>([])
  const [filter, setFilter] = useState<'all' | 'unread'>('all')
  const [error, setError] = useState<string | null>(null)

  async function load() {
    try {
      const page = await fetchNotifications(filter === 'all' ? undefined : false)
      setRows(page.content)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter])

  return (
    <div className="dash-page">
      <h1>Notifications</h1>
      {error ? <p className="alert alert--error">{error}</p> : null}
      <div className="card card--flat" style={{ marginBottom: 12 }}>
        <button type="button" className="btn-inline" onClick={() => setFilter('all')}>
          Toutes
        </button>{' '}
        <button type="button" className="btn-inline" onClick={() => setFilter('unread')}>
          Non lues
        </button>{' '}
        <button
          type="button"
          className="btn-secondary"
          onClick={() => void markAllNotificationsRead().then(load)}
        >
          Tout marquer lu
        </button>
      </div>
      <div className="card card--flat">
        {rows.map((n) => (
          <div key={n.id} style={{ padding: '0.75rem', borderBottom: '1px solid #e2e8f0' }}>
            <strong>{n.title}</strong> <small>[{n.type}]</small>
            <p>{n.message}</p>
            <small>{new Date(n.createdAt).toLocaleString('fr-FR')}</small>
            <div>
              {!n.read ? (
                <button type="button" className="linkish" onClick={() => void markNotificationRead(n.id).then(load)}>
                  Marquer lue
                </button>
              ) : null}
              <button type="button" className="linkish danger" onClick={() => void deleteNotification(n.id).then(load)}>
                Supprimer
              </button>
            </div>
          </div>
        ))}
        {!rows.length ? <p className="muted">Aucune notification.</p> : null}
      </div>
    </div>
  )
}

