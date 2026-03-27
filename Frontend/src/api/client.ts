import { useAuthStore } from '../store/authStore'
import type { AuthResponse } from '../types/auth'

function forceLoginRedirect(): void {
  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}

async function tryRefresh(refreshToken: string): Promise<AuthResponse | null> {
  const refreshRes = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!refreshRes.ok) return null
  return (await refreshRes.json()) as AuthResponse
}

/**
 * API authentifiée : ajoute le Bearer, tente un refresh si access absent ou si 401.
 */
export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const store = useAuthStore.getState()
  let { accessToken, refreshToken, setFromAuthResponse, clear } = store

  /* Après rehydratation Zustand, l'access peut être encore null une frame ; le refresh suffit. */
  if (!accessToken && refreshToken) {
    const data = await tryRefresh(refreshToken)
    if (data) {
      setFromAuthResponse(data)
      accessToken = data.accessToken
      refreshToken = data.refreshToken
    } else {
      clear()
      forceLoginRedirect()
    }
  }

  const doRequest = async (token: string | null) => {
    const headers = new Headers(init.headers)
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
    if (
      init.body != null &&
      !headers.has('Content-Type') &&
      !(init.body instanceof FormData)
    ) {
      headers.set('Content-Type', 'application/json')
    }
    return fetch(path, { ...init, headers })
  }

  let res = await doRequest(accessToken)

  if (res.status === 401) {
    const rt = useAuthStore.getState().refreshToken
    if (!rt) {
      clear()
      forceLoginRedirect()
      return res
    }
    const data = await tryRefresh(rt)
    if (!data) {
      clear()
      forceLoginRedirect()
      return res
    }
    setFromAuthResponse(data)
    res = await doRequest(data.accessToken)
    if (res.status === 401) {
      clear()
      forceLoginRedirect()
    }
  }

  return res
}
