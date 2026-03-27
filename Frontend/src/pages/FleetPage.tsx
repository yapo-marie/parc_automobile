import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { createPoolAssignment } from '../api/assignments'
import {
  createFleetVehicle,
  deleteFleetVehicle,
  fetchFleetStats,
  fetchFleetVehicles,
  updateFleetVehicle,
} from '../api/fleet'
import { fetchUsers } from '../api/users'
import { fetchVehicles } from '../api/vehicles'
import type { CreatePoolAssignmentPayload } from '../types/assignment'
import type {
  CreateFleetVehiclePayload,
  FleetStatsDto,
  FleetVehicleDto,
  UpdateFleetVehiclePayload,
} from '../types/fleet'
import type { UserDto } from '../types/user'
import type { VehicleDto } from '../types/vehicle'

function num(v: number | string | null | undefined): number {
  if (v === null || v === undefined) return 0
  if (typeof v === 'number') return Number.isFinite(v) ? v : 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

function fmtEuro(n: number): string {
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(n)
}

function fmtPct(n: number): string {
  return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(n)} %`
}

function toIsoUtc(localDatetime: string): string {
  return new Date(localDatetime).toISOString()
}

function availabilityLabel(a: string): string {
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

export function FleetPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canFleet = useMemo(() => meAuths?.includes('FLEET_MANAGE') ?? false, [meAuths])
  const canAssign = useMemo(() => meAuths?.includes('ASSIGNMENT_MANAGE') ?? false, [meAuths])

  const [stats, setStats] = useState<FleetStatsDto | null>(null)
  const [rows, setRows] = useState<FleetVehicleDto[]>([])
  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [drivers, setDrivers] = useState<UserDto[]>([])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [filterAdmin, setFilterAdmin] = useState('')
  const [appliedAdmin, setAppliedAdmin] = useState('')

  const [modal, setModal] = useState<'create' | 'edit' | 'assign' | null>(null)
  const [editing, setEditing] = useState<FleetVehicleDto | null>(null)
  const [assignTarget, setAssignTarget] = useState<FleetVehicleDto | null>(null)

  const [formVehicleId, setFormVehicleId] = useState('')
  const [formAdmin, setFormAdmin] = useState('')
  const [formDaily, setFormDaily] = useState('')
  const [formKm, setFormKm] = useState('')
  const [formAnnual, setFormAnnual] = useState('')
  const [formStart, setFormStart] = useState('')
  const [formEnd, setFormEnd] = useState('')
  const [formNotes, setFormNotes] = useState('')

  const [aDriver, setADriver] = useState('')
  const [aType, setAType] = useState('MISSION')
  const [aStart, setAStart] = useState('')
  const [aEnd, setAEnd] = useState('')
  const [aMileageStart, setAMileageStart] = useState('')
  const [aMileageEnd, setAMileageEnd] = useState('')
  const [aReason, setAReason] = useState('')

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

  const loadAll = useCallback(async () => {
    if (!canFleet) return
    setError(null)
    const [st, page, vdata, udata] = await Promise.all([
      fetchFleetStats(),
      fetchFleetVehicles({ page: 0, size: 500, administration: appliedAdmin || undefined }),
      fetchVehicles({ page: 0, size: 300 }),
      fetchUsers({ page: 0, size: 200, status: 'ACTIVE', role: 'DRIVER' }),
    ])
    setStats(st)
    setRows(page.content)
    setVehicles(vdata.content)
    setDrivers(udata.content)
    setFormVehicleId((id) => id || vdata.content[0]?.id || '')
    setADriver((d) => d || udata.content[0]?.id || '')
  }, [canFleet, appliedAdmin])

  useEffect(() => {
    void loadMe()
  }, [loadMe])

  useEffect(() => {
    if (!canFleet) {
      setLoading(false)
      return
    }
    setLoading(true)
    void (async () => {
      try {
        await loadAll()
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erreur')
      } finally {
        setLoading(false)
      }
    })()
  }, [canFleet, loadAll])

  const fleetVehicleIds = useMemo(() => new Set(rows.map((r) => r.vehicleId)), [rows])

  const vehicleChoices = useMemo(() => {
    if (modal === 'edit' && editing) {
      return vehicles.filter((v) => v.id === editing.vehicleId)
    }
    return vehicles.filter((v) => !fleetVehicleIds.has(v.id))
  }, [vehicles, fleetVehicleIds, modal, editing])

  const grouped = useMemo(() => {
    const m = new Map<string, FleetVehicleDto[]>()
    const sorted = [...rows].sort((a, b) => {
      const c = a.administration.localeCompare(b.administration, 'fr')
      if (c !== 0) return c
      return a.plateNumber.localeCompare(b.plateNumber, 'fr')
    })
    for (const r of sorted) {
      const k = r.administration || '—'
      if (!m.has(k)) m.set(k, [])
      m.get(k)!.push(r)
    }
    return Array.from(m.entries()).sort((a, b) => a[0].localeCompare(b[0], 'fr'))
  }, [rows])

  const totalAnnualFromRows = useMemo(
    () => rows.reduce((s, r) => s + num(r.annualBudget), 0),
    [rows],
  )

  function openCreate() {
    setEditing(null)
    setFormVehicleId(vehicleChoices[0]?.id ?? '')
    setFormAdmin('')
    setFormDaily('')
    setFormKm('')
    setFormAnnual('')
    setFormStart('')
    setFormEnd('')
    setFormNotes('')
    setModal('create')
  }

  function openEdit(row: FleetVehicleDto) {
    setEditing(row)
    setFormAdmin(row.administration)
    setFormDaily(row.dailyCost != null ? String(row.dailyCost) : '')
    setFormKm(row.costPerKm != null ? String(row.costPerKm) : '')
    setFormAnnual(row.annualBudget != null ? String(row.annualBudget) : '')
    setFormStart(row.startDate ?? '')
    setFormEnd(row.endDate ?? '')
    setFormNotes(row.notes ?? '')
    setModal('edit')
  }

  function openAssign(row: FleetVehicleDto) {
    setAssignTarget(row)
    setAType('MISSION')
    setAStart('')
    setAEnd('')
    setAMileageStart('')
    setAMileageEnd('')
    setAReason('')
    setADriver((d) => d || drivers[0]?.id || '')
    setModal('assign')
  }

  function closeModal() {
    setModal(null)
    setEditing(null)
    setAssignTarget(null)
  }

  async function onSubmitFleet(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      if (modal === 'create') {
        if (vehicleChoices.length === 0) {
          setError('Aucun véhicule disponible à ajouter.')
          return
        }
        if (!formVehicleId || !formAdmin.trim()) {
          setError('Véhicule et administration sont requis.')
          return
        }
        const payload: CreateFleetVehiclePayload = {
          vehicleId: formVehicleId,
          administration: formAdmin.trim(),
          dailyCost: formDaily === '' ? null : Number(formDaily),
          costPerKm: formKm === '' ? null : Number(formKm),
          annualBudget: formAnnual === '' ? null : Number(formAnnual),
          startDate: formStart || null,
          endDate: formEnd || null,
          notes: formNotes.trim() || null,
        }
        await createFleetVehicle(payload)
      } else if (modal === 'edit' && editing) {
        const payload: UpdateFleetVehiclePayload = {
          administration: formAdmin.trim(),
          dailyCost: formDaily === '' ? null : Number(formDaily),
          costPerKm: formKm === '' ? null : Number(formKm),
          annualBudget: formAnnual === '' ? null : Number(formAnnual),
          startDate: formStart || null,
          endDate: formEnd || null,
          notes: formNotes.trim() || null,
        }
        if (!formAdmin.trim()) {
          setError('Administration requise.')
          return
        }
        await updateFleetVehicle(editing.id, payload)
      }
      closeModal()
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onAssign(e: FormEvent) {
    e.preventDefault()
    if (!assignTarget || !aDriver || !aStart || !aEnd) {
      setError('Conducteur et plages horaires sont requis.')
      return
    }
    setError(null)
    try {
      const payload: CreatePoolAssignmentPayload = {
        fleetVehicleId: assignTarget.id,
        driverId: aDriver,
        assignmentType: aType.trim() || 'MISSION',
        startDatetime: toIsoUtc(aStart),
        endDatetime: toIsoUtc(aEnd),
        mileageStart: aMileageStart === '' ? null : Number(aMileageStart),
        mileageEnd: aMileageEnd === '' ? null : Number(aMileageEnd),
        reason: aReason.trim() || null,
      }
      await createPoolAssignment(payload)
      closeModal()
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Échec attribution')
    }
  }

  async function onDelete(row: FleetVehicleDto) {
    if (!window.confirm(`Retirer ${row.plateNumber} de la flotte (suppression logique) ?`)) return
    setError(null)
    try {
      await deleteFleetVehicle(row.id)
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Échec')
    }
  }

  if (!canFleet && meAuths !== null) {
    return (
      <div className="dash-page users-page">
        <h1>Flotte administrative</h1>
        <p className="dash-lead">
          Vous n’avez pas la permission <code>FLEET_MANAGE</code> nécessaire pour gérer le pool
          flotte.
        </p>
      </div>
    )
  }

  return (
    <div className="dash-page users-page">
      <h1>Flotte administrative</h1>
      <p className="dash-lead">
        Suivi des véhicules de flotte par administration, budgets et attribution depuis le pool.
      </p>

      {error ? (
        <p className="alert alert--error" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? <p className="muted">Chargement…</p> : null}

      {!loading && stats ? (
        <section className="grid-kpi">
          <article className="kpi">
            <span className="kpi__label">Véhicules en flotte</span>
            <span className="kpi__value">{stats.totalFleetVehicles}</span>
          </article>
          <article className="kpi">
            <span className="kpi__label">Budget annuel total</span>
            <span className="kpi__value kpi__value--sm">{fmtEuro(num(stats.totalAnnualBudget))}</span>
          </article>
          <article className="kpi">
            <span className="kpi__label">Budget utilisé (visites, année)</span>
            <span className="kpi__value kpi__value--sm">{fmtPct(num(stats.globalBudgetUsedPercent))}</span>
          </article>
          <article className="kpi">
            <span className="kpi__label">Taux d’utilisation</span>
            <span className="kpi__value kpi__value--sm">
              {fmtPct(num(stats.fleetUtilizationPercent))}
            </span>
            <span className="tiny muted">Véhicules marqués « attribué »</span>
          </article>
        </section>
      ) : null}

      <section className="users-filters">
        <div className="filters-grid">
          <label className="field field--inline">
            <span>Filtrer par administration</span>
            <input
              value={filterAdmin}
              onChange={(e) => setFilterAdmin(e.target.value)}
              placeholder="Ex. : Direction générale"
            />
          </label>
          <button
            type="button"
            className="btn-secondary btn-inline"
            onClick={() => setAppliedAdmin(filterAdmin.trim())}
          >
            Appliquer
          </button>
          <button type="button" className="btn-inline" onClick={openCreate}>
            + Ajouter à la flotte
          </button>
        </div>
      </section>

      {grouped.map(([admin, list]) => {
        const groupAnnual = list.reduce((s, r) => s + num(r.annualBudget), 0)
        const sharePct =
          totalAnnualFromRows > 0 ? (groupAnnual / totalAnnualFromRows) * 100 : 0
        const barFill =
          stats && num(stats.globalBudgetUsedPercent) > 0
            ? Math.min(100, num(stats.globalBudgetUsedPercent))
            : 0

        return (
          <section key={admin} className="card fleet-admin-block">
            <div className="fleet-admin-head">
              <h2 className="users-h2">{admin}</h2>
              <p className="tiny muted">
                Budget annuel groupe : {fmtEuro(groupAnnual)}
                {totalAnnualFromRows > 0
                  ? ` · ${fmtPct(sharePct)} du budget total`
                  : null}
              </p>
              <div className="fleet-budget-bar" title="Avancement budget global (visites terminées)">
                <div className="fleet-budget-bar__track">
                  <div className="fleet-budget-bar__fill" style={{ width: `${barFill}%` }} />
                </div>
                <span className="tiny muted">
                  Indicateur global : {fmtPct(barFill)} (cohortes flotte)
                </span>
              </div>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Véhicule</th>
                    <th>Disponibilité</th>
                    <th>Coût / jour</th>
                    <th>Coût / km</th>
                    <th>Budget annuel</th>
                    <th>Période</th>
                    <th className="cell-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {list.map((r) => (
                    <tr key={r.id}>
                      <td>
                        <Link to={`/dashboard/vehicles/${r.vehicleId}`}>
                          {r.plateNumber} — {r.brand} {r.model}
                        </Link>
                      </td>
                      <td>{availabilityLabel(r.vehicleAvailability)}</td>
                      <td>{r.dailyCost != null ? fmtEuro(num(r.dailyCost)) : '—'}</td>
                      <td>{r.costPerKm != null ? `${num(r.costPerKm)} €/km` : '—'}</td>
                      <td>{r.annualBudget != null ? fmtEuro(num(r.annualBudget)) : '—'}</td>
                      <td className="tiny">
                        {[r.startDate, r.endDate].filter(Boolean).join(' → ') || '—'}
                      </td>
                      <td className="cell-actions cell-actions--stack">
                        <button type="button" className="linkish" onClick={() => openEdit(r)}>
                          Modifier
                        </button>
                        {canAssign ? (
                          <button type="button" className="linkish" onClick={() => openAssign(r)}>
                            Attribuer à une personne
                          </button>
                        ) : null}
                        <button type="button" className="linkish" onClick={() => onDelete(r)}>
                          Retirer
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )
      })}

      {!loading && rows.length === 0 ? (
        <p className="muted">Aucun véhicule dans la flotte. Ajoutez-en un pour commencer.</p>
      ) : null}

      {modal === 'create' || modal === 'edit' ? (
        <div className="reject-modal" role="presentation" onClick={closeModal}>
          <div
            className="reject-modal__panel fleet-modal"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="users-h2">{modal === 'create' ? 'Ajouter à la flotte' : 'Modifier'}</h2>
            <form onSubmit={onSubmitFleet}>
              {modal === 'create' ? (
                <label className="field">
                  <span>Véhicule</span>
                  {vehicleChoices.length === 0 ? (
                    <p className="alert alert--warn">
                      Tous les véhicules visibles sont déjà dans la flotte ou aucun véhicule n’est
                      disponible.
                    </p>
                  ) : (
                    <select
                      value={formVehicleId}
                      onChange={(e) => setFormVehicleId(e.target.value)}
                      required
                    >
                      {vehicleChoices.map((v) => (
                        <option key={v.id} value={v.id}>
                          {v.plateNumber} — {v.brand} {v.model}
                        </option>
                      ))}
                    </select>
                  )}
                </label>
              ) : null}
              <label className="field">
                <span>Administration</span>
                <input value={formAdmin} onChange={(e) => setFormAdmin(e.target.value)} required />
              </label>
              <label className="field">
                <span>Coût journalier (€)</span>
                <input
                  type="number"
                  step="0.01"
                  value={formDaily}
                  onChange={(e) => setFormDaily(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Coût au km (€)</span>
                <input
                  type="number"
                  step="0.0001"
                  value={formKm}
                  onChange={(e) => setFormKm(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Budget annuel (€)</span>
                <input
                  type="number"
                  step="0.01"
                  value={formAnnual}
                  onChange={(e) => setFormAnnual(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Date début</span>
                <input type="date" value={formStart} onChange={(e) => setFormStart(e.target.value)} />
              </label>
              <label className="field">
                <span>Date fin</span>
                <input type="date" value={formEnd} onChange={(e) => setFormEnd(e.target.value)} />
              </label>
              <label className="field">
                <span>Notes</span>
                <textarea value={formNotes} onChange={(e) => setFormNotes(e.target.value)} rows={3} />
              </label>
              <button type="submit">Enregistrer</button>
              <button type="button" className="btn-secondary" onClick={closeModal}>
                Annuler
              </button>
            </form>
          </div>
        </div>
      ) : null}

      {modal === 'assign' && assignTarget ? (
        <div className="reject-modal" role="presentation" onClick={closeModal}>
          <div
            className="reject-modal__panel fleet-modal"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="users-h2">
              Attribution — {assignTarget.plateNumber}{' '}
              <span className="tiny muted">(pool flotte)</span>
            </h2>
            <form onSubmit={onAssign}>
              <label className="field">
                <span>Conducteur</span>
                <select value={aDriver} onChange={(e) => setADriver(e.target.value)} required>
                  {drivers.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.firstname} {d.lastname} ({d.email})
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Type</span>
                <input value={aType} onChange={(e) => setAType(e.target.value)} />
              </label>
              <label className="field">
                <span>Début</span>
                <input
                  type="datetime-local"
                  value={aStart}
                  onChange={(e) => setAStart(e.target.value)}
                  required
                />
              </label>
              <label className="field">
                <span>Fin</span>
                <input
                  type="datetime-local"
                  value={aEnd}
                  onChange={(e) => setAEnd(e.target.value)}
                  required
                />
              </label>
              <label className="field">
                <span>Km départ</span>
                <input
                  type="number"
                  value={aMileageStart}
                  onChange={(e) => setAMileageStart(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Km fin</span>
                <input
                  type="number"
                  value={aMileageEnd}
                  onChange={(e) => setAMileageEnd(e.target.value)}
                />
              </label>
              <label className="field">
                <span>Motif</span>
                <input value={aReason} onChange={(e) => setAReason(e.target.value)} />
              </label>
              <button type="submit">Créer l’attribution</button>
              <button type="button" className="btn-secondary" onClick={closeModal}>
                Annuler
              </button>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}
