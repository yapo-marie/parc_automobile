import type { VehicleAvailability } from './vehicle'

export type FleetVehicleDto = {
  id: string
  vehicleId: string
  plateNumber: string
  brand: string
  model: string
  vehicleAvailability: VehicleAvailability
  administration: string
  dailyCost: number | string | null
  costPerKm: number | string | null
  annualBudget: number | string | null
  startDate: string | null
  endDate: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export type FleetVehiclePageResponse = {
  content: FleetVehicleDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type FleetStatsDto = {
  totalFleetVehicles: number
  totalAnnualBudget: number | string
  globalBudgetUsedPercent: number | string
  fleetUtilizationPercent: number | string
}

export type CreateFleetVehiclePayload = {
  vehicleId: string
  administration: string
  dailyCost?: number | null
  costPerKm?: number | null
  annualBudget?: number | null
  startDate?: string | null
  endDate?: string | null
  notes?: string | null
}

export type UpdateFleetVehiclePayload = {
  administration: string
  dailyCost?: number | null
  costPerKm?: number | null
  annualBudget?: number | null
  startDate?: string | null
  endDate?: string | null
  notes?: string | null
}
