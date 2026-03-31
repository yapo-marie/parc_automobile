import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../../hooks/useNotifications'
import { Bell } from 'lucide-react'

export function NotificationBell() {
  const navigate = useNavigate()
  const { unreadCount } = useNotifications()

  return (
    <button
      type="button"
      className="notif-btn"
      aria-label="Notifications"
      title="Notifications"
      onClick={() => navigate('/dashboard/notifications')}
    >
      <Bell size={18} strokeWidth={1.75} />
      {unreadCount > 0 && (
        <span className="badge-notif">{unreadCount > 99 ? '99+' : unreadCount}</span>
      )}
    </button>
  )
}
