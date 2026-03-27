import type {
  AssignmentPageResponse,
  CreateAssignmentPayload,
  CreatePoolAssignmentPayload,
  EndAssignmentPayload,
  WithdrawAssignmentPayload,
} from '../types/assignment'
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

export async function fetchAllAssignments(options: {
  page?: number
  size?: number
  status?: string
  vehicleId?: string
  driverId?: string
}): Promise<AssignmentPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'startDate,desc',
    status: options.status,
    vehicleId: options.vehicleId,
    driverId: options.driverId,
  })
  const res = await apiFetch(`/api/assignments?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as AssignmentPageResponse
}

export async function fetchMyAssignments(options: {
  page?: number
  size?: number
}): Promise<AssignmentPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'startDate,desc',
  })
  const res = await apiFetch(`/api/assignments/my?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as AssignmentPageResponse
}

export async function createAssignment(body: CreateAssignmentPayload): Promise<void> {
  const res = await apiFetch('/api/assignments', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function createPoolAssignment(body: CreatePoolAssignmentPayload): Promise<void> {
  const res = await apiFetch('/api/assignments/pool', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function endAssignment(id: string, body: EndAssignmentPayload): Promise<void> {
  const res = await apiFetch(`/api/assignments/${encodeURIComponent(id)}/end`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function withdrawAssignment(
  id: string,
  body: WithdrawAssignmentPayload,
): Promise<void> {
  const res = await apiFetch(`/api/assignments/${encodeURIComponent(id)}/withdraw`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

