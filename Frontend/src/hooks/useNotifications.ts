import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { fetchUnreadCount } from '../api/notifications'
import { useAuthStore } from '../store/authStore'

function parseJwtPayload(token: string): { sub?: string } | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    return JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))) as { sub?: string }
  } catch {
    return null
  }
}

export function useNotifications() {
  const [unreadCount, setUnreadCount] = useState(0)
  const accessToken = useAuthStore((s) => s.accessToken)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    void fetchUnreadCount().then(setUnreadCount).catch(() => {})
  }, [])

  useEffect(() => {
    if (!accessToken) return
    const payload = parseJwtPayload(accessToken)
    const sub = payload?.sub
    if (!sub) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws/gps'),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${sub}`, () => {
          setUnreadCount((c) => c + 1)
        })
      },
    })
    client.activate()
    clientRef.current = client
    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [accessToken])

  return { unreadCount, setUnreadCount }
}

