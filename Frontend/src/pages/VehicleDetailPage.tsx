import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { createBreakdown, fetchBreakdowns, resolveBreakdown } from '../api/breakdowns'
import { createTechnicalVisit, fetchTechnicalVisits } from '../api/technicalVisits'
import { fetchVehicle } from '../api/vehicles'
import type { BreakdownDto, CreateBreakdownPayload } from '../types/breakdown'
import type { CreateTechnicalVisitPayload } from '../types/technicalVisit'
import type { VehicleDto, VehicleAvailability } from '../types/vehicle'

type TabId = 'info' | 'breakdowns' | 'visits'

function fmtDate(s: string | null): string {
  if (!s) return '—'
  const d = Date.parse(s)
  if (Number.isNaN(d)) return s
  return new Date(d).toLocaleDateString('fr-FR', { dateStyle: 'medium' })
}

function fmtDateTime(s: string | null): string {
  if (!s) return '—'
  const d = Date.parse(s)
  if (Number.isNaN(d)) return s
  return new Date(d).toLocaleString('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short',
  })
}

function fmtMoney(v: number | string | null): string {
  if (v == null || v === '') return '—'
  const n = typeof v === 'number' ? v : Number(v)
  if (Number.isNaN(n)) return String(v)
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(n)
}

function availabilityLabel(a: VehicleAvailability): string {
  switch (a) {
    case 'AVAILABLE':
      return 'Disponible'
    case 'ASSIGNED':
      return 'Attribué'
    case 'IN_REPAIR':
      return 'En réparation'
    case 'CONTROLE_REQUIS':
      return 'Contrôle requis'
    case 'OUT_OF_SERVICE':
      return 'Hors service'
    default:
      return a
  }
}

