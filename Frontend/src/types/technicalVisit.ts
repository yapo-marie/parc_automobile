export type TechnicalVisitResponse = {
  id: string
  vehicleId: string
  vehiclePlate: string
  vehicleLabel: string
  type: string
  scheduledDate: string
  completedDate: string | null
  result: string
  garage: string | null
  cost: number | string | null
  nextDueDate: string | null
  comments: string | null
  createdAt: string
}

export type TechnicalVisitPageResponse = {
  content: TechnicalVisitResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateTechnicalVisitPayload = {
  vehicleId: string
  type: string
  scheduledDate: string
  completedDate?: string | null
  result?: string | null
  garage?: string | null
  cost?: number | string | null
  nextDueDate?: string | null
  comments?: string | null
}

export type UpdateTechnicalVisitPayload = {
  type: string
  scheduledDate?: string | null
  completedDate?: string | null
  result?: string | null
  garage?: string | null
  cost?: number | string | null
  nextDueDate?: string | null
  comments?: string | null
}

