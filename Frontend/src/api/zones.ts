import { apiFetch } from './client'
import { readApiMessage } from './errors'

export type ZoneDto = {
  id: string
  name: string
  description: string | null
  color: string
  type: 'CIRCLE' | 'POLYGON'
  centerLat: number | null
  centerLng: number | null
  radiusMeters: number | null
  polygonCoordinates: string | null
  maxSpeedKmh: number | null
  active: boolean
  vehicleIds: string[]
  createdAt: string
}

export async function fetchZones(): Promise<ZoneDto[]> {
  const res = await apiFetch('/api/zones')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as ZoneDto[]
}

