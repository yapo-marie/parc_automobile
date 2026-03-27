import type {
  CreateReservationPayload,
  ReservationPageResponse,
} from '../types/reservation'
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

export async function fetchAllReservations(options: {
  page?: number
  size?: number
  status?: string
  vehicleId?: string
}): Promise<ReservationPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'startDatetime,desc',
    status: options.status,
    vehicleId: options.vehicleId,
  })
  const res = await apiFetch(`/api/reservations?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as ReservationPageResponse
}

export async function fetchMyReservations(options: { page?: number; size?: number }): Promise<ReservationPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'startDatetime,desc',
  })
  const res = await apiFetch(`/api/reservations/my?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as ReservationPageResponse
}

export async function createReservation(body: CreateReservationPayload): Promise<void> {
  const res = await apiFetch('/api/reservations', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function confirmReservation(id: string): Promise<void> {
  const res = await apiFetch(`/api/reservations/${encodeURIComponent(id)}/confirm`, {
    method: 'PATCH',
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function rejectReservation(id: string, reason: string): Promise<void> {
  const res = await apiFetch(`/api/reservations/${encodeURIComponent(id)}/reject`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function cancelReservation(id: string, reason?: string | null): Promise<void> {
  const init: RequestInit = { method: 'PATCH' }
  if (reason != null && reason.trim() !== '') {
    init.body = JSON.stringify({ reason: reason.trim() })
  }
  const res = await apiFetch(`/api/reservations/${encodeURIComponent(id)}/cancel`, init)
  if (!res.ok) throw new Error(await readApiMessage(res))
}
