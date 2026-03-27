import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import Calendar from 'react-calendar'
import 'react-calendar/dist/Calendar.css'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import {
  cancelReservation,
  confirmReservation,
  createReservation,
  fetchAllReservations,
  fetchMyReservations,
  rejectReservation,
} from '../api/reservations'
import { fetchVehicles } from '../api/vehicles'
import type { ReservationDto, ReservationStatus } from '../types/reservation'
import type { VehicleDto } from '../types/vehicle'

const STATUSES: ReservationStatus[] = ['EN_ATTENTE', 'CONFIRMEE', 'TERMINEE', 'REFUSEE', 'ANNULEE']

function statusLabel(s: ReservationStatus): string {
  switch (s) {
    case 'EN_ATTENTE':
      return 'En attente'
    case 'CONFIRMEE':
      return 'Confirmée'
    case 'TERMINEE':
      return 'Terminée'
    case 'REFUSEE':
      return 'Refusée'
    case 'ANNULEE':
      return 'Annulée'
    default:
      return s
  }
}

function calPriorityClass(statuses: ReservationStatus[]): string {
  if (statuses.includes('EN_ATTENTE')) return 'pending'
  if (statuses.includes('CONFIRMEE')) return 'confirmed'
  if (statuses.includes('TERMINEE')) return 'done'
  if (statuses.includes('REFUSEE')) return 'rejected'
  return 'cancelled'
}

function dayReservations(day: Date, rows: ReservationDto[]): ReservationDto[] {
  const start = new Date(day)
  start.setHours(0, 0, 0, 0)
  const end = new Date(day)
  end.setHours(23, 59, 59, 999)
  const t0 = start.getTime()
  const t1 = end.getTime()
  return rows.filter((r) => {
    const rs = new Date(r.startDatetime).getTime()
    const re = new Date(r.endDatetime).getTime()
    return rs <= t1 && re >= t0
  })
}

function toIsoUtc(localDatetime: string): string {
  return new Date(localDatetime).toISOString()
}

function fmtDt(iso: string): string {
  const d = Date.parse(iso)
  if (Number.isNaN(d)) return iso
  return new Date(d).toLocaleString('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short',
  })
}

