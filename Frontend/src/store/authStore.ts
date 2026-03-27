import type { AuthResponse } from '../types/auth'
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  mustChangePassword: boolean
  setFromAuthResponse: (data: AuthResponse) => void
  setMustChangePassword: (value: boolean) => void
  clear: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      mustChangePassword: false,
      setFromAuthResponse: (data) =>
        set({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          mustChangePassword: data.mustChangePassword,
        }),
      setMustChangePassword: (value) => set({ mustChangePassword: value }),
      clear: () =>
        set({
          accessToken: null,
          refreshToken: null,
          mustChangePassword: false,
        }),
    }),
    {
      name: 'fleetpro-auth',
      partialize: (s) => ({
        accessToken: s.accessToken,
        refreshToken: s.refreshToken,
        mustChangePassword: s.mustChangePassword,
      }),
    },
  ),
)
