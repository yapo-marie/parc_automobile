import { apiFetch } from './client'
import { readApiMessage } from './errors'
import type {
  CreateFuelRecordPayload,
  FuelRecordDto,
  FuelRecordPageResponse,
  FuelRecordStatsDto,
} from '../types/fuelRecord'

function qs(params: Record<string, string | number | undefined | null>): string {
  const u = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === '') continue
    u.set(k, String(v))
  }
  return u.toString()
}

export async function fetchFuelRecords(options: {
  page?: number
  size?: number
  vehicleId?: string
  fromDate?: string
  toDate?: string
}): Promise<FuelRecordPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    vehicleId: options.vehicleId,
    fromDate: options.fromDate,
    toDate: options.toDate,
  })
  const res = await apiFetch(`/api/fuel-records?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FuelRecordPageResponse
}

export async function fetchFuelStats(vehicleId: string): Promise<FuelRecordStatsDto> {
  const query = qs({ vehicleId })
  const res = await apiFetch(`/api/fuel-records/stats?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FuelRecordStatsDto
}

export async function createFuelRecord(body: CreateFuelRecordPayload): Promise<FuelRecordDto> {
  const res = await apiFetch('/api/fuel-records', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as FuelRecordDto
}

