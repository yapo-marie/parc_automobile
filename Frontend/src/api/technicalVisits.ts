import type {
  CreateTechnicalVisitPayload,
  TechnicalVisitPageResponse,
  TechnicalVisitResponse,
  UpdateTechnicalVisitPayload,
} from '../types/technicalVisit'
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

export async function fetchTechnicalVisits(options: {
  page?: number
  size?: number
  vehicleId?: string
  type?: string
  result?: string
}): Promise<TechnicalVisitPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    vehicleId: options.vehicleId,
    type: options.type,
    result: options.result,
  })
  const res = await apiFetch(`/api/technical-visits?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as TechnicalVisitPageResponse
}

export async function fetchTechnicalVisitById(id: string): Promise<TechnicalVisitResponse> {
  const res = await apiFetch(`/api/technical-visits/${encodeURIComponent(id)}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as TechnicalVisitResponse
}

export async function createTechnicalVisit(body: CreateTechnicalVisitPayload): Promise<void> {
  const res = await apiFetch('/api/technical-visits', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function updateTechnicalVisit(
  id: string,
  body: UpdateTechnicalVisitPayload,
): Promise<void> {
  const res = await apiFetch(`/api/technical-visits/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

