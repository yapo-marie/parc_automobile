export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED' | 'LOCKED'

export type UserDto = {
  id: string
  firstname: string
  lastname: string
  email: string
  phone: string | null
  position: string | null
  status: UserStatus
  mustChangePassword: boolean
  createdAt: string
  lastLogin: string | null
  roleNames: string[]
}

export type UserPageResponse = {
  content: UserDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateUserPayload = {
  firstname: string
  lastname: string
  email: string
  phone?: string | null
  position?: string | null
  password: string
  roleNames: string[]
  mustChangePassword?: boolean | null
}

export type UpdateSelfProfilePayload = {
  firstname: string
  lastname: string
  phone?: string | null
  position?: string | null
}
