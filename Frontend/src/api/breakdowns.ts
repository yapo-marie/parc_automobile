import type {
  BreakdownPageResponse,
  CreateBreakdownPayload,
  ResolveBreakdownPayload,
  BreakdownDto,
} from '../types/breakdown'
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

export async function fetchBreakdowns(options: {
  page?: number
  size?: number
  vehicleId?: string
  status?: string
}): Promise<BreakdownPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    vehicleId: options.vehicleId,
    status: options.status,
  })
  const res = await apiFetch(`/api/breakdowns?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as BreakdownPageResponse
}

export async function createBreakdown(body: CreateBreakdownPayload): Promise<BreakdownDto> {
  const res = await apiFetch('/api/breakdowns', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as BreakdownDto
}

export async function resolveBreakdown(
  id: string,
  body: ResolveBreakdownPayload,
): Promise<BreakdownDto> {
  const res = await apiFetch(`/api/breakdowns/${encodeURIComponent(id)}/resolve`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as BreakdownDto
}

