export type AlertType =
  | 'OVERSPEED'
  | 'GEOFENCE_EXIT'
  | 'GEOFENCE_ENTER'
  | 'LOW_FUEL'
  | 'COLLISION'
  | 'ENGINE_OFF_ZONE'
  | 'POWER_CUT'
  | 'SOS'

export type GpsPosition = {
  vehicleId: string
  vehiclePlate: string
  imei: string
  latitude: number
  longitude: number
  speed: number
  heading: number
  ignitionOn: boolean
  recordedAt: string
}

export type GpsAlert = {
  id: string
  vehicleId: string
  type: AlertType
  message: string
  latitude?: number | null
  longitude?: number | null
  speed?: number | null
  acknowledged: boolean
  createdAt: string
}

