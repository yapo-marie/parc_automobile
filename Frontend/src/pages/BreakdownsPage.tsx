import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { fetchVehicles } from '../api/vehicles'
import { createBreakdown, fetchBreakdowns, resolveBreakdown } from '../api/breakdowns'
import type { BreakdownDto, BreakdownPageResponse, BreakdownStatus, CreateBreakdownPayload } from '../types/breakdown'
import type { VehicleDto } from '../types/vehicle'

type FormState = {
  vehicleId: string
  description: string
  mileageAtBreakdown: string
  garage: string
  repairCost: string
}

function emptyForm(vehicleId: string): FormState {
  return {
    vehicleId,
    description: '',
    mileageAtBreakdown: '',
    garage: '',
    repairCost: '',
  }
}

function toPayload(form: FormState): CreateBreakdownPayload {
  return {
    vehicleId: form.vehicleId,
    description: form.description.trim(),
    mileageAtBreakdown: form.mileageAtBreakdown ? Number(form.mileageAtBreakdown) : null,
    garage: form.garage ? form.garage.trim() : null,
    repairCost: form.repairCost ? Number(form.repairCost) : null,
  }
}

function statusLabel(s: BreakdownStatus): string {
  return s === 'DECLAREE' ? 'Déclarée' : 'Résolue'
}

export function BreakdownsPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canManage = useMemo(() => meAuths?.includes('FLEET_MANAGE') ?? false, [meAuths])
  const canDeclare = useMemo(
    () =>
      Boolean(meAuths?.includes('FLEET_MANAGE') || meAuths?.includes('VEHICLE_READ')),
    [meAuths],
  )

  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [rows, setRows] = useState<BreakdownDto[]>([])
  const [page, setPage] = useState(0)
  const [size] = useState(15)
  const [totalPages, setTotalPages] = useState(1)

  const [filterVehicleId, setFilterVehicleId] = useState('')
  const [filterStatus, setFilterStatus] = useState('')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [form, setForm] = useState<FormState>(() => emptyForm(''))
  const [msg, setMsg] = useState<string | null>(null)

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
    const firstId = data.content[0]?.id ?? ''
    setForm((f) => (f.vehicleId ? f : emptyForm(firstId)))
  }, [])

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data: BreakdownPageResponse = await fetchBreakdowns({
        page,
        size,
        vehicleId: filterVehicleId || undefined,
        status: filterStatus || undefined,
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setLoading(false)
    }
  }, [filterStatus, filterVehicleId, page, size])

  useEffect(() => {
    void loadMe()
    void loadVehicles()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setMsg(null)
    if (!canDeclare) return
    if (!form.vehicleId || !form.description.trim()) {
      setMsg('Véhicule et description sont requis.')
      return
    }
    try {
      await createBreakdown(toPayload(form))
      setMsg('Panne déclarée.')
      setForm((f) => emptyForm(f.vehicleId))
      await load()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onResolve(row: BreakdownDto) {
    if (!confirm('Marquer cette panne comme résolue ?')) return
    try {
      await resolveBreakdown(row.id, { resolvedAt: new Date().toISOString() })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur')
    }
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Pannes</h1>
      <p className="dash-lead">Déclaration et résolution des pannes.</p>

      {error ? <p className="alert alert--error">{error}</p> : null}

      <section className="card card--flat users-filters">
        <h2 className="users-h2">Filtres</h2>
        <div className="filters-grid filters-grid--wrap">
          <label className="field field--inline">
            <span>Véhicule</span>
            <select value={filterVehicleId} onChange={(e) => setFilterVehicleId(e.target.value)}>
              <option value="">Tous</option>
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.plateNumber} — {v.brand} {v.model}
                </option>
              ))}
            </select>
          </label>
          <label className="field field--inline">
            <span>Statut</span>
            <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <option value="">Tous</option>
              <option value="DECLAREE">Déclarée</option>
              <option value="RESOLUE">Résolue</option>
            </select>
          </label>
          <button type="button" className="btn-inline" onClick={() => void load()}>
            Appliquer
          </button>
        </div>
      </section>

      <section className="card card--flat">
        <h2 className="users-h2">Liste</h2>
        {loading ? <p className="muted">Chargement…</p> : null}
        {!loading ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Description</th>
                  <th>Déclarée</th>
                  <th>Garage</th>
                  <th>Coût</th>
                  <th>Statut</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <code>{r.vehiclePlate}</code>
                      <div className="muted tiny">{r.vehicleLabel}</div>
                    </td>
                    <td>
                      {r.description}
                      {r.mileageAtBreakdown != null ? (
                        <div className="muted tiny">Km : {r.mileageAtBreakdown}</div>
                      ) : null}
                    </td>
                    <td>{r.declaredAt ? new Date(r.declaredAt).toLocaleString('fr-FR') : '—'}</td>
                    <td>{r.garage ?? '—'}</td>
                    <td>{r.repairCost == null ? '—' : r.repairCost}</td>
                    <td>{statusLabel(r.status)}</td>
                    <td>
                      {canManage && r.status === 'DECLAREE' ? (
                        <button type="button" className="linkish" onClick={() => void onResolve(r)}>
                          Résoudre
                        </button>
                      ) : null}
                    </td>
                  </tr>
                ))}
                {!rows.length ? (
                  <tr>
                    <td colSpan={7} className="muted">
                      Aucune panne.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        ) : null}

        <div className="pager">
          <button type="button" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
            Précédent
          </button>
          <span className="muted">
            Page {page + 1} / {Math.max(1, totalPages)}
          </span>
          <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Suivant
          </button>
        </div>
      </section>

      {canDeclare ? (
        <section className="card">
          <h2 className="users-h2">Déclarer une panne</h2>
          <form className="user-form" onSubmit={(e) => void onSubmit(e)}>
            <div className="form-grid form-grid--vehicle">
              <label className="field field--full">
                <span>Véhicule *</span>
                <select required value={form.vehicleId} onChange={(e) => setForm({ ...form, vehicleId: e.target.value })}>
                  <option value="">— Choisir —</option>
                  {vehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.plateNumber} — {v.brand} {v.model}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field field--full">
                <span>Description *</span>
                <textarea rows={3} required value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} maxLength={5000} />
              </label>
              <label className="field">
                <span>Km</span>
                <input type="number" min={0} value={form.mileageAtBreakdown} onChange={(e) => setForm({ ...form, mileageAtBreakdown: e.target.value })} />
              </label>
              <label className="field">
                <span>Garage</span>
                <input value={form.garage} onChange={(e) => setForm({ ...form, garage: e.target.value })} maxLength={200} />
              </label>
              <label className="field">
                <span>Coût réparation</span>
                <input type="number" step="0.01" min={0} value={form.repairCost} onChange={(e) => setForm({ ...form, repairCost: e.target.value })} />
              </label>
            </div>
            <button type="submit">Enregistrer</button>
            {msg ? <p className="muted">{msg}</p> : null}
          </form>
        </section>
      ) : null}
    </div>
  )
}

