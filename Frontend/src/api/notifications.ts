import { apiFetch } from './client'
import { readApiMessage } from './errors'

export type NotificationDto = {
  id: string
  title: string
  message: string
  type: 'ALERT_GPS' | 'RESERVATION' | 'MAINTENANCE' | 'SYSTEM'
  link: string | null
  read: boolean
  createdAt: string
}

export type NotificationPage = {
  content: NotificationDto[]
  totalPages: number
  totalElements: number
  page: number
  size: number
}

export async function fetchNotifications(read?: boolean): Promise<NotificationPage> {
  const q = read == null ? '' : `?read=${read}`
  const res = await apiFetch(`/api/notifications${q}`)
  if (!res.ok) throw new Error(await readApiMessage(res))
  return (await res.json()) as NotificationPage
}

export async function fetchUnreadCount(): Promise<number> {
  const res = await apiFetch('/api/notifications/count')
  if (!res.ok) throw new Error(await readApiMessage(res))
  const body = (await res.json()) as { unread: number }
  return body.unread
}

export async function markNotificationRead(id: string): Promise<void> {
  const res = await apiFetch(`/api/notifications/${encodeURIComponent(id)}/read`, { method: 'PATCH' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function markAllNotificationsRead(): Promise<void> {
  const res = await apiFetch('/api/notifications/read-all', { method: 'PATCH' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

export async function deleteNotification(id: string): Promise<void> {
  const res = await apiFetch(`/api/notifications/${encodeURIComponent(id)}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(await readApiMessage(res))
}

