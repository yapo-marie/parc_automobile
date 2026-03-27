import type {
  CreateFleetVehiclePayload,
  FleetStatsDto,
  FleetVehicleDto,
  FleetVehiclePageResponse,
  UpdateFleetVehiclePayload,
} from '../types/fleet'
import { apiFetch } from './client'
import { readApiMessage } from './errors'

function qs(params: Record<string, string | number | undefined | null>): string {
  const u = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === '') continue
    u.set(k, String(v))
  }
  return u.toString()
}

function stripNullMoney(body: Record<string, unknown>): Record<string, unknown> {
  const out: Record<string, unknown> = { ...body }
  for (const key of ['dailyCost', 'costPerKm', 'annualBudget']) {
    if (out[key] === '' || out[key] === undefined) {
      out[key] = null
    }
  }
  return out
}

export async function fetchFleetStats(): Promise<FleetStatsDto> {
  const res = await apiFetch('/api/fleet/stats')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FleetStatsDto
}

export async function fetchFleetVehicles(options: {
  page?: number
  size?: number
  administration?: string
  vehicleId?: string
}): Promise<FleetVehiclePageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 200,
    sort: 'administration,asc',
    administration: options.administration,
    vehicleId: options.vehicleId,
  })
  const res = await apiFetch(`/api/fleet?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FleetVehiclePageResponse
}

export async function fetchFleetVehicle(id: string): Promise<FleetVehicleDto> {
  const res = await apiFetch(`/api/fleet/${encodeURIComponent(id)}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FleetVehicleDto
}

export async function createFleetVehicle(body: CreateFleetVehiclePayload): Promise<FleetVehicleDto> {
  const res = await apiFetch('/api/fleet', {
    method: 'POST',
    body: JSON.stringify(stripNullMoney(body as unknown as Record<string, unknown>)),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FleetVehicleDto
}

export async function updateFleetVehicle(
  id: string,
  body: UpdateFleetVehiclePayload,
): Promise<FleetVehicleDto> {
  const res = await apiFetch(`/api/fleet/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(stripNullMoney(body as unknown as Record<string, unknown>)),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FleetVehicleDto
}

export async function deleteFleetVehicle(id: string): Promise<void> {
  const res = await apiFetch(`/api/fleet/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}
