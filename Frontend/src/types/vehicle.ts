export type VehicleCategory = 'BERLINE' | 'SUV' | 'UTILITAIRE' | 'CAMION' | 'MOTO'

export type FuelType = 'ESSENCE' | 'DIESEL' | 'HYBRIDE' | 'ELECTRIQUE' | 'GPL'

export type VehicleAvailability =
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'IN_REPAIR'
  | 'CONTROLE_REQUIS'
  | 'OUT_OF_SERVICE'

export type VehicleRecordStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'

export type VehicleDto = {
  id: string
  plateNumber: string
  brand: string
  model: string
  year: number | null
  color: string | null
  category: VehicleCategory | null
  fuelType: FuelType | null
  mileage: number
  power: number | null
  seats: number | null
  acquisitionDate: string | null
  acquisitionValue: number | string | null
  insuranceExpiry: string | null
  availability: VehicleAvailability
  status: VehicleRecordStatus
  photoUrl: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export type VehiclePageResponse = {
  content: VehicleDto[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type VehicleWritePayload = {
  plateNumber: string
  brand: string
  model: string
  year: number | null
  color: string | null
  category: VehicleCategory | null
  fuelType: FuelType | null
  mileage: number | null
  power: number | null
  seats: number | null
  acquisitionDate: string | null
  acquisitionValue: number | null
  insuranceExpiry: string | null
  availability: VehicleAvailability | null
  status: VehicleRecordStatus | null
  photoUrl: string | null
  notes: string | null
}
