import { useEffect, useState } from 'react'
import { useAuthStore } from '../store/authStore'

/** Attend la réhydratation du store persisté (évite une redirection /login fantôme au F5). */
export function useHydratedAuth(): boolean {
  const [hydrated, setHydrated] = useState(() => useAuthStore.persist.hasHydrated())

  useEffect(() => {
    setHydrated(useAuthStore.persist.hasHydrated())
    return useAuthStore.persist.onFinishHydration(() => {
      setHydrated(true)
    })
  }, [])

  return hydrated
}
