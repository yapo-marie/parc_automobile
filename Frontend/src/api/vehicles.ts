import type { VehicleDto, VehiclePageResponse, VehicleWritePayload } from '../types/vehicle'
import { apiFetch } from './client'
import { readApiMessage } from './errors'

function finiteIntOrNull(v: number | null | undefined): number | null {
  if (v == null) return null
  if (typeof v !== 'number' || !Number.isFinite(v)) return null
  return Math.trunc(v)
}

function finiteLongOrNull(v: number | null | undefined): number | null {
  if (v == null) return null
  if (typeof v !== 'number' || !Number.isFinite(v)) return null
  return Math.max(0, Math.floor(v))
}

function yearOrNull(v: number | null | undefined): number | null {
  if (v == null) return null
  if (typeof v !== 'number' || !Number.isFinite(v)) return null
  const y = Math.round(v)
  if (y < 1900 || y > 2100) return null
  return y
}

/** Normalise les nombres pour Jackson (Long/Integer sans décimales). */
function toVehicleApiBody(body: VehicleWritePayload): VehicleWritePayload {
  return {
    plateNumber: body.plateNumber.trim(),
    brand: body.brand.trim(),
    model: body.model.trim(),
    year: yearOrNull(body.year),
    color: body.color?.trim() || null,
    imei: body.imei?.trim() || null,
    category: body.category,
    fuelType: body.fuelType,
    mileage: finiteLongOrNull(body.mileage),
    power: finiteIntOrNull(body.power),
    seats: finiteIntOrNull(body.seats),
    acquisitionDate: body.acquisitionDate?.trim() || null,
    acquisitionValue:
      body.acquisitionValue != null && Number.isFinite(body.acquisitionValue)
        ? body.acquisitionValue
        : null,
    insuranceExpiry: body.insuranceExpiry?.trim() || null,
    availability: body.availability,
    status: body.status,
    photoUrl: body.photoUrl?.trim() || null,
    notes: body.notes?.trim() || null,
  }
}

function qs(params: Record<string, string | number | undefined | null>): string {
  const u = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === '') continue
    u.set(k, String(v))
  }
  return u.toString()
}

export async function fetchVehicle(id: string): Promise<VehicleDto> {
  const res = await apiFetch(`/api/vehicles/${encodeURIComponent(id)}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as VehicleDto
}

export async function fetchVehicles(options: {
  page?: number
  size?: number
  q?: string
  category?: string
  availability?: string
  status?: string
}): Promise<VehiclePageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'plateNumber,asc',
    q: options.q,
    category: options.category,
    availability: options.availability,
    status: options.status,
  })
  const res = await apiFetch(`/api/vehicles?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as VehiclePageResponse
}

export async function createVehicle(body: VehicleWritePayload): Promise<VehicleDto> {
  const res = await apiFetch('/api/vehicles', {
    method: 'POST',
    body: JSON.stringify(toVehicleApiBody(body)),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as VehicleDto
}

export async function updateVehicle(id: string, body: VehicleWritePayload): Promise<VehicleDto> {
  const res = await apiFetch(`/api/vehicles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(toVehicleApiBody(body)),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as VehicleDto
}

export async function patchVehicleAvailability(id: string, availability: string): Promise<void> {
  const res = await apiFetch(`/api/vehicles/${id}/availability`, {
    method: 'PATCH',
    body: JSON.stringify({ availability }),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function deleteVehicle(id: string): Promise<void> {
  const res = await apiFetch(`/api/vehicles/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function uploadVehiclePhoto(id: string, file: File): Promise<VehicleDto> {
  const fd = new FormData()
  fd.append('file', file)
  const res = await fetch(`/api/vehicles/${encodeURIComponent(id)}/photo`, {
    method: 'POST',
    body: fd,
    credentials: 'include',
    headers: (() => {
      const raw = localStorage.getItem('auth-storage')
      const headers: Record<string, string> = {}
      if (!raw) return headers
      try {
        const parsed = JSON.parse(raw) as { state?: { accessToken?: string | null } }
        const token = parsed.state?.accessToken
        if (token) headers.Authorization = `Bearer ${token}`
        return headers
      } catch {
        return headers
      }
    })(),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as VehicleDto
}
