import { apiFetch } from './client'
import { readApiMessage } from './errors'
import type {
  CreateRolePayload,
  PermissionOptionDto,
  RoleDetailDto,
  SetRolePermissionsPayload,
  UpdateRolePayload,
} from '../types/role'

export async function fetchRolesDetail(): Promise<RoleDetailDto[]> {
  const res = await apiFetch('/api/roles')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as RoleDetailDto[]
}

export async function fetchPermissionOptions(): Promise<PermissionOptionDto[]> {
  const res = await apiFetch('/api/permissions')
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as PermissionOptionDto[]
}

export async function createRole(body: CreateRolePayload): Promise<RoleDetailDto> {
  const res = await apiFetch('/api/roles', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as RoleDetailDto
}

export async function updateRole(id: string, body: UpdateRolePayload): Promise<RoleDetailDto> {
  const res = await apiFetch(`/api/roles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as RoleDetailDto
}

export async function setRolePermissions(
  id: string,
  body: SetRolePermissionsPayload,
): Promise<RoleDetailDto> {
  const res = await apiFetch(`/api/roles/${id}/permissions`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as RoleDetailDto
}

export async function deleteRole(id: string): Promise<void> {
  const res = await apiFetch(`/api/roles/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}
