import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { apiFetch } from '../../api/client'
import { useAuthStore } from '../../store/authStore'

type Me = { firstname: string; lastname: string }

export function UserDropdown() {
  const [open, setOpen] = useState(false)
  const [me, setMe] = useState<Me | null>(null)
  const refreshToken = useAuthStore((s) => s.refreshToken)
  const clear = useAuthStore((s) => s.clear)
  const navigate = useNavigate()

  useEffect(() => {
    void (async () => {
      const res = await apiFetch('/api/me')
      if (res.ok) setMe((await res.json()) as Me)
    })()
  }, [])

  async function logout() {
    if (refreshToken) {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      }).catch(() => {})
    }
    clear()
    navigate('/login', { replace: true })
  }

  const label = me ? `${me.firstname} ${me.lastname}` : 'Mon compte'
  return (
    <div className="userdd">
      <button type="button" className="userdd__btn" onClick={() => setOpen((v) => !v)}>
        <span className="userdd__avatar">{label.slice(0, 2).toUpperCase()}</span>
        <span>{label}</span>
        <span>▾</span>
      </button>
      {open ? (
        <div className="userdd__menu">
          <Link to="/dashboard/profile" onClick={() => setOpen(false)}>
            Mon profil
          </Link>
          <Link to="/dashboard/settings" onClick={() => setOpen(false)}>
            Paramètres
          </Link>
          <button type="button" onClick={() => void logout()}>
            Déconnexion
          </button>
        </div>
      ) : null}
    </div>
  )
}

