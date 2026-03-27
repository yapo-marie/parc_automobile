export async function readApiMessage(res: Response): Promise<string> {
  try {
    const data: unknown = await res.json()
    if (data && typeof data === 'object') {
      const o = data as { message?: unknown; code?: unknown; details?: unknown }
      if (o.code === 'VALIDATION_ERROR' && Array.isArray(o.details) && o.details.length > 0) {
        const parts = o.details.filter((d): d is string => typeof d === 'string')
        if (parts.length) return parts.join(' — ')
      }
      if (typeof o.message === 'string' && o.message.length > 0) {
        return o.message
      }
    }
  } catch {
    /* ignore */
  }
  return `HTTP ${res.status}`
}
