import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import {
  createAssignment,
  endAssignment,
  fetchAllAssignments,
  fetchMyAssignments,
  withdrawAssignment,
} from '../api/assignments'
import { fetchVehicles } from '../api/vehicles'
import { fetchUsers } from '../api/users'
import type {
  AssignmentDto,
  AssignmentPageResponse,
  AssignmentStatus,
  CreateAssignmentPayload,
} from '../types/assignment'
import type { UserDto } from '../types/user'
import type { VehicleDto } from '../types/vehicle'

const STATUSES: AssignmentStatus[] = ['ACTIVE', 'ENDED', 'WITHDRAWN']

function statusLabel(s: AssignmentStatus): string {
  switch (s) {
    case 'ACTIVE':
      return 'Active'
    case 'ENDED':
      return 'Terminée'
    case 'WITHDRAWN':
      return 'Retirée'
    default:
      return s
  }
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

export function AssignmentsPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canManage = useMemo(() => meAuths?.includes('ASSIGNMENT_MANAGE') ?? false, [meAuths])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [drivers, setDrivers] = useState<UserDto[]>([])

  const [mineRows, setMineRows] = useState<AssignmentDto[]>([])
  const [minePage, setMinePage] = useState(0)
  const [mineTotalPages, setMineTotalPages] = useState(1)

  const [allRows, setAllRows] = useState<AssignmentDto[]>([])
  const [allPage, setAllPage] = useState(0)
  const [allTotalPages, setAllTotalPages] = useState(1)
  const [filterStatus, setFilterStatus] = useState('')

  const [vehicleId, setVehicleId] = useState('')
  const [driverId, setDriverId] = useState('')
  const [assignmentType, setAssignmentType] = useState('MISSION')
  const [startLocal, setStartLocal] = useState('')
  const [endLocal, setEndLocal] = useState('')
  const [mileageStart, setMileageStart] = useState('')
  const [mileageEnd, setMileageEnd] = useState('')
  const [reason, setReason] = useState('')

  const loadMe = useCallback(async () => {
    try {
      const res = await apiFetch('/api/me')
      if (!res.ok) throw new Error(`Impossible de charger le profil (${res.status})`)
      const me = (await res.json()) as { authorities: string[] }
      setMeAuths(me.authorities)
    } catch {
      setMeAuths([])
    }
  }, [])

  const loadVehicles = useCallback(async () => {
    const data = await fetchVehicles({ page: 0, size: 200 })
    setVehicles(data.content)
    setVehicleId((v) => (v ? v : data.content[0]?.id ?? ''))
  }, [])

  const loadDrivers = useCallback(async () => {
    const data = await fetchUsers({ page: 0, size: 200, status: 'ACTIVE', role: 'DRIVER' })
    setDrivers(data.content)
    setDriverId((d) => (d ? d : data.content[0]?.id ?? ''))
  }, [])

  const loadMine = useCallback(async () => {
    const data: AssignmentPageResponse = await fetchMyAssignments({ page: minePage, size: 15 })
    setMineRows(data.content)
    setMineTotalPages(data.totalPages)
  }, [minePage])

  const loadAll = useCallback(async () => {
    if (!canManage) return
    const data: AssignmentPageResponse = await fetchAllAssignments({
      page: allPage,
      size: 15,
      status: filterStatus || undefined,
    })
    setAllRows(data.content)
    setAllTotalPages(data.totalPages)
  }, [allPage, canManage, filterStatus])

  useEffect(() => {
    void loadMe()
  }, [loadMe])

  useEffect(() => {
    if (!canManage) return
    void (async () => {
      setError(null)
      try {
        await loadVehicles()
        await loadDrivers()
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erreur')
      }
    })()
  }, [canManage, loadDrivers, loadVehicles])

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
  }, [canManage, loadAll, loadMine])

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setError(null)

    if (!vehicleId || !driverId || !startLocal || !endLocal) {
      setError('Véhicule, conducteur et plages horaires sont requis.')
      return
    }

    const payload: CreateAssignmentPayload = {
      vehicleId,
      driverId,
      assignmentType: assignmentType.trim() || 'MISSION',
      startDatetime: toIsoUtc(startLocal),
      endDatetime: toIsoUtc(endLocal),
      mileageStart: mileageStart === '' ? null : Number(mileageStart),
      mileageEnd: mileageEnd === '' ? null : Number(mileageEnd),
      reason: reason.trim() || null,
    }

    try {
      await createAssignment(payload)
      setReason('')
      setMileageStart('')
      setMileageEnd('')
      await loadMine()
      if (canManage) await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onEnd(a: AssignmentDto) {
    if (!confirm('Mettre fin à cette attribution ?')) return
    try {
      await endAssignment(a.id, { endDatetime: new Date().toISOString(), reason: null })
      await loadMine()
      if (canManage) await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur')
    }
  }

  async function onWithdraw(a: AssignmentDto) {
    if (!confirm('Retirer cette attribution ?')) return
    const r = prompt('Motif (optionnel) :') ?? ''
    try {
      await withdrawAssignment(a.id, { reason: r.trim() || null })
      await loadMine()
      if (canManage) await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur')
    }
  }

  function renderTable(rows: AssignmentDto[], empty: string) {
    if (!rows.length) return <p className="muted">{empty}</p>
    return (
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Véhicule</th>
              <th>Conducteur</th>
              <th>Début</th>
              <th>Fin</th>
              <th>Type</th>
              <th>Statut</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {rows.map((a) => (
              <tr key={a.id}>
                <td>
                  <Link to={`/dashboard/vehicles/${a.vehicleId}`}>
                    <code>{a.vehiclePlate}</code>
                  </Link>
                  <div className="muted tiny">{a.vehicleLabel}</div>
                </td>
                <td>
                  {a.driverFirstname} {a.driverLastname}
                  <div className="muted tiny">{a.driverEmail}</div>
                </td>
                <td>{fmtDt(a.startDatetime)}</td>
                <td>{fmtDt(a.endDatetime)}</td>
                <td>{a.assignmentType}</td>
                <td>{statusLabel(a.status)}</td>
                <td className="cell-actions">
                  {canManage && a.status === 'ACTIVE' ? (
                    <div className="cell-actions cell-actions--stack">
                      <button type="button" className="linkish" onClick={() => void onEnd(a)}>
                        Fin
                      </button>
                      <button
                        type="button"
                        className="linkish danger"
                        onClick={() => void onWithdraw(a)}
                      >
                        Retirer
                      </button>
                    </div>
                  ) : null}
                </td>
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
      <h1>Attributions</h1>
      <p className="dash-lead">
        Assigner un véhicule à un conducteur, puis terminer/retirer l’attribution.
      </p>

      {error ? <p className="alert alert--error">{error}</p> : null}

      {canManage ? (
        <section className="card">
          <h2 className="users-h2">Nouvelle attribution</h2>
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
              <label className="field field--full">
                <span>Conducteur *</span>
                <select required value={driverId} onChange={(e) => setDriverId(e.target.value)}>
                  <option value="">— Choisir —</option>
                  {drivers.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.firstname} {d.lastname}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field field--full">
                <span>Type</span>
                <input
                  value={assignmentType}
                  onChange={(e) => setAssignmentType(e.target.value)}
                  maxLength={30}
                />
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
              <label className="field">
                <span>Km début</span>
                <input
                  type="number"
                  min={0}
                  value={mileageStart}
                  onChange={(e) => setMileageStart(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Km fin</span>
                <input
                  type="number"
                  min={0}
                  value={mileageEnd}
                  onChange={(e) => setMileageEnd(e.target.value)}
                />
              </label>
              <label className="field field--full">
                <span>Motif</span>
                <textarea
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  maxLength={2000}
                />
              </label>
            </div>
            <button type="submit">Créer l’attribution</button>
          </form>
        </section>
      ) : null}

      <section className="card card--flat">
        <h2 className="users-h2">Mes attributions</h2>
        {loading ? (
          <p className="muted">Chargement…</p>
        ) : (
          renderTable(mineRows, 'Aucune attribution pour le moment.')
        )}
        <div className="pager">
          <button type="button" disabled={minePage <= 0} onClick={() => setMinePage((p) => Math.max(0, p - 1))}>
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
          <h2 className="users-h2">Toutes les attributions</h2>
          <div className="filters-grid filters-grid--wrap">
            <label className="field field--inline">
              <span>Statut</span>
              <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
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
          {renderTable(allRows, 'Aucune attribution.')}
          <div className="pager">
            <button type="button" disabled={allPage <= 0} onClick={() => setAllPage((p) => Math.max(0, p - 1))}>
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
    </div>
  )
}

