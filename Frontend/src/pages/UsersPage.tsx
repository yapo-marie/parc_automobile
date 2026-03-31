import { useCallback, useEffect, useState, type FormEvent } from 'react'
import {
  createUser,
  fetchRoleNames,
  fetchUsers,
  patchUserStatus,
} from '../api/users'
import { apiFetch } from '../api/client'
import type { CreateUserPayload, UserDto } from '../types/user'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import { Shield, UsersRound, KeyRound, Plus } from 'lucide-react'

export function UsersPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const [rows, setRows] = useState<UserDto[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [size] = useState(10)
  const [roleNames, setRoleNames] = useState<string[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [fn, setFn] = useState('')
  const [ln, setLn] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('Temporaire1!')
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['DRIVER'])
  const [mustChange, setMustChange] = useState(true)
  const [createMsg, setCreateMsg] = useState<string | null>(null)
  const [confirmAction, setConfirmAction] = useState<{
    title: string
    message: string
    variant: 'danger' | 'warning'
    run: () => Promise<void>
  } | null>(null)

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
        size
      })
      setRows(data.content)
      setTotalPages(data.totalPages)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur de chargement')
    } finally {
      setLoading(false)
    }
  }, [page, size])

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
      setPassword('Temporaire1!')
      setSelectedRoles(['DRIVER'])
      setMustChange(true)
      await load()
    } catch (err) {
      setCreateMsg(err instanceof Error ? err.message : 'Échec de la création')
    }
  }

  async function onDeactivate(u: UserDto) {
    setConfirmAction({
      title: 'Désactiver cet utilisateur ?',
      message: `L'utilisateur ${u.firstname} ${u.lastname} ne pourra plus se connecter.`,
      variant: 'warning',
      run: async () => {
        await patchUserStatus(u.id, 'INACTIVE')
        await load()
      },
    })
  }

  async function onActivate(u: UserDto) {
    try {
      await patchUserStatus(u.id, 'ACTIVE')
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }



  return (
    <div className="dash-page users-page">
      <div className="page-header">
        <div className="page-title-group">
          <Shield size={24} color="#10b981" />
          <h1>Administration</h1>
        </div>
        <p className="dash-lead">
          Gestion des utilisateurs et des rôles (RBAC)
        </p>
      </div>

      {error ? <p className="alert alert--error">{error}</p> : null}

      <div className="admin-grid">
        <div className="admin-column">
          <section className="card card--flat">
            <div className="card-header-flex">
              <div className="page-title-group" style={{ marginBottom: 0 }}>
                <UsersRound size={18} color="#64748b" />
                <h2 className="users-h2" style={{ margin: 0 }}>Utilisateurs</h2>
              </div>
              {can('USER_CREATE') && (
                <button type="button" className="btn-primary" onClick={() => {}}>
                  <Plus size={16} />
                  Ajouter
                </button>
              )}
            </div>

            {loading ? (
              <p className="muted">Chargement…</p>
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Utilisateur</th>
                      <th>Rôle</th>
                      <th>Actif</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((u) => (
                      <tr key={u.id}>
                        <td>
                          <div style={{ fontWeight: 600 }}>{u.firstname} {u.lastname?.charAt(0)}.</div>
                          <div className="muted">{u.email}</div>
                        </td>
                        <td>
                          {u.roleNames.map(r => (
                            <span key={r} className={`badge ${r.toLowerCase() === 'admin' ? 'badge--danger' : r.toLowerCase() === 'manager' ? 'badge--info' : 'badge--neutral'}`} style={{ marginRight: '4px' }}>
                              {r.toLowerCase()}
                            </span>
                          ))}
                        </td>
                        <td>
                          <input 
                            type="checkbox" 
                            className="toggle-switch" 
                            checked={u.status === 'ACTIVE'}
                            onChange={() => u.status === 'ACTIVE' ? onDeactivate(u) : onActivate(u)}
                            disabled={!can('USER_UPDATE')}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <div className="pager">
              <button
                type="button"
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Précédent
              </button>
              <button
                type="button"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Suivant
              </button>
            </div>
          </section>
        </div>

        <div className="admin-column">
          <section className="card card--flat">
            <div className="card-header-flex">
              <div className="page-title-group" style={{ marginBottom: 0 }}>
                <KeyRound size={18} color="#64748b" />
                <h2 className="users-h2" style={{ margin: 0 }}>Rôles & Accès</h2>
              </div>
              <button type="button" className="btn-primary">
                <Plus size={16} />
                Nouveau rôle
              </button>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' }}>
                <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem' }}>Admin</h3>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <span className="badge badge--neutral">/</span>
                  <span className="badge badge--neutral">/vehicles</span>
                  <span className="badge badge--neutral">/tracking</span>
                  <span className="badge badge--neutral">/alerts</span>
                  <span className="badge badge--neutral">/maintenance</span>
                  <span className="badge badge--neutral">/admin</span>
                </div>
              </div>
              <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' }}>
                <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem' }}>Manager</h3>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <span className="badge badge--neutral">/</span>
                  <span className="badge badge--neutral">/vehicles</span>
                  <span className="badge badge--neutral">/tracking</span>
                  <span className="badge badge--neutral">/alerts</span>
                  <span className="badge badge--neutral">/maintenance</span>
                </div>
              </div>
              <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' }}>
                <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem' }}>Operator</h3>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <span className="badge badge--neutral">/</span>
                  <span className="badge badge--neutral">/vehicles</span>
                  <span className="badge badge--neutral">/tracking</span>
                  <span className="badge badge--neutral">/alerts</span>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>


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
              Changer de mot de passe à la prochaine connexion
            </label>
            <button type="submit">Créer</button>
          </form>
          {createMsg ? <p className="muted">{createMsg}</p> : null}
        </section>
      ) : null}
      <ConfirmDialog
        open={!!confirmAction}
        title={confirmAction?.title ?? ''}
        message={confirmAction?.message ?? ''}
        confirmLabel={confirmAction?.variant === 'danger' ? 'Archiver définitivement' : 'Désactiver'}
        confirmVariant={confirmAction?.variant ?? 'warning'}
        onCancel={() => setConfirmAction(null)}
        onConfirm={() => {
          const action = confirmAction
          setConfirmAction(null)
          if (!action) return
          void action.run().catch((e) => setError(e instanceof Error ? e.message : 'Erreur'))
        }}
      />
    </div>
  )
}
