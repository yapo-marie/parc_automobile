export type ReservationStatus =
  | 'EN_ATTENTE'
  | 'CONFIRMEE'
  | 'TERMINEE'
  | 'REFUSEE'
  | 'ANNULEE'

export type ReservationDto = {
  id: string
  vehicleId: string
  vehiclePlate: string
  vehicleLabel: string
  requesterId: string
  requesterEmail: string
  requesterFirstname: string
  requesterLastname: string
  startDatetime: string
  endDatetime: string
  reason: string | null
  destination: string | null
  estimatedKm: number | null
  passengerCount: number | null
  status: ReservationStatus
  confirmedById: string | null
  confirmedByEmail: string | null
  confirmedAt: string | null
  rejectionReason: string | null
  createdAt: string
}

export type ReservationPageResponse = {
  content: ReservationDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateReservationPayload = {
  vehicleId: string
  startDatetime: string
  endDatetime: string
  reason?: string | null
  destination?: string | null
  estimatedKm?: number | null
  passengerCount?: number | null
}
