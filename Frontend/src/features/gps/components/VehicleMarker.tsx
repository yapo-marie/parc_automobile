import { Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import type { GpsPosition } from '../types'
import { EngineControlButton } from './EngineControlButton'

function markerColor(pos: GpsPosition) {
  if (pos.speed > 0) return 'green'
  if (pos.ignitionOn) return 'blue'
  return 'gray'
}

function createDivIcon(color: string, heading: number) {
  // Marker “simple” (divIcon) pour éviter les problèmes d’assets d’icônes Leaflet.
  return L.divIcon({
    className: '',
    html: `
      <div style="
        width: 14px;
        height: 14px;
        border-radius: 50%;
        background: ${color};
        border: 2px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.25);
        transform: rotate(${heading}deg);
      "></div>
    `,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
  })
}

export function VehicleMarker({ position }: { position: GpsPosition }) {
  const color = markerColor(position)
  const icon = createDivIcon(color, position.heading ?? 0)

  return (
    <Marker position={[position.latitude, position.longitude]} icon={icon}>
      <Popup>
        <div style={{ minWidth: 220 }}>
          <strong>{position.vehiclePlate}</strong>
          <div style={{ marginTop: 6, fontSize: 13 }}>
            Vitesse : {Math.round(position.speed)} km/h
            <br />
            Contact : {position.ignitionOn ? 'ON' : 'OFF'}
            <br />
            Vu :{' '}
            {new Date(position.recordedAt).toLocaleString('fr-FR', {
              dateStyle: 'short',
              timeStyle: 'short',
            })}
          </div>
          <EngineControlButton vehicleId={position.vehicleId} />
        </div>
      </Popup>
    </Marker>
  )
}

