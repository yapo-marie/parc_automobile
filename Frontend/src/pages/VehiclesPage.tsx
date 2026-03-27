import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import {
  createVehicle,
  deleteVehicle,
  fetchVehicles,
  patchVehicleAvailability,
  updateVehicle,
} from '../api/vehicles'
import type {
  FuelType,
  VehicleAvailability,
  VehicleCategory,
  VehicleDto,
  VehicleRecordStatus,
  VehicleWritePayload,
} from '../types/vehicle'

const CATEGORIES: VehicleCategory[] = ['BERLINE', 'SUV', 'UTILITAIRE', 'CAMION', 'MOTO']
const FUELS: FuelType[] = ['ESSENCE', 'DIESEL', 'HYBRIDE', 'ELECTRIQUE', 'GPL']
const AVAILS: VehicleAvailability[] = [
  'AVAILABLE',
  'ASSIGNED',
  'IN_REPAIR',
  'CONTROLE_REQUIS',
  'OUT_OF_SERVICE',
]
const STATUSES: VehicleRecordStatus[] = ['ACTIVE', 'INACTIVE', 'ARCHIVED']

function emptyForm(): VehicleWritePayload {
  return {
    plateNumber: '',
    brand: '',
    model: '',
    year: null,
    color: null,
    category: null,
    fuelType: null,
    mileage: null,
    power: null,
    seats: null,
    acquisitionDate: null,
    acquisitionValue: null,
    insuranceExpiry: null,
    availability: 'AVAILABLE',
    status: 'ACTIVE',
    photoUrl: null,
    notes: null,
  }
}

function dtoToForm(v: VehicleDto): VehicleWritePayload {
  let acq: number | null = null
  if (v.acquisitionValue != null && v.acquisitionValue !== '') {
    const n =
      typeof v.acquisitionValue === 'number'
        ? v.acquisitionValue
        : Number(v.acquisitionValue)
    if (!Number.isNaN(n)) acq = n
  }
  return {
    plateNumber: v.plateNumber,
    brand: v.brand,
    model: v.model,
    year: v.year,
    color: v.color,
    category: v.category,
    fuelType: v.fuelType,
    mileage: v.mileage,
    power: v.power,
    seats: v.seats,
    acquisitionDate: v.acquisitionDate,
    acquisitionValue: acq,
    insuranceExpiry: v.insuranceExpiry,
    availability: v.availability,
    status: v.status,
    photoUrl: v.photoUrl,
    notes: v.notes,
  }
}

