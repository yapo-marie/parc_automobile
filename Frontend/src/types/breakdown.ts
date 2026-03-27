export type BreakdownStatus = 'DECLAREE' | 'RESOLUE'

export type BreakdownDto = {
  id: string
  vehicleId: string
  vehiclePlate: string
  vehicleLabel: string
  declaredById: string
  declaredByEmail: string
  description: string
  declaredAt: string
  mileageAtBreakdown: number | null
  garage: string | null
  repairCost: number | string | null
  resolvedAt: string | null
  status: BreakdownStatus
}

export type BreakdownPageResponse = {
  content: BreakdownDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateBreakdownPayload = {
  vehicleId: string
  description: string
  mileageAtBreakdown?: number | null
  garage?: string | null
  repairCost?: number | string | null
}

export type ResolveBreakdownPayload = {
  resolvedAt?: string | null
}

