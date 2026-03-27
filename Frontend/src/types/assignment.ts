export type AssignmentStatus = 'ACTIVE' | 'ENDED' | 'WITHDRAWN'

export type AssignmentDto = {
  id: string
  vehicleId: string
  vehiclePlate: string
  vehicleLabel: string
  driverId: string
  driverEmail: string
  driverFirstname: string
  driverLastname: string
  startDatetime: string
  endDatetime: string
  assignmentType: string
  mileageStart: number | null
  mileageEnd: number | null
  reason: string | null
  status: AssignmentStatus
  withdrawnAt: string | null
}

export type AssignmentPageResponse = {
  content: AssignmentDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateAssignmentPayload = {
  vehicleId: string
  driverId: string
  assignmentType: string
  startDatetime: string
  endDatetime: string
  mileageStart?: number | null
  mileageEnd?: number | null
  reason?: string | null
}

export type CreatePoolAssignmentPayload = {
  fleetVehicleId: string
  driverId: string
  assignmentType: string
  startDatetime: string
  endDatetime: string
  mileageStart?: number | null
  mileageEnd?: number | null
  reason?: string | null
}

export type EndAssignmentPayload = {
  endDatetime: string
  reason?: string | null
}

export type WithdrawAssignmentPayload = {
  reason?: string | null
}