export function VehiclesPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const [rows, setRows] = useState<VehicleDto[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [size] = useState(10)
  const [qDraft, setQDraft] = useState('')
  const [appliedQ, setAppliedQ] = useState('')
  const [catDraft, setCatDraft] = useState('')
  const [appliedCat, setAppliedCat] = useState('')
  const [avDraft, setAvDraft] = useState('')
  const [appliedAv, setAppliedAv] = useState('')
  const [stDraft, setStDraft] = useState('')
  const [appliedSt, setAppliedSt] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [form, setForm] = useState<VehicleWritePayload>(() => emptyForm())
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formMsg, setFormMsg] = useState<string | null>(null)

  const can = useCallback(
    (code: string) => (meAuths ? meAuths.includes(code) : false),
    [meAuths],
  )

  const load = useCallback(async () => {
    setError(null)
    setLoading(true)
    try {
      const data = await fetchVehicles({
        page,
        size,
        q: appliedQ || undefined,
        category: appliedCat || undefined,
        availability: appliedAv || undefined,
        status: appliedSt || undefined,
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur de chargement')
    } finally {
      setLoading(false)
    }
  }, [page, size, appliedQ, appliedCat, appliedAv, appliedSt])

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
    void load()
  }, [load])

  const formTitle = useMemo(
    () => (editingId ? 'Modifier le véhicule' : 'Nouveau véhicule'),
    [editingId],
  )

  function startEdit(v: VehicleDto) {
    setForm(dtoToForm(v))
    setEditingId(v.id)
    setFormMsg(null)
  }

  function cancelEdit() {
    setForm(emptyForm())
    setEditingId(null)
    setFormMsg(null)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setFormMsg(null)
    try {
      if (editingId) {
        await updateVehicle(editingId, form)
        setFormMsg('Véhicule mis à jour.')
      } else {
        await createVehicle(form)
        setFormMsg('Véhicule créé.')
        setForm(emptyForm())
      }
      await load()
    } catch (err) {
      setFormMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onAvChange(v: VehicleDto, availability: string) {
    try {
      await patchVehicleAvailability(v.id, availability)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onDelete(v: VehicleDto) {
    if (!confirm(`Archiver ${v.plateNumber} ?`)) return
    try {
      await deleteVehicle(v.id)
      if (editingId === v.id) cancelEdit()
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Véhicules</h1>
      <p className="dash-lead">Parc : liste, filtres, création et mise à jour (CDC module 2).</p>

      {error ? <p className="alert alert--error">{error}</p> : null}

      <section className="card card--flat users-filters">
        <h2 className="users-h2">Filtres</h2>
        <div className="filters-grid filters-grid--wrap">
          <label className="field field--inline">
            <span>Recherche</span>
            <input
              value={qDraft}
              onChange={(e) => setQDraft(e.target.value)}
              placeholder="Plaque, marque, modèle"
            />
          </label>
          <label className="field field--inline">
            <span>Catégorie</span>
            <select value={catDraft} onChange={(e) => setCatDraft(e.target.value)}>
              <option value="">Toutes</option>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <label className="field field--inline">
            <span>Disponibilité</span>
            <select value={avDraft} onChange={(e) => setAvDraft(e.target.value)}>
              <option value="">Toutes</option>
              {AVAILS.map((a) => (
                <option key={a} value={a}>
                  {a}
                </option>
              ))}
            </select>
          </label>
          <label className="field field--inline">
            <span>Statut enregistrement</span>
            <select value={stDraft} onChange={(e) => setStDraft(e.target.value)}>
              <option value="">Tous</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            className="btn-inline"
            onClick={() => {
              setAppliedQ(qDraft.trim())
              setAppliedCat(catDraft)
              setAppliedAv(avDraft)
              setAppliedSt(stDraft)
              setPage(0)
            }}
          >
            Appliquer
          </button>
        </div>
      </section>

      <section className="card card--flat">
        <h2 className="users-h2">Liste</h2>
        {loading ? (
          <p className="muted">Chargement…</p>
        ) : (
          <>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Plaque</th>
                    <th>Marque / modèle</th>
                    <th>Cat.</th>
                    <th>Dispo.</th>
                    <th>Statut</th>
                    <th>Km</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((v) => (
                    <tr key={v.id}>
                      <td>
                        <Link to={`/dashboard/vehicles/${v.id}`}>
                          <code>{v.plateNumber}</code>
                        </Link>
                      </td>
                      <td>
                        {v.brand} {v.model}
                      </td>
                      <td>{v.category ?? '—'}</td>
                      <td>
                        {can('VEHICLE_UPDATE') ? (
                          <select
                            className="select-inline"
                            value={v.availability}
                            onChange={(e) => void onAvChange(v, e.target.value)}
                          >
                            {AVAILS.map((a) => (
                              <option key={a} value={a}>
                                {a}
                              </option>
                            ))}
                          </select>
                        ) : (
                          v.availability
                        )}
                      </td>
                      <td>{v.status}</td>
                      <td>{v.mileage}</td>
                      <td className="cell-actions">
                        {can('VEHICLE_UPDATE') ? (
                          <button type="button" className="linkish" onClick={() => startEdit(v)}>
                            Modifier
                          </button>
                        ) : null}
                        {can('VEHICLE_DELETE') ? (
                          <button type="button" className="linkish danger" onClick={() => void onDelete(v)}>
                            Archiver
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="pager">
              <button type="button" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                Précédent
              </button>
              <span className="muted">
                Page {page + 1} / {Math.max(1, totalPages)} — {totalElements} véhicule(s)
              </span>
              <button
                type="button"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Suivant
              </button>
            </div>
          </>
        )}
      </section>

      {can('VEHICLE_CREATE') || (can('VEHICLE_UPDATE') && editingId) ? (
        <section className="card">
          <h2 className="users-h2">{formTitle}</h2>
          {editingId ? (
            <p className="muted">
              Édition : <code>{editingId}</code>{' '}
              <button type="button" className="linkish" onClick={cancelEdit}>
                Annuler
              </button>
            </p>
          ) : null}
          <form className="user-form vehicle-form" onSubmit={(e) => void onSubmit(e)}>
            <div className="form-grid form-grid--vehicle">
              <label className="field">
                <span>Immatriculation *</span>
                <input
                  required
                  value={form.plateNumber}
                  onChange={(e) => setForm({ ...form, plateNumber: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Marque *</span>
                <input
                  required
                  value={form.brand}
                  onChange={(e) => setForm({ ...form, brand: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Modèle *</span>
                <input
                  required
                  value={form.model}
                  onChange={(e) => setForm({ ...form, model: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Année</span>
                <input
                  type="number"
                  value={form.year ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      year: e.target.value === '' ? null : Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="field">
                <span>Couleur</span>
                <input
                  value={form.color ?? ''}
                  onChange={(e) => setForm({ ...form, color: e.target.value || null })}
                />
              </label>
              <label className="field">
                <span>Catégorie</span>
                <select
                  value={form.category ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      category: (e.target.value || null) as VehicleCategory | null,
                    })
                  }
                >
                  <option value="">—</option>
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Carburant</span>
                <select
                  value={form.fuelType ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      fuelType: (e.target.value || null) as FuelType | null,
                    })
                  }
                >
                  <option value="">—</option>
                  {FUELS.map((f) => (
                    <option key={f} value={f}>
                      {f}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Kilométrage</span>
                <input
                  type="number"
                  min={0}
                  value={form.mileage ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      mileage: e.target.value === '' ? null : Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="field">
                <span>Puissance (ch)</span>
                <input
                  type="number"
                  value={form.power ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      power: e.target.value === '' ? null : Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="field">
                <span>Places</span>
                <input
                  type="number"
                  value={form.seats ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      seats: e.target.value === '' ? null : Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="field">
                <span>Date acquisition</span>
                <input
                  type="date"
                  value={form.acquisitionDate ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, acquisitionDate: e.target.value || null })
                  }
                />
              </label>
              <label className="field">
                <span>Valeur acquisition</span>
                <input
                  type="number"
                  step="0.01"
                  min={0}
                  value={form.acquisitionValue ?? ''}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      acquisitionValue: e.target.value === '' ? null : Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="field">
                <span>Fin assurance</span>
                <input
                  type="date"
                  value={form.insuranceExpiry ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, insuranceExpiry: e.target.value || null })
                  }
                />
              </label>
              <label className="field">
                <span>Disponibilité</span>
                <select
                  value={form.availability ?? 'AVAILABLE'}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      availability: e.target.value as VehicleAvailability,
                    })
                  }
                >
                  {AVAILS.map((a) => (
                    <option key={a} value={a}>
                      {a}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Statut fiche</span>
                <select
                  value={form.status ?? 'ACTIVE'}
                  onChange={(e) =>
                    setForm({ ...form, status: e.target.value as VehicleRecordStatus })
                  }
                >
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>URL photo</span>
                <input
                  value={form.photoUrl ?? ''}
                  onChange={(e) => setForm({ ...form, photoUrl: e.target.value || null })}
                />
              </label>
              <label className="field field--full">
                <span>Notes</span>
                <textarea
                  rows={2}
                  value={form.notes ?? ''}
                  onChange={(e) => setForm({ ...form, notes: e.target.value || null })}
                />
              </label>
            </div>
            {editingId && can('VEHICLE_UPDATE') ? (
              <button type="submit">Enregistrer</button>
            ) : null}
            {!editingId && can('VEHICLE_CREATE') ? <button type="submit">Créer</button> : null}
          </form>
          {formMsg ? <p className="muted">{formMsg}</p> : null}
        </section>
      ) : null}
    </div>
  )
}
