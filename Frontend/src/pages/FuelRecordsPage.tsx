import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import { Line } from 'react-chartjs-2'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import { createFuelRecord, fetchFuelRecords, fetchFuelStats } from '../api/fuelRecords'
import { fetchVehicles } from '../api/vehicles'
import type { FuelRecordDto, CreateFuelRecordPayload, FuelRecordStatsDto } from '../types/fuelRecord'
import type { VehicleDto } from '../types/vehicle'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

type FormState = {
  vehicleId: string
  fillDate: string
  liters: string
  unitPrice: string
  totalCost: string
  mileage: string
  station: string
}

function emptyForm(vehicleId: string): FormState {
  return {
    vehicleId,
    fillDate: new Date().toISOString().slice(0, 10),
    liters: '',
    unitPrice: '',
    totalCost: '',
    mileage: '',
    station: '',
  }
}

function toPayload(form: FormState): CreateFuelRecordPayload {
  return {
    vehicleId: form.vehicleId,
    fillDate: form.fillDate,
    liters: Number(form.liters),
    unitPrice: form.unitPrice ? Number(form.unitPrice) : null,
    totalCost: form.totalCost ? Number(form.totalCost) : null,
    mileage: form.mileage ? Number(form.mileage) : null,
    station: form.station ? form.station.trim() : null,
  }
}

function num(v: number | string | null | undefined): number | null {
  if (v == null || v === '') return null
  const n = typeof v === 'number' ? v : Number(v)
  return Number.isFinite(n) ? n : null
}

function labelYearMonth(ym: string): string {
  const parts = ym.split('-').map(Number)
  if (parts.length < 2 || Number.isNaN(parts[0]) || Number.isNaN(parts[1])) return ym
  const d = new Date(parts[0], parts[1] - 1, 1)
  return d.toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' })
}

function fmtMoney(n: number | null): string {
  if (n == null) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(n)
}

