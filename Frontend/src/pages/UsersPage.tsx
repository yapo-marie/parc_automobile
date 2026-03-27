import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  createUser,
  deleteUser,
  fetchRoleNames,
  fetchUsers,
  patchUserStatus,
} from '../api/users'
import { apiFetch } from '../api/client'
import type { CreateUserPayload, UserDto } from '../types/user'

const STATUS_OPTIONS: { value: string; label: string }[] = [
  { value: '', label: 'Tous' },
  { value: 'ACTIVE', label: 'Actif' },
  { value: 'INACTIVE', label: 'Inactif' },
  { value: 'LOCKED', label: 'Verrouillé' },
]

export function UsersPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const [rows, setRows] = useState<UserDto[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [size] = useState(10)
  const [qDraft, setQDraft] = useState('')
  const [appliedQ, setAppliedQ] = useState('')
  const [statusDraft, setStatusDraft] = useState('')
  const [appliedStatus, setAppliedStatus] = useState('')
  const [roleDraft, setRoleDraft] = useState('')
  const [appliedRole, setAppliedRole] = useState('')
  const [roleNames, setRoleNames] = useState<string[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [fn, setFn] = useState('')
  const [ln, setLn] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [position, setPosition] = useState('')
  const [password, setPassword] = useState('Temporaire1!')
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['DRIVER'])
  const [mustChange, setMustChange] = useState(true)
  const [createMsg, setCreateMsg] = useState<string | null>(null)

  const can = useCallback(
    (code: string) => (meAuths ? meAuths.includes(code) : false),
    [meAuths],
  )

  const load = useCallback(async () => {
    setError(null)
    setLoading(true)
    try {
      const data = await fetchUsers({
        page,
        size,
        q: appliedQ || undefined,
        status: appliedStatus || undefined,
        role: appliedRole || undefined,
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur de chargement')
    } finally {
      setLoading(false)
    }
  }, [page, size, appliedQ, appliedStatus, appliedRole])

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
    void (async () => {
      try {
        setRoleNames(await fetchRoleNames())
      } catch {
        setRoleNames([])
      }
    })()
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  function toggleRole(name: string) {
    setSelectedRoles((prev) =>
      prev.includes(name) ? prev.filter((r) => r !== name) : [...prev, name],
    )
  }

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setCreateMsg(null)
    if (selectedRoles.length === 0) {
      setCreateMsg('Choisissez au moins un rôle.')
      return
    }
    const payload: CreateUserPayload = {
      firstname: fn.trim(),
      lastname: ln.trim(),
      email: email.trim(),
      phone: phone.trim() || null,
      position: position.trim() || null,
      password,
      roleNames: selectedRoles,
      mustChangePassword: mustChange,
    }
    try {
      await createUser(payload)
      setCreateMsg('Utilisateur créé.')
      setFn('')
      setLn('')
      setEmail('')
      setPhone('')
      setPosition('')
      setPassword('Temporaire1!')
      setSelectedRoles(['DRIVER'])
      setMustChange(true)
      await load()
    } catch (err) {
      setCreateMsg(err instanceof Error ? err.message : 'Échec de la création')
    }
  }

  async function onDeactivate(u: UserDto) {
    if (!confirm(`Désactiver ${u.email} ?`)) return
    try {
      await patchUserStatus(u.id, 'INACTIVE')
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onActivate(u: UserDto) {
    try {
      await patchUserStatus(u.id, 'ACTIVE')
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onDelete(u: UserDto) {
    if (!confirm(`Supprimer (archiver) ${u.email} ?`)) return
    try {
      await deleteUser(u.id)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  const roleOptions = useMemo(() => [...roleNames].sort(), [roleNames])

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Utilisateurs</h1>
      <p className="dash-lead">
        Liste paginée, filtres et création (API <code>/api/users</code>).
      </p>

      {error ? <p className="alert alert--error">{error}</p> : null}

      <section className="card card--flat users-filters">
        <h2 className="users-h2">Filtres</h2>
        <div className="filters-grid">
          <label className="field field--inline">
            <span>Recherche</span>
            <input
              value={qDraft}
              onChange={(e) => setQDraft(e.target.value)}
              placeholder="Nom, prénom, email"
            />
          </label>
          <label className="field field--inline">
            <span>Statut</span>
            <select
              value={statusDraft}
              onChange={(e) => setStatusDraft(e.target.value)}
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value || 'all'} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label className="field field--inline">
            <span>Rôle</span>
            <select value={roleDraft} onChange={(e) => setRoleDraft(e.target.value)}>
              <option value="">Tous</option>
              {roleOptions.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            className="btn-inline"
            onClick={() => {
              setAppliedQ(qDraft.trim())
              setAppliedStatus(statusDraft)
              setAppliedRole(roleDraft)
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
                    <th>Nom</th>
                    <th>Email</th>
                    <th>Statut</th>
                    <th>Rôles</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((u) => (
                    <tr key={u.id}>
                      <td>
                        {u.firstname} {u.lastname}
                      </td>
                      <td>{u.email}</td>
                      <td>{u.status}</td>
                      <td className="cell-roles">{u.roleNames.join(', ')}</td>
                      <td className="cell-actions">
                        {can('USER_UPDATE') && u.status === 'ACTIVE' ? (
                          <button type="button" className="linkish" onClick={() => void onDeactivate(u)}>
                            Désactiver
                          </button>
                        ) : null}
                        {can('USER_UPDATE') && u.status === 'INACTIVE' ? (
                          <button type="button" className="linkish" onClick={() => void onActivate(u)}>
                            Activer
                          </button>
                        ) : null}
                        {can('USER_DELETE') ? (
                          <button type="button" className="linkish danger" onClick={() => void onDelete(u)}>
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
              <button
                type="button"
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Précédent
              </button>
              <span className="muted">
                Page {page + 1} / {Math.max(1, totalPages)} — {totalElements} utilisateur(s)
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

      {can('USER_CREATE') ? (
        <section className="card">
          <h2 className="users-h2">Nouvel utilisateur</h2>
          <form className="user-form" onSubmit={(e) => void onCreate(e)}>
            <div className="form-grid">
              <label className="field">
                <span>Prénom</span>
                <input value={fn} onChange={(e) => setFn(e.target.value)} required />
              </label>
              <label className="field">
                <span>Nom</span>
                <input value={ln} onChange={(e) => setLn(e.target.value)} required />
              </label>
              <label className="field">
                <span>Email</span>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </label>
              <label className="field">
                <span>Téléphone</span>
                <input value={phone} onChange={(e) => setPhone(e.target.value)} />
              </label>
              <label className="field">
                <span>Poste</span>
                <input value={position} onChange={(e) => setPosition(e.target.value)} />
              </label>
              <label className="field">
                <span>Mot de passe initial</span>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </label>
            </div>
            <fieldset className="roles-fieldset">
              <legend>Rôles</legend>
              <div className="roles-grid">
                {roleNames.map((r) => (
                  <label key={r} className="check">
                    <input
                      type="checkbox"
                      checked={selectedRoles.includes(r)}
                      onChange={() => toggleRole(r)}
                    />
                    {r}
                  </label>
                ))}
              </div>
            </fieldset>
            <label className="check">
              <input type="checkbox" checked={mustChange} onChange={(e) => setMustChange(e.target.checked)} />
              Forcer le changement de mot de passe à la prochaine connexion
            </label>
            <button type="submit">Créer</button>
          </form>
          {createMsg ? <p className="muted">{createMsg}</p> : null}
        </section>
      ) : null}
    </div>
  )
}
