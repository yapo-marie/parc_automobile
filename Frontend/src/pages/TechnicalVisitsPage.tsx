import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { fetchVehicles } from '../api/vehicles'
import { createTechnicalVisit, fetchTechnicalVisits, updateTechnicalVisit } from '../api/technicalVisits'
import type { CreateTechnicalVisitPayload, TechnicalVisitPageResponse, TechnicalVisitResponse, UpdateTechnicalVisitPayload } from '../types/technicalVisit'
import type { VehicleDto } from '../types/vehicle'

type FormState = {
  vehicleId: string
  type: string
  scheduledDate: string
  completedDate: string
  result: string
  garage: string
  cost: string
  nextDueDate: string
  comments: string
}

function emptyForm(vehicleId: string): FormState {
  return {
    vehicleId,
    type: '',
    scheduledDate: '',
    completedDate: '',
    result: '',
    garage: '',
    cost: '',
    nextDueDate: '',
    comments: '',
  }
}

function toPayload(f: FormState): CreateTechnicalVisitPayload {
  return {
    vehicleId: f.vehicleId,
    type: f.type.trim(),
    scheduledDate: f.scheduledDate,
    completedDate: f.completedDate ? f.completedDate : null,
    result: f.result ? f.result.trim() : null,
    garage: f.garage ? f.garage.trim() : null,
    cost: f.cost ? Number(f.cost) : null,
    nextDueDate: f.nextDueDate ? f.nextDueDate : null,
    comments: f.comments ? f.comments : null,
  }
}

function toUpdatePayload(f: FormState): UpdateTechnicalVisitPayload {
  return {
    type: f.type.trim(),
    scheduledDate: f.scheduledDate,
    completedDate: f.completedDate ? f.completedDate : null,
    result: f.result ? f.result.trim() : null,
    garage: f.garage ? f.garage.trim() : null,
    cost: f.cost ? Number(f.cost) : null,
    nextDueDate: f.nextDueDate ? f.nextDueDate : null,
    comments: f.comments ? f.comments : null,
  }
}