function downloadFuelCsv(rows: FuelRecordDto[], plateLabel: string) {
  const headers = [
    'date',
    'immatriculation',
    'litres',
    'prix_unitaire',
    'cout_total',
    'kilometrage',
    'station',
  ]
  const lines = [headers.join(';')]
  for (const r of rows) {
    const station = (r.station ?? '').replaceAll(';', ',').replaceAll('\n', ' ')
    lines.push(
      [
        r.fillDate,
        r.vehiclePlate,
        String(r.liters),
        r.unitPrice != null ? String(r.unitPrice) : '',
        r.totalCost != null ? String(r.totalCost) : '',
        r.mileage != null ? String(r.mileage) : '',
        station,
      ].join(';'),
    )
  }
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const safe = plateLabel.replace(/[^\w-]+/g, '_').slice(0, 40)
  a.download = `carburant_${safe || 'export'}_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export function FuelRecordsPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canManage = useMemo(() => meAuths?.includes('FLEET_MANAGE') ?? false, [meAuths])

  const [vehicles, setVehicles] = useState<VehicleDto[]>([])
  const [rows, setRows] = useState<FuelRecordDto[]>([])
  const [page, setPage] = useState(0)
  const [size] = useState(15)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [filterVehicleId, setFilterVehicleId] = useState('')
  const [filterFromDate, setFilterFromDate] = useState('')
  const [filterToDate, setFilterToDate] = useState('')

  const [stats, setStats] = useState<FuelRecordStatsDto | null>(null)
  const [statsLoading, setStatsLoading] = useState(false)
  const [statsError, setStatsError] = useState<string | null>(null)

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
      const data = await fetchFuelRecords({
        page,
        size,
        vehicleId: filterVehicleId || undefined,
        fromDate: filterFromDate || undefined,
        toDate: filterToDate || undefined,
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setLoading(false)
    }
  }, [page, size, filterVehicleId, filterFromDate, filterToDate])

  const loadStats = useCallback(async () => {
    if (!filterVehicleId) {
      setStats(null)
      setStatsError(null)
      return
    }
    setStatsLoading(true)
    setStatsError(null)
    try {
      setStats(await fetchFuelStats(filterVehicleId))
    } catch (e) {
      setStats(null)
      setStatsError(e instanceof Error ? e.message : 'Stats indisponibles')
    } finally {
      setStatsLoading(false)
    }
  }, [filterVehicleId])

  useEffect(() => {
    void loadMe()
    void loadVehicles()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  useEffect(() => {
    void loadStats()
  }, [loadStats])

  const selectedVehicleLabel = useMemo(() => {
    const v = vehicles.find((x) => x.id === filterVehicleId)
    return v ? `${v.plateNumber}` : ''
  }, [vehicles, filterVehicleId])

  const chartData = useMemo(() => {
    if (!stats?.lastSixMonths?.length) return null
    return {
      labels: stats.lastSixMonths.map((p) => labelYearMonth(p.yearMonth)),
      datasets: [
        {
          label: 'Consommation moy. (L/100 km)',
          data: stats.lastSixMonths.map((p) => {
            const n = num(p.avgLitersPer100km)
            return n != null ? n : NaN
          }),
          borderColor: '#0284c7',
          backgroundColor: 'rgba(2,132,199,0.12)',
          fill: true,
          tension: 0.25,
          spanGaps: false,
        },
      ],
    }
  }, [stats])

  const chartOptions = useMemo(
    () => ({
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true, position: 'top' as const },
        title: { display: false },
      },
      scales: {
        y: {
          beginAtZero: true,
          title: { display: true, text: 'L / 100 km' },
        },
      },
    }),
    [],
  )

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setMsg(null)
    if (!canManage) return
    if (!form.vehicleId || !form.fillDate || !form.liters) {
      setMsg('Véhicule, date et litres sont requis.')
      return
    }
    try {
      await createFuelRecord(toPayload(form))
      setMsg('Relevé carburant enregistré.')
      setForm((f) => emptyForm(f.vehicleId))
      await load()
      if (filterVehicleId && form.vehicleId === filterVehicleId) void loadStats()
    } catch (err) {
      setMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onExportCsv() {
    if (!filterVehicleId) return
    setError(null)
    try {
      const data = await fetchFuelRecords({
        page: 0,
        size: 2000,
        vehicleId: filterVehicleId,
        fromDate: filterFromDate || undefined,
        toDate: filterToDate || undefined,
      })
      downloadFuelCsv(data.content, selectedVehicleLabel || filterVehicleId)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Export impossible')
    }
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Carburant</h1>
      <p className="dash-lead">Relevés, coûts et consommation (L/100 km) par véhicule.</p>

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
            <span>Du</span>
            <input type="date" value={filterFromDate} onChange={(e) => setFilterFromDate(e.target.value)} />
          </label>
          <label className="field field--inline">
            <span>Au</span>
            <input type="date" value={filterToDate} onChange={(e) => setFilterToDate(e.target.value)} />
          </label>
          <button type="button" className="btn-inline" onClick={() => void load()}>
            Appliquer
          </button>
          {filterVehicleId ? (
            <button type="button" className="btn-secondary btn-inline" onClick={() => void onExportCsv()}>
              Exporter CSV
            </button>
          ) : null}
        </div>
        <p className="tiny muted" style={{ marginTop: '0.65rem' }}>
          Sélectionnez un véhicule pour afficher les KPI, la courbe sur 6 mois et un export filtré.
        </p>
      </section>

      {filterVehicleId ? (
        <>
          {statsError ? <p className="alert alert--error">{statsError}</p> : null}
          {statsLoading ? <p className="muted">Chargement des statistiques…</p> : null}
          {!statsLoading && stats ? (
            <>
              <section className="grid-kpi">
                <article className="kpi">
                  <span className="kpi__label">Conso. moyenne</span>
                  <span className="kpi__value kpi__value--sm">
                    {num(stats.averageLitersPer100km) != null
                      ? `${num(stats.averageLitersPer100km)} L/100 km`
                      : '—'}
                  </span>
                  <span className="tiny muted">Entre pleins avec km renseigné</span>
                </article>
                <article className="kpi">
                  <span className="kpi__label">Litres (mois en cours)</span>
                  <span className="kpi__value kpi__value--sm">
                    {num(stats.currentMonthLiters) != null
                      ? `${num(stats.currentMonthLiters)} L`
                      : '—'}
                  </span>
                </article>
                <article className="kpi">
                  <span className="kpi__label">Coût (mois en cours)</span>
                  <span className="kpi__value kpi__value--sm">
                    {fmtMoney(num(stats.currentMonthCost))}
                  </span>
                </article>
                <article className="kpi">
                  <span className="kpi__label">Pleins (mois / total)</span>
                  <span className="kpi__value kpi__value--sm">
                    {stats.currentMonthFillCount} / {stats.totalFillCount}
                  </span>
                </article>
              </section>

              <section className="card card--flat fuel-chart-card">
                <h2 className="users-h2">Consommation sur 6 mois</h2>
                {chartData ? (
                  <div className="fuel-chart-wrap">
                    <Line data={chartData} options={chartOptions} />
                  </div>
                ) : (
                  <p className="muted">Pas assez de données pour le graphique.</p>
                )}
              </section>
            </>
          ) : null}
        </>
      ) : null}

      <section className="card card--flat">
        <h2 className="users-h2">Liste</h2>
        {loading ? <p className="muted">Chargement…</p> : null}
        {!loading ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Véhicule</th>
                  <th>Litres</th>
                  <th>P.U.</th>
                  <th>Total</th>
                  <th>Km</th>
                  <th>Station</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>{r.fillDate}</td>
                    <td>
                      <Link to={`/dashboard/vehicles/${r.vehicleId}`}>
                        <code>{r.vehiclePlate}</code>
                      </Link>
                      <div className="muted tiny">{r.vehicleLabel}</div>
                    </td>
                    <td>{r.liters}</td>
                    <td>{r.unitPrice ?? '—'}</td>
                    <td>{r.totalCost ?? '—'}</td>
                    <td>{r.mileage ?? '—'}</td>
                    <td>{r.station ?? '—'}</td>
                  </tr>
                ))}
                {!rows.length ? (
                  <tr>
                    <td colSpan={7} className="muted">
                      Aucun relevé.
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
          <h2 className="users-h2">Ajouter un relevé</h2>
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
              <label className="field">
                <span>Date *</span>
                <input type="date" required value={form.fillDate} onChange={(e) => setForm({ ...form, fillDate: e.target.value })} />
              </label>
              <label className="field">
                <span>Litres *</span>
                <input type="number" step="0.01" min={0.01} required value={form.liters} onChange={(e) => setForm({ ...form, liters: e.target.value })} />
              </label>
              <label className="field">
                <span>Prix unitaire</span>
                <input type="number" step="0.01" min={0} value={form.unitPrice} onChange={(e) => setForm({ ...form, unitPrice: e.target.value })} />
              </label>
              <label className="field">
                <span>Total</span>
                <input type="number" step="0.01" min={0} value={form.totalCost} onChange={(e) => setForm({ ...form, totalCost: e.target.value })} />
              </label>
              <label className="field">
                <span>Kilométrage</span>
                <input type="number" min={0} value={form.mileage} onChange={(e) => setForm({ ...form, mileage: e.target.value })} />
              </label>
              <label className="field field--full">
                <span>Station</span>
                <input value={form.station} maxLength={200} onChange={(e) => setForm({ ...form, station: e.target.value })} />
              </label>
            </div>
            <p className="tiny muted">
              Indiquez le kilométrage à chaque plein pour calculer la consommation L/100 km.
            </p>
            <button type="submit">Enregistrer</button>
            {msg ? <p className="muted">{msg}</p> : null}
          </form>
        </section>
      ) : null}
    </div>
  )
}
