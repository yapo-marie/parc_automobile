import { useState } from 'react'
import { Plus, Phone, IdCard, CarFront } from 'lucide-react'

// Mock des conducteurs basés sur la capture d'écran
const MOCK_DRIVERS = [
  { id: 1, initials: 'MD', name: 'Marc Dupont', phone: '+33 6 12 34 56 78', license: 'Permis B', plate: 'AB-123-CD' },
  { id: 2, initials: 'JM', name: 'Julie Martin', phone: '+33 6 98 76 54 32', license: 'Permis B', plate: 'EF-456-GH' },
  { id: 3, initials: 'PL', name: 'Pierre Leclerc', phone: '+33 6 11 22 33 44', license: 'Permis C', plate: 'MN-012-OP' },
  { id: 4, initials: 'SB', name: 'Sophie Bernard', phone: '+33 6 55 66 77 88', license: 'Permis B', plate: 'UV-678-WX' },
]

export function DriversPage() {
  const [drivers] = useState(MOCK_DRIVERS)

  return (
    <div className="dash-page users-page">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div className="page-title-group">
            <h1>Chauffeurs</h1>
          </div>
          <p className="dash-lead">
            {drivers.length} conducteurs enregistrés
          </p>
        </div>
        <button type="button" className="btn-primary">
          <Plus size={16} />
          Ajouter
        </button>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
        gap: '1.25rem',
        marginTop: '1.5rem'
      }}>
        {drivers.map(driver => (
          <article key={driver.id} className="card card--flat" style={{ padding: '1.25rem', marginTop: 0, display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{
                width: '48px', height: '48px', borderRadius: '50%',
                background: '#ecfdf5', color: '#10b981',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontWeight: 600, fontSize: '1.1rem'
              }}>
                {driver.initials}
              </div>
              <div>
                <h2 style={{ margin: '0 0 0.25rem', fontSize: '1.05rem', color: '#0f172a' }}>{driver.name}</h2>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: '#64748b', fontSize: '0.85rem' }}>
                  <Phone size={14} />
                  {driver.phone}
                </div>
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '0.75rem', marginTop: 'auto' }}>
              <span className="badge badge--neutral" style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', padding: '0.35rem 0.65rem' }}>
                <IdCard size={14} />
                {driver.license}
              </span>
              <span className="badge badge--neutral" style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', padding: '0.35rem 0.65rem' }}>
                <CarFront size={14} />
                {driver.plate}
              </span>
            </div>
          </article>
        ))}
      </div>
    </div>
  )
}