export function ReservationsPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [mine, setMine] = useState<ReservationDto[]>([])
  const [minePage, setMinePage] = useState(0)
  const [mineTotalPages, setMineTotalPages] = useState(0)

  const [allRows, setAllRows] = useState<ReservationDto[]>([])
  const [allPage, setAllPage] = useState(0)
  const [allTotalPages, setAllTotalPages] = useState(0)
  const [filterStatus, setFilterStatus] = useState('')

  const [vehicleId, setVehicleId] = useState('')
  const [startLocal, setStartLocal] = useState('')
  const [endLocal, setEndLocal] = useState('')
  const [reason, setReason] = useState('')
  const [destination, setDestination] = useState('')
  const [estimatedKm, setEstimatedKm] = useState('')
  const [passengerCount, setPassengerCount] = useState('')

  const [rejectId, setRejectId] = useState<string | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const [viewMode, setViewMode] = useState<'list' | 'calendar'>('list')
  const [calRows, setCalRows] = useState<ReservationDto[]>([])
  const [calLoading, setCalLoading] = useState(false)
  const [calDate, setCalDate] = useState<Date>(() => new Date())

  const [cancelTarget, setCancelTarget] = useState<ReservationDto | null>(null)
  const [cancelReasonDraft, setCancelReasonDraft] = useState('')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [formMsg, setFormMsg] = useState<string | null>(null)

  const canManage = meAuths?.includes('RESERVATION_MANAGE') ?? false

  const loadVehicles = useCallback(async () => {
    try {
      const data = await fetchVehicles({ page: 0, size: 200 })
      setVehicles(data.content)
    } catch {
      setVehicles([])
    }
  }, [])

  const loadMine = useCallback(async () => {
    const data = await fetchMyReservations({ page: minePage, size: 15 })
    setMine(data.content)
    setMineTotalPages(data.totalPages)
  }, [minePage])

  const loadAll = useCallback(async () => {
    if (!canManage) return
    const data = await fetchAllReservations({
      page: allPage,
      size: 15,
      status: filterStatus || undefined,
    })
    setAllRows(data.content)
    setAllTotalPages(data.totalPages)
  }, [allPage, canManage, filterStatus])

  const loadCalendar = useCallback(async () => {
    setCalLoading(true)
    try {
      if (canManage) {
        const data = await fetchAllReservations({ page: 0, size: 500 })
        setCalRows(data.content)
      } else {
        const data = await fetchMyReservations({ page: 0, size: 500 })
        setCalRows(data.content)
      }
    } catch {
      setCalRows([])
    } finally {
      setCalLoading(false)
    }
  }, [canManage])

  useEffect(() => {
    void (async () => {
      try {
        const res = await apiFetch('/api/me')
        if (res.ok) {
          const me = (await res.json()) as { authorities: string[] }
          setMeAuths(me.authorities)
        }
      } catch {
        setMeAuths([])
      }
    })()
  }, [])

  useEffect(() => {
    void loadVehicles()
  }, [loadVehicles])

  useEffect(() => {
    setLoading(true)
    setError(null)
    void (async () => {
      try {
        await loadMine()
        if (canManage) await loadAll()
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erreur')
      } finally {
        setLoading(false)
      }
    })()
  }, [canManage, loadMine, loadAll])

  useEffect(() => {
    if (viewMode === 'calendar') void loadCalendar()
  }, [viewMode, loadCalendar])

  const dayDetail = useMemo(() => dayReservations(calDate, calRows), [calDate, calRows])

  const tileClassName = useCallback(
    ({ date, view }: { date: Date; view: string }) => {
      if (view !== 'month') return ''
      const items = dayReservations(date, calRows)
      if (!items.length) return ''
      const cls = calPriorityClass(items.map((i) => i.status))
      return `res-cal__tile res-cal__tile--${cls}`
    },
    [calRows],
  )

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setFormMsg(null)
    if (!vehicleId || !startLocal || !endLocal) {
      setFormMsg('Véhicule et plages horaire requis.')
      return
    }
    try {
      await createReservation({
        vehicleId,
        startDatetime: toIsoUtc(startLocal),
        endDatetime: toIsoUtc(endLocal),
        reason: reason.trim() || null,
        destination: destination.trim() || null,
        estimatedKm: estimatedKm === '' ? null : Number(estimatedKm),
        passengerCount: passengerCount === '' ? null : Number(passengerCount),
      })
      setFormMsg('Demande enregistrée.')
      setReason('')
      setDestination('')
      setEstimatedKm('')
      setPassengerCount('')
      await loadMine()
      if (canManage) await loadAll()
      if (viewMode === 'calendar') await loadCalendar()
    } catch (err) {
      setFormMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function handleConfirmReservation(id: string) {
    if (!window.confirm('Confirmer cette réservation ?')) return
    try {
      await confirmReservation(id)
      await loadMine()
      if (canManage) await loadAll()
      if (viewMode === 'calendar') await loadCalendar()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onRejectSubmit(e: FormEvent) {
    e.preventDefault()
    if (!rejectId) return
    try {
      await rejectReservation(rejectId, rejectReason.trim())
      setRejectId(null)
      setRejectReason('')
      await loadMine()
      if (canManage) await loadAll()
      if (viewMode === 'calendar') await loadCalendar()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onCancelConfirm(e: FormEvent) {
    e.preventDefault()
    if (!cancelTarget) return
    try {
      await cancelReservation(
        cancelTarget.id,
        cancelReasonDraft.trim() ? cancelReasonDraft.trim() : null,
      )
      setCancelTarget(null)
      setCancelReasonDraft('')
      await loadMine()
      if (canManage) await loadAll()
      if (viewMode === 'calendar') await loadCalendar()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  function rowActions(r: ReservationDto) {
    const pending = r.status === 'EN_ATTENTE'
    const cancellable = r.status === 'EN_ATTENTE' || r.status === 'CONFIRMEE'
    return (
      <div className="cell-actions--stack">
        {canManage && pending ? (
          <>
            <button type="button" className="linkish" onClick={() => void handleConfirmReservation(r.id)}>
              Confirmer
            </button>
            <button type="button" className="linkish" onClick={() => setRejectId(r.id)}>
              Refuser
            </button>
          </>
        ) : null}
        {cancellable ? (
          <button type="button" className="linkish danger" onClick={() => setCancelTarget(r)}>
            Annuler
          </button>
        ) : null}
      </div>
    )
  }

  function renderTable(rows: ReservationDto[], empty: string) {
    if (!rows.length) return <p className="muted">{empty}</p>
    return (
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Véhicule</th>
              <th>Début</th>
              <th>Fin</th>
              <th>Statut</th>
              <th>Demandeur</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>
                  <Link to={`/dashboard/vehicles/${r.vehicleId}`}>
                    <code>{r.vehiclePlate}</code>
                  </Link>
                  <div className="muted tiny">{r.vehicleLabel}</div>
                </td>
                <td>{fmtDt(r.startDatetime)}</td>
                <td>{fmtDt(r.endDatetime)}</td>
                <td>{statusLabel(r.status)}</td>
                <td>
                  {r.requesterFirstname} {r.requesterLastname}
                  <div className="muted tiny">{r.requesterEmail}</div>
                </td>
                <td>{rowActions(r)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Réservations</h1>
      <p className="dash-lead">
        Demandes de véhicule : création, suivi, confirmation ; vue liste ou calendrier.
      </p>

      <div className="res-view-toggle">
        <button
          type="button"
          className={viewMode === 'list' ? 'res-view-toggle__btn res-view-toggle__btn--on' : 'res-view-toggle__btn'}
          onClick={() => setViewMode('list')}
        >
          Liste
        </button>
        <button
          type="button"
          className={
            viewMode === 'calendar' ? 'res-view-toggle__btn res-view-toggle__btn--on' : 'res-view-toggle__btn'
          }
          onClick={() => setViewMode('calendar')}
        >
          Calendrier
        </button>
      </div>

      {error ? <p className="alert alert--error">{error}</p> : null}

      {viewMode === 'calendar' ? (
        <section className="card card--flat">
          <h2 className="users-h2">Calendrier</h2>
          <p className="tiny muted">
            Couleurs : en attente (ambre), confirmée (vert), terminée (gris), refusée (rouge), annulée (ardoise).
          </p>
          {calLoading ? <p className="muted">Chargement…</p> : null}
          <div className="res-cal-wrap">
            <Calendar
              value={calDate}
              onChange={(v) => {
                if (v instanceof Date) setCalDate(v)
              }}
              locale="fr-FR"
              tileClassName={tileClassName}
            />
          </div>
          <h3 className="users-h2">
            {calDate.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
          </h3>
          {!dayDetail.length ? (
            <p className="muted">Aucune réservation ce jour.</p>
          ) : (
            <ul className="res-cal-daylist">
              {dayDetail.map((r) => (
                <li key={r.id} className="res-cal-daylist__item">
                  <strong>{statusLabel(r.status)}</strong> — <code>{r.vehiclePlate}</code> — {fmtDt(r.startDatetime)} →{' '}
                  {fmtDt(r.endDatetime)}
                  <div className="cell-actions--stack" style={{ marginTop: '0.35rem' }}>
                    {rowActions(r)}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      ) : null}

      <section className="card">
        <h2 className="users-h2">Nouvelle demande</h2>
        <form className="user-form" onSubmit={(e) => void onCreate(e)}>
          <div className="form-grid form-grid--vehicle">
            <label className="field field--full">
              <span>Véhicule *</span>
              <select required value={vehicleId} onChange={(e) => setVehicleId(e.target.value)}>
                <option value="">— Choisir —</option>
                {vehicles.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.plateNumber} — {v.brand} {v.model}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>Début *</span>
              <input
                type="datetime-local"
                required
                value={startLocal}
                onChange={(e) => setStartLocal(e.target.value)}
              />
            </label>
            <label className="field">
              <span>Fin *</span>
              <input
                type="datetime-local"
                required
                value={endLocal}
                onChange={(e) => setEndLocal(e.target.value)}
              />
            </label>
            <label className="field field--full">
              <span>Motif</span>
              <input value={reason} onChange={(e) => setReason(e.target.value)} maxLength={500} />
            </label>
            <label className="field field--full">
              <span>Destination</span>
              <input value={destination} onChange={(e) => setDestination(e.target.value)} maxLength={300} />
            </label>
            <label className="field">
              <span>Km estimés</span>
              <input
                type="number"
                min={0}
                value={estimatedKm}
                onChange={(e) => setEstimatedKm(e.target.value)}
              />
            </label>
            <label className="field">
              <span>Passagers</span>
              <input
                type="number"
                min={0}
                max={50}
                value={passengerCount}
                onChange={(e) => setPassengerCount(e.target.value)}
              />
            </label>
          </div>
          <button type="submit">Envoyer la demande</button>
          {formMsg ? <p className="muted">{formMsg}</p> : null}
        </form>
      </section>

      {viewMode === 'list' ? (
        <>
          <section className="card card--flat">
            <h2 className="users-h2">Mes réservations</h2>
            {loading ? <p className="muted">Chargement…</p> : renderTable(mine, 'Aucune demande pour le moment.')}
            <div className="pager">
              <button
                type="button"
                disabled={minePage <= 0}
                onClick={() => setMinePage((p) => Math.max(0, p - 1))}
              >
                Précédent
              </button>
              <span className="muted">
                Page {minePage + 1} / {Math.max(1, mineTotalPages)}
              </span>
              <button
                type="button"
                disabled={minePage + 1 >= mineTotalPages}
                onClick={() => setMinePage((p) => p + 1)}
              >
                Suivant
              </button>
            </div>
          </section>

          {canManage ? (
            <section className="card card--flat">
              <h2 className="users-h2">Toutes les réservations</h2>
              <div className="filters-grid filters-grid--wrap">
                <label className="field field--inline">
                  <span>Statut</span>
                  <select
                    value={filterStatus}
                    onChange={(e) => {
                      setFilterStatus(e.target.value)
                      setAllPage(0)
                    }}
                  >
                    <option value="">Tous</option>
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {statusLabel(s)}
                      </option>
                    ))}
                  </select>
                </label>
                <button type="button" className="btn-inline" onClick={() => void loadAll()}>
                  Actualiser
                </button>
              </div>
              {loading ? null : renderTable(allRows, 'Aucune réservation.')}
              <div className="pager">
                <button
                  type="button"
                  disabled={allPage <= 0}
                  onClick={() => setAllPage((p) => Math.max(0, p - 1))}
                >
                  Précédent
                </button>
                <span className="muted">
                  Page {allPage + 1} / {Math.max(1, allTotalPages)}
                </span>
                <button
                  type="button"
                  disabled={allPage + 1 >= allTotalPages}
                  onClick={() => setAllPage((p) => p + 1)}
                >
                  Suivant
                </button>
              </div>
            </section>
          ) : null}
        </>
      ) : null}

      {rejectId ? (
        <div className="reject-modal" role="dialog" aria-modal="true">
          <div className="reject-modal__panel">
            <h2 className="users-h2">Refuser la demande</h2>
            <form onSubmit={(e) => void onRejectSubmit(e)}>
              <label className="field field--full">
                <span>Motif du refus *</span>
                <textarea
                  required
                  rows={3}
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  maxLength={2000}
                />
              </label>
              <button type="submit">Refuser</button>
              <button type="button" className="btn-secondary" onClick={() => setRejectId(null)}>
                Fermer
              </button>
            </form>
          </div>
        </div>
      ) : null}

      {cancelTarget ? (
        <div className="reject-modal" role="dialog" aria-modal="true">
          <div className="reject-modal__panel">
            <h2 className="users-h2">Annuler la réservation</h2>
            <p className="tiny muted">
              <code>{cancelTarget.vehiclePlate}</code> — {fmtDt(cancelTarget.startDatetime)}
            </p>
            <form onSubmit={(e) => void onCancelConfirm(e)}>
              <label className="field field--full">
                <span>Motif (optionnel)</span>
                <textarea
                  rows={3}
                  value={cancelReasonDraft}
                  onChange={(e) => setCancelReasonDraft(e.target.value)}
                  maxLength={2000}
                  placeholder="Ex. : véhicule plus nécessaire, report de mission…"
                />
              </label>
              <button type="submit">Confirmer l&apos;annulation</button>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setCancelTarget(null)
                  setCancelReasonDraft('')
                }}
              >
                Fermer
              </button>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}