export function VehicleDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [tab, setTab] = useState<TabId>('info')

  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canFleet = useMemo(() => meAuths?.includes('FLEET_MANAGE') ?? false, [meAuths])
  const canDeclareBreakdown = useMemo(
    () =>
      Boolean(meAuths?.includes('FLEET_MANAGE') || meAuths?.includes('VEHICLE_READ')),
    [meAuths],
  )

  const [vehicle, setVehicle] = useState<VehicleDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [breakdowns, setBreakdowns] = useState<BreakdownDto[]>([])
  const [bdLoading, setBdLoading] = useState(false)

  const [visits, setVisits] = useState<
    Awaited<ReturnType<typeof fetchTechnicalVisits>>['content']
  >([])
  const [visLoading, setVisLoading] = useState(false)

  const [modalBreakdown, setModalBreakdown] = useState(false)
  const [modalVisit, setModalVisit] = useState(false)
  const [bdDesc, setBdDesc] = useState('')
  const [bdMileage, setBdMileage] = useState('')
  const [bdGarage, setBdGarage] = useState('')
  const [bdCost, setBdCost] = useState('')

  const [vtType, setVtType] = useState('')
  const [vtScheduled, setVtScheduled] = useState('')
  const [vtGarage, setVtGarage] = useState('')
  const [vtCost, setVtCost] = useState('')
  const [vtComments, setVtComments] = useState('')

  const [msg, setMsg] = useState<string | null>(null)

  const loadMe = useCallback(async () => {
    try {
      const res = await apiFetch('/api/me')
      if (!res.ok) throw new Error()
      const me = (await res.json()) as { authorities: string[] }
      setMeAuths(me.authorities)
    } catch {
      setMeAuths([])
    }
  }, [])

  const loadVehicle = useCallback(async () => {
    if (!id) return
    setError(null)
    setLoading(true)
    try {
      setVehicle(await fetchVehicle(id))
    } catch (e) {
      setVehicle(null)
      setError(e instanceof Error ? e.message : 'Véhicule introuvable')
    } finally {
      setLoading(false)
    }
  }, [id])

  const loadBreakdowns = useCallback(async () => {
    if (!id) return
    setBdLoading(true)
    try {
      const data = await fetchBreakdowns({ vehicleId: id, page: 0, size: 50 })
      setBreakdowns(data.content)
    } catch {
      setBreakdowns([])
    } finally {
      setBdLoading(false)
    }
  }, [id])

  const loadVisits = useCallback(async () => {
    if (!id) return
    setVisLoading(true)
    try {
      const data = await fetchTechnicalVisits({ vehicleId: id, page: 0, size: 50 })
      setVisits(data.content)
    } catch {
      setVisits([])
    } finally {
      setVisLoading(false)
    }
  }, [id])

  useEffect(() => {
    void loadMe()
  }, [loadMe])

  useEffect(() => {
    void loadVehicle()
  }, [loadVehicle])

  useEffect(() => {
    if (tab === 'breakdowns') void loadBreakdowns()
    if (tab === 'visits') void loadVisits()
  }, [tab, loadBreakdowns, loadVisits])

  async function onSubmitBreakdown(e: FormEvent) {
    e.preventDefault()
    setMsg(null)
    if (!id || !bdDesc.trim()) {
      setMsg('La description est requise.')
      return
    }
    const payload: CreateBreakdownPayload = {
      vehicleId: id,
      description: bdDesc.trim(),
      mileageAtBreakdown: bdMileage === '' ? null : Number(bdMileage),
      garage: bdGarage.trim() || null,
      repairCost: bdCost === '' ? null : Number(bdCost),
    }
    try {
      await createBreakdown(payload)
      setModalBreakdown(false)
      setBdDesc('')
      setBdMileage('')
      setBdGarage('')
      setBdCost('')
      await loadVehicle()
      await loadBreakdowns()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onResolveBreakdown(row: BreakdownDto) {
    if (!window.confirm('Marquer cette panne comme résolue ?')) return
    setMsg(null)
    try {
      await resolveBreakdown(row.id, { resolvedAt: new Date().toISOString() })
      await loadVehicle()
      await loadBreakdowns()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Erreur')
    }
  }

  async function onSubmitVisit(e: FormEvent) {
    e.preventDefault()
    setMsg(null)
    if (!id || !vtType.trim() || !vtScheduled) {
      setMsg('Type et date prévue sont requis.')
      return
    }
    const payload: CreateTechnicalVisitPayload = {
      vehicleId: id,
      type: vtType.trim(),
      scheduledDate: vtScheduled,
      garage: vtGarage.trim() || null,
      cost: vtCost === '' ? null : Number(vtCost),
      comments: vtComments.trim() || null,
    }
    try {
      await createTechnicalVisit(payload)
      setModalVisit(false)
      setVtType('')
      setVtScheduled('')
      setVtGarage('')
      setVtCost('')
      setVtComments('')
      await loadVisits()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  const v = vehicle

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard/vehicles">← Véhicules</Link>
      </p>
      <h1>Fiche véhicule</h1>

      {msg ? <p className="alert alert--warn">{msg}</p> : null}
      {loading ? <p className="muted">Chargement…</p> : null}
      {error ? <p className="alert alert--error">{error}</p> : null}

      {!loading && v ? (
        <>
          <nav className="vehicle-detail__tabs" aria-label="Sections fiche véhicule">
            <button
              type="button"
              className={'vehicle-detail__tab' + (tab === 'info' ? ' vehicle-detail__tab--active' : '')}
              onClick={() => setTab('info')}
            >
              Informations
            </button>
            <button
              type="button"
              className={
                'vehicle-detail__tab' + (tab === 'breakdowns' ? ' vehicle-detail__tab--active' : '')
              }
              onClick={() => setTab('breakdowns')}
            >
              Pannes
            </button>
            <button
              type="button"
              className={'vehicle-detail__tab' + (tab === 'visits' ? ' vehicle-detail__tab--active' : '')}
              onClick={() => setTab('visits')}
            >
              Visites techniques
            </button>
          </nav>

          {tab === 'info' ? (
            <div className="vehicle-detail">
              {v.photoUrl ? (
                <div className="vehicle-detail__photo card">
                  <img src={v.photoUrl} alt="" className="vehicle-detail__img" />
                </div>
              ) : null}
              <section className="card card--flat">
                <h2 className="users-h2">
                  <code>{v.plateNumber}</code> — {v.brand} {v.model}
                </h2>
                <dl className="vehicle-detail__dl">
                  <dt>Année</dt>
                  <dd>{v.year ?? '—'}</dd>
                  <dt>Couleur</dt>
                  <dd>{v.color ?? '—'}</dd>
                  <dt>Catégorie</dt>
                  <dd>{v.category ?? '—'}</dd>
                  <dt>Carburant</dt>
                  <dd>{v.fuelType ?? '—'}</dd>
                  <dt>Kilométrage</dt>
                  <dd>{v.mileage.toLocaleString('fr-FR')} km</dd>
                  <dt>Puissance</dt>
                  <dd>{v.power != null ? `${v.power} ch` : '—'}</dd>
                  <dt>Places</dt>
                  <dd>{v.seats ?? '—'}</dd>
                  <dt>Date d&apos;acquisition</dt>
                  <dd>{fmtDate(v.acquisitionDate)}</dd>
                  <dt>Valeur d&apos;acquisition</dt>
                  <dd>{fmtMoney(v.acquisitionValue)}</dd>
                  <dt>Fin d&apos;assurance</dt>
                  <dd>{fmtDate(v.insuranceExpiry)}</dd>
                  <dt>Disponibilité</dt>
                  <dd>{availabilityLabel(v.availability)}</dd>
                  <dt>Statut fiche</dt>
                  <dd>{v.status}</dd>
                  <dt>Créé le</dt>
                  <dd>{fmtDateTime(v.createdAt)}</dd>
                  <dt>Mis à jour</dt>
                  <dd>{fmtDateTime(v.updatedAt)}</dd>
                </dl>
                {v.notes ? (
                  <>
                    <h3 className="users-h2">Notes</h3>
                    <p className="vehicle-detail__notes">{v.notes}</p>
                  </>
                ) : null}
              </section>
            </div>
          ) : null}

          {tab === 'breakdowns' ? (
            <section className="card card--flat">
              <div className="vehicle-detail__tab-actions">
                <h2 className="users-h2">Pannes</h2>
                {canDeclareBreakdown ? (
                  <button type="button" className="btn-inline" onClick={() => setModalBreakdown(true)}>
                    + Déclarer une panne
                  </button>
                ) : null}
              </div>
              {bdLoading ? <p className="muted">Chargement…</p> : null}
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Statut</th>
                      <th>Description</th>
                      <th>Déclarée</th>
                      <th>Garage</th>
                      <th>Coût</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {breakdowns.map((r) => (
                      <tr key={r.id}>
                        <td>{r.status === 'DECLAREE' ? 'Déclarée' : 'Résolue'}</td>
                        <td>
                          {r.description}
                          {r.mileageAtBreakdown != null ? (
                            <div className="muted tiny">Km : {r.mileageAtBreakdown}</div>
                          ) : null}
                        </td>
                        <td className="tiny">{fmtDateTime(r.declaredAt)}</td>
                        <td>{r.garage ?? '—'}</td>
                        <td>{fmtMoney(r.repairCost)}</td>
                        <td className="cell-actions">
                          {canFleet && r.status === 'DECLAREE' ? (
                            <button
                              type="button"
                              className="linkish"
                              onClick={() => void onResolveBreakdown(r)}
                            >
                              Résoudre
                            </button>
                          ) : null}
                        </td>
                      </tr>
                    ))}
                    {!breakdowns.length && !bdLoading ? (
                      <tr>
                        <td colSpan={6} className="muted">
                          Aucune panne enregistrée pour ce véhicule.
                        </td>
                      </tr>
                    ) : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {tab === 'visits' ? (
            <section className="card card--flat">
              <div className="vehicle-detail__tab-actions">
                <h2 className="users-h2">Visites techniques</h2>
                {canFleet ? (
                  <button type="button" className="btn-inline" onClick={() => setModalVisit(true)}>
                    + Planifier une visite
                  </button>
                ) : null}
              </div>
              {visLoading ? <p className="muted">Chargement…</p> : null}
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Prévue</th>
                      <th>Réalisée</th>
                      <th>Résultat</th>
                      <th>Garage</th>
                      <th>Coût</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visits.map((t) => (
                      <tr key={t.id}>
                        <td>{t.type}</td>
                        <td>{fmtDate(t.scheduledDate)}</td>
                        <td>{t.completedDate ? fmtDate(t.completedDate) : '—'}</td>
                        <td>{t.result ?? '—'}</td>
                        <td>{t.garage ?? '—'}</td>
                        <td>{fmtMoney(t.cost)}</td>
                      </tr>
                    ))}
                    {!visits.length && !visLoading ? (
                      <tr>
                        <td colSpan={6} className="muted">
                          Aucune visite pour ce véhicule.
                        </td>
                      </tr>
                    ) : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {modalBreakdown ? (
            <div className="reject-modal" role="presentation" onClick={() => setModalBreakdown(false)}>
              <div
                className="reject-modal__panel fleet-modal"
                role="dialog"
                aria-modal="true"
                onClick={(e) => e.stopPropagation()}
              >
                <h2 className="users-h2">Déclarer une panne</h2>
                <p className="tiny muted">
                  Le véhicule passera en « en réparation » côté disponibilité (sauf hors service).
                </p>
                <form onSubmit={(e) => void onSubmitBreakdown(e)}>
                  <label className="field">
                    <span>Description *</span>
                    <textarea
                      rows={4}
                      required
                      value={bdDesc}
                      onChange={(e) => setBdDesc(e.target.value)}
                      maxLength={5000}
                    />
                  </label>
                  <label className="field">
                    <span>Km au moment de la panne</span>
                    <input
                      type="number"
                      min={0}
                      value={bdMileage}
                      onChange={(e) => setBdMileage(e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Garage</span>
                    <input
                      value={bdGarage}
                      onChange={(e) => setBdGarage(e.target.value)}
                      maxLength={200}
                    />
                  </label>
                  <label className="field">
                    <span>Coût réparation (estim.)</span>
                    <input
                      type="number"
                      step="0.01"
                      min={0}
                      value={bdCost}
                      onChange={(e) => setBdCost(e.target.value)}
                    />
                  </label>
                  <button type="submit">Enregistrer</button>
                  <button type="button" className="btn-secondary" onClick={() => setModalBreakdown(false)}>
                    Annuler
                  </button>
                </form>
              </div>
            </div>
          ) : null}

          {modalVisit ? (
            <div className="reject-modal" role="presentation" onClick={() => setModalVisit(false)}>
              <div
                className="reject-modal__panel fleet-modal"
                role="dialog"
                aria-modal="true"
                onClick={(e) => e.stopPropagation()}
              >
                <h2 className="users-h2">Planifier une visite technique</h2>
                <form onSubmit={(e) => void onSubmitVisit(e)}>
                  <label className="field">
                    <span>Type *</span>
                    <input value={vtType} onChange={(e) => setVtType(e.target.value)} required />
                  </label>
                  <label className="field">
                    <span>Date prévue *</span>
                    <input
                      type="date"
                      value={vtScheduled}
                      onChange={(e) => setVtScheduled(e.target.value)}
                      required
                    />
                  </label>
                  <label className="field">
                    <span>Garage</span>
                    <input value={vtGarage} onChange={(e) => setVtGarage(e.target.value)} />
                  </label>
                  <label className="field">
                    <span>Coût estimé</span>
                    <input
                      type="number"
                      step="0.01"
                      min={0}
                      value={vtCost}
                      onChange={(e) => setVtCost(e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Commentaires</span>
                    <textarea rows={2} value={vtComments} onChange={(e) => setVtComments(e.target.value)} />
                  </label>
                  <button type="submit">Créer</button>
                  <button type="button" className="btn-secondary" onClick={() => setModalVisit(false)}>
                    Annuler
                  </button>
                </form>
              </div>
            </div>
          ) : null}
        </>
      ) : null}
    </div>
  )
}
