export type AuthResponse = {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  mustChangePassword: boolean
}

export type MeResponse = {
  email: string
  authorities: string[]
}
