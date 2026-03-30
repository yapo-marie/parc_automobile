import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../../hooks/useNotifications'

export function NotificationBell() {
  const navigate = useNavigate()
  const { unreadCount } = useNotifications()
  return (
    <button type="button" className="notif-btn" onClick={() => navigate('/notifications')}>
      🔔
      {unreadCount > 0 ? (
        <span className="badge-notif">{unreadCount > 99 ? '99+' : unreadCount}</span>
      ) : null}
    </button>
  )
}

