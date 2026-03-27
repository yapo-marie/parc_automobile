import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet'
import type { GpsPosition } from '../types'
import { useGpsWebSocket } from '../hooks/useGpsWebSocket'

// Fix des icônes Leaflet (souvent nécessaire avec Vite).
// On évite l’envoi de PNG distants.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).toString(),
  iconUrl: new URL('leaflet/dist/images/marker-icon.png', import.meta.url).toString(),
  shadowUrl: new URL('leaflet/dist/images/marker-shadow.png', import.meta.url).toString(),
})

function vehicleSummary(p: GpsPosition) {
  return (
    <div style={{ minWidth: 220 }}>
      <strong>{p.vehiclePlate}</strong>
      <div style={{ marginTop: 6, fontSize: 13 }}>
        Vitesse : {Math.round(p.speed)} km/h
        <br />
        Contact : {p.ignitionOn ? 'ON' : 'OFF'}
      </div>
    </div>
  )
}

export function FleetMap() {
  const { positions } = useGpsWebSocket()
  const posArr = Array.from(positions.values())

  return (
    <MapContainer
      center={[5.35, -4.0]}
      zoom={12}
      style={{ height: '70vh', width: '100%' }}
    >
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

      {posArr.map((p) => (
        <Marker key={p.vehicleId} position={[p.latitude, p.longitude]}>
          <Popup>{vehicleSummary(p)}</Popup>
        </Marker>
      ))}
    </MapContainer>
  )
}

