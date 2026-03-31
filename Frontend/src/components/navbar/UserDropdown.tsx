import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { apiFetch } from '../../api/client'
import { useAuthStore } from '../../store/authStore'

type Me = {
  firstname?: string
  firstname_?: string
  firstName?: string
  lastname?: string
  lastName?: string
  username?: string
  email?: string
}

export function UserDropdown() {
  const [open, setOpen] = useState(false)
  const [me, setMe] = useState<Me | null>(null)
  const [loading, setLoading] = useState(true)
  const refreshToken = useAuthStore((s) => s.refreshToken)
  const clear = useAuthStore((s) => s.clear)
  const navigate = useNavigate()
  const retryRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    let cancelled = false
    let attempt = 0

    async function fetchMe() {
      try {
        const res = await apiFetch('/api/me')
        if (cancelled) return
        if (res.ok) {
          setMe((await res.json()) as Me)
          setLoading(false)
        } else if (attempt < 5) {
          attempt++
          retryRef.current = setTimeout(fetchMe, 2000 * attempt)
        } else {
          setLoading(false)
        }
      } catch {
        if (cancelled) return
        if (attempt < 5) {
          attempt++
          retryRef.current = setTimeout(fetchMe, 2000 * attempt)
        } else {
          setLoading(false)
        }
      }
    }

    void fetchMe()
    return () => {
      cancelled = true
      if (retryRef.current) clearTimeout(retryRef.current)
    }
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

  // Compatibilité camelCase / lowercase selon ce que renvoie le backend
  const first = me?.firstname ?? me?.firstName ?? ''
  const last = me?.lastname ?? me?.lastName ?? ''
  const displayName = me?.username ?? me?.email ?? null

  const initials = first || last
    ? `${first.charAt(0)}${last.charAt(0)}`.toUpperCase().trim() || '?'
    : displayName?.charAt(0).toUpperCase() ?? '?'

  const label = first || last
    ? `${first} ${last}`.trim()
    : displayName

  return (
    <div className="userdd">
      <button type="button" className="userdd__btn" onClick={() => setOpen((v) => !v)}>
        <span className="userdd__avatar" title={label ?? 'Chargement...'}>
          {loading ? <span style={{ fontSize: '0.65rem', opacity: 0.7 }}>…</span> : initials}
        </span>
        {label && <span className="userdd__name">{label}</span>}
        <span className="userdd__caret">▾</span>
      </button>
      {open && (
        <div className="userdd__menu">
          {label && (
            <div className="userdd__menu-header">
              <strong>{label}</strong>
            </div>
          )}
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
      )}
    </div>
  )
}
