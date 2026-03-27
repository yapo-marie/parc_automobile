export type RoleDetailDto = {
  id: string
  name: string
  description: string | null
  permissionCodes: string[]
  assignedUserCount: number
}

export type PermissionOptionDto = {
  code: string
  description: string
}

export type CreateRolePayload = {
  name: string
  description?: string | null
  permissionCodes?: string[]
}

export type UpdateRolePayload = {
  name?: string | null
  description?: string | null
}

export type SetRolePermissionsPayload = {
  permissionCodes: string[]
}
