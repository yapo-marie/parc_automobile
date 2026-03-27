import { useEffect, useMemo, useRef, useState } from 'react'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { GpsAlert, GpsPosition } from '../types'

function safeJsonParse<T>(text: string): T | null {
  try {
    return JSON.parse(text) as T
  } catch {
    return null
  }
}

export function useGpsWebSocket(vehicleId?: string) {
  const [positions, setPositions] = useState<Map<string, GpsPosition>>(() => new Map())
  const [alerts, setAlerts] = useState<GpsAlert[]>([])

  const activeVehicleId = useMemo(() => vehicleId?.trim() ?? undefined, [vehicleId])
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws/gps'),
      reconnectDelay: 3000,
      debug: () => {},
      onConnect: () => {
        // positions flotte
        client.subscribe('/topic/fleet', (msg: IMessage) => {
          const pos = safeJsonParse<GpsPosition>(msg.body)
          if (!pos) return
          setPositions((prev) => {
            const next = new Map(prev)
            next.set(pos.vehicleId, pos)
            return next
          })
        })

        // alerts globales
        client.subscribe('/topic/alerts', (msg: IMessage) => {
          const alert = safeJsonParse<GpsAlert>(msg.body)
          if (!alert) return
          setAlerts((prev) => {
            const next = [alert, ...prev]
            // on garde un historique court
            return next.slice(0, 100)
          })
        })

        // si subscription véhicule spécifique
        if (activeVehicleId) {
          client.subscribe(`/topic/gps/${activeVehicleId}`, (msg: IMessage) => {
            const pos = safeJsonParse<GpsPosition>(msg.body)
            if (!pos) return
            setPositions((prev) => {
              const next = new Map(prev)
              next.set(activeVehicleId, pos)
              return next
            })
          })
          client.subscribe(`/topic/alerts/${activeVehicleId}`, (msg: IMessage) => {
            const alert = safeJsonParse<GpsAlert>(msg.body)
            if (!alert) return
            setAlerts((prev) => {
              const next = [alert, ...prev]
              return next.slice(0, 100)
            })
          })
        }
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [activeVehicleId])

  return { positions, alerts }
}