export function TechnicalVisitsPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canManage = useMemo(() => meAuths?.includes('FLEET_MANAGE') ?? false, [meAuths])

  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [rows, setRows] = useState<TechnicalVisitResponse[]>([])
  const [page, setPage] = useState(0)
  const [size] = useState(15)
  const [totalPages, setTotalPages] = useState(1)

  const [filterVehicleId, setFilterVehicleId] = useState('')
  const [filterType, setFilterType] = useState('')
  const [filterResult, setFilterResult] = useState('')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [editingId, setEditingId] = useState<string | null>(null)
  const [msg, setMsg] = useState<string | null>(null)
  const [form, setForm] = useState<FormState>(() => emptyForm(''))

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
    try {
      const data = await fetchVehicles({ page: 0, size: 200 })
      setVehicles(data.content)
      const firstId = data.content[0]?.id ?? ''
      setForm((f) => (f.vehicleId ? f : emptyForm(firstId)))
    } catch {
      setVehicles([])
    }
  }, [])

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data: TechnicalVisitPageResponse = await fetchTechnicalVisits({
        page,
        size,
        vehicleId: filterVehicleId || undefined,
        type: filterType || undefined,
        result: filterResult || undefined,
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setLoading(false)
    }
  }, [filterResult, filterType, filterVehicleId, page, size])

  useEffect(() => {
    void loadMe()
    void loadVehicles()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  function startEdit(v: TechnicalVisitResponse) {
    setEditingId(v.id)
    setMsg(null)
    setForm({
      vehicleId: v.vehicleId,
      type: v.type ?? '',
      scheduledDate: v.scheduledDate ?? '',
      completedDate: v.completedDate ?? '',
      result: v.result ?? '',
      garage: v.garage ?? '',
      cost: v.cost == null ? '' : String(v.cost),
      nextDueDate: v.nextDueDate ?? '',
      comments: v.comments ?? '',
    })
  }

  function cancelEdit() {
    setEditingId(null)
    setMsg(null)
    setForm((f) => emptyForm(f.vehicleId))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setMsg(null)
    if (!canManage) return
    if (!form.vehicleId || !form.type.trim() || !form.scheduledDate) {
      setMsg('Véhicule, type et date planifiée sont requis.')
      return
    }
    try {
      if (editingId) {
        await updateTechnicalVisit(editingId, toUpdatePayload(form))
        setMsg('Visite mise à jour.')
      } else {
        await createTechnicalVisit(toPayload(form))
        setMsg('Visite créée.')
      }
      setEditingId(null)
      setForm((f) => emptyForm(f.vehicleId))
      await load()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Visites techniques</h1>
      <p className="dash-lead">Planification et suivi des opérations techniques.</p>

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
            <span>Type</span>
            <input value={filterType} onChange={(e) => setFilterType(e.target.value)} />
          </label>
          <label className="field field--inline">
            <span>Résultat</span>
            <input value={filterResult} onChange={(e) => setFilterResult(e.target.value)} />
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
                  <th>Type</th>
                  <th>Planifiée</th>
                  <th>Terminée</th>
                  <th>Résultat</th>
                  <th>Garage</th>
                  <th>Coût</th>
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
                    <td>{r.type}</td>
                    <td>{r.scheduledDate ?? '—'}</td>
                    <td>{r.completedDate ?? '—'}</td>
                    <td>{r.result ?? '—'}</td>
                    <td>{r.garage ?? '—'}</td>
                    <td>{r.cost == null ? '—' : r.cost}</td>
                    <td>
                      {canManage ? (
                        <button type="button" className="linkish" onClick={() => startEdit(r)}>
                          Modifier
                        </button>
                      ) : null}
                    </td>
                  </tr>
                ))}
                {!rows.length ? (
                  <tr>
                    <td colSpan={8} className="muted">
                      Aucune visite.
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

      {canManage ? (
        <section className="card">
          <h2 className="users-h2">{editingId ? 'Modifier une visite' : 'Nouvelle visite'}</h2>
          {editingId ? (
            <p className="muted">
              Édition : <code>{editingId}</code>{' '}
              <button type="button" className="linkish" onClick={cancelEdit}>
                Annuler
              </button>
            </p>
          ) : null}
          <form className="user-form" onSubmit={(e) => void onSubmit(e)}>
            <div className="form-grid form-grid--vehicle">
              <label className="field field--full">
                <span>Véhicule *</span>
                <select
                  required
                  value={form.vehicleId}
                  onChange={(e) => setForm({ ...form, vehicleId: e.target.value })}
                >
                  <option value="">— Choisir —</option>
                  {vehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.plateNumber} — {v.brand} {v.model}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field field--full">
                <span>Type *</span>
                <input
                  required
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value })}
                  maxLength={50}
                />
              </label>
              <label className="field">
                <span>Date planifiée *</span>
                <input
                  type="date"
                  required
                  value={form.scheduledDate}
                  onChange={(e) => setForm({ ...form, scheduledDate: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Date terminée</span>
                <input
                  type="date"
                  value={form.completedDate}
                  onChange={(e) => setForm({ ...form, completedDate: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Résultat</span>
                <input
                  value={form.result}
                  onChange={(e) => setForm({ ...form, result: e.target.value })}
                  maxLength={30}
                />
              </label>
              <label className="field">
                <span>Garage</span>
                <input
                  value={form.garage}
                  onChange={(e) => setForm({ ...form, garage: e.target.value })}
                  maxLength={200}
                />
              </label>
              <label className="field">
                <span>Coût</span>
                <input
                  type="number"
                  step="0.01"
                  min={0}
                  value={form.cost}
                  onChange={(e) => setForm({ ...form, cost: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Prochaine échéance</span>
                <input
                  type="date"
                  value={form.nextDueDate}
                  onChange={(e) => setForm({ ...form, nextDueDate: e.target.value })}
                />
              </label>
              <label className="field field--full">
                <span>Commentaires</span>
                <textarea
                  rows={3}
                  value={form.comments}
                  onChange={(e) => setForm({ ...form, comments: e.target.value })}
                  maxLength={5000}
                />
              </label>
            </div>
            <button type="submit">{editingId ? 'Enregistrer' : 'Créer'}</button>
            {msg ? <p className="muted">{msg}</p> : null}
          </form>
        </section>
      ) : null}
    </div>
  )
}

