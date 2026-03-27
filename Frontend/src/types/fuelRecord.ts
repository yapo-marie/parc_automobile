export type FuelRecordDto = {
  id: string
  vehicleId: string
  vehiclePlate: string
  vehicleLabel: string
  filledById: string
  filledByEmail: string
  fillDate: string
  liters: number | string
  unitPrice: number | string | null
  totalCost: number | string | null
  mileage: number | null
  station: string | null
  createdAt: string
}

export type FuelRecordPageResponse = {
  content: FuelRecordDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type CreateFuelRecordPayload = {
  vehicleId: string
  fillDate: string
  liters: number
  unitPrice?: number | null
  totalCost?: number | null
  mileage?: number | null
  station?: string | null
}

export type FuelMonthlySeriesPoint = {
  yearMonth: string
  avgLitersPer100km: number | string | null
  totalLiters: number | string
}

export type FuelRecordStatsDto = {
  averageLitersPer100km: number | string | null
  currentMonthLiters: number | string
  currentMonthCost: number | string
  currentMonthFillCount: number
  totalFillCount: number
  lastSixMonths: FuelMonthlySeriesPoint[]
}

