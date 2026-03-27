import type {
  CreateUserPayload,
  UpdateSelfProfilePayload,
  UserDto,
  UserPageResponse,
} from '../types/user'
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

export async function fetchMeProfile(): Promise<UserDto> {
  const res = await apiFetch('/api/users/me')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as UserDto
}

export async function updateMeProfile(body: UpdateSelfProfilePayload): Promise<UserDto> {
  const res = await apiFetch('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as UserDto
}

export async function changeMyPassword(currentPassword: string, newPassword: string): Promise<void> {
  const res = await apiFetch('/api/users/me/password', {
    method: 'PUT',
    body: JSON.stringify({ currentPassword, newPassword }),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function fetchUsers(options: {
  page?: number
  size?: number
  q?: string
  status?: string
  role?: string
}): Promise<UserPageResponse> {
  const query = qs({
    page: options.page ?? 0,
    size: options.size ?? 20,
    sort: 'lastname,asc',
    q: options.q,
    status: options.status,
    role: options.role,
  })
  const res = await apiFetch(`/api/users?${query}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as UserPageResponse
}

export async function fetchRoleNames(): Promise<string[]> {
  const res = await apiFetch('/api/roles/names')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as string[]
}

export async function createUser(body: CreateUserPayload): Promise<void> {
  const res = await apiFetch('/api/users', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function patchUserStatus(id: string, status: string): Promise<void> {
  const res = await apiFetch(`/api/users/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function deleteUser(id: string): Promise<void> {
  const res = await apiFetch(`/api/users/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}
