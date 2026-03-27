import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/client'
import {
  createRole,
  deleteRole,
  fetchPermissionOptions,
  fetchRolesDetail,
  setRolePermissions,
  updateRole,
} from '../api/roles'
import type { PermissionOptionDto, RoleDetailDto } from '../types/role'

const SEEDED_NAMES = new Set([
  'SUPER_ADMIN',
  'ADMIN',
  'FLEET_MANAGER',
  'DRIVER',
  'VIEWER',
])

function permLabel(code: string): string {
  return code.replaceAll('_', ' ').toLowerCase()
}

export function RolesPage() {
  const [meAuths, setMeAuths] = useState<string[] | null>(null)
  const canManage = useMemo(() => meAuths?.includes('ROLE_MANAGE') ?? false, [meAuths])

  const [roles, setRoles] = useState<RoleDetailDto[]>([])
  const [permOptions, setPermOptions] = useState<PermissionOptionDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [msg, setMsg] = useState<string | null>(null)
  const [modalErr, setModalErr] = useState<string | null>(null)

  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [editing, setEditing] = useState<RoleDetailDto | null>(null)
  const [formName, setFormName] = useState('')
  const [formDesc, setFormDesc] = useState('')
  const [formPerms, setFormPerms] = useState<Set<string>>(() => new Set())

  const load = useCallback(async () => {
    setError(null)
    setLoading(true)
    try {
      const [r, p] = await Promise.all([fetchRolesDetail(), fetchPermissionOptions()])
      setRoles(r)
      setPermOptions(p)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Chargement impossible')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void (async () => {
      try {
        const res = await apiFetch('/api/me')
        if (res.ok) {
          const me = (await res.json()) as { authorities: string[] }
          setMeAuths(me.authorities)
        } else {
          setMeAuths([])
        }
      } catch {
        setMeAuths([])
      }
    })()
  }, [])

  useEffect(() => {
    if (!canManage) {
      setLoading(false)
      return
    }
    void load()
  }, [canManage, load])

  function openCreate() {
    setMsg(null)
    setModalErr(null)
    setEditing(null)
    setFormName('')
    setFormDesc('')
    setFormPerms(new Set())
    setModal('create')
  }

  function openEdit(r: RoleDetailDto) {
    setMsg(null)
    setModalErr(null)
    setEditing(r)
    setFormName(r.name)
    setFormDesc(r.description ?? '')
    setFormPerms(new Set(r.permissionCodes))
    setModal('edit')
  }

  function closeModal() {
    setModal(null)
    setEditing(null)
  }

  function togglePerm(code: string) {
    setFormPerms((prev) => {
      const next = new Set(prev)
      if (next.has(code)) next.delete(code)
      else next.add(code)
      return next
    })
  }

  async function onSubmitModal(e: FormEvent) {
    e.preventDefault()
    setModalErr(null)
    try {
      if (modal === 'create') {
        const name = formName.trim()
        if (!name) {
          setModalErr('Indiquez un nom de rôle.')
          return
        }
        await createRole({
          name,
          description: formDesc.trim() || null,
          permissionCodes: [...formPerms],
        })
        setMsg('Rôle créé.')
        closeModal()
        await load()
        return
      }
      if (modal === 'edit' && editing) {
        const isSeeded = SEEDED_NAMES.has(editing.name)
        const normalizedName = formName.trim().toUpperCase().replace(/\s+/g, '_')
        if (!isSeeded && normalizedName && normalizedName !== editing.name) {
          await updateRole(editing.id, {
            name: normalizedName,
            description: formDesc.trim() || null,
          })
        } else {
          await updateRole(editing.id, {
            description: formDesc.trim() || null,
          })
        }
        await setRolePermissions(editing.id, { permissionCodes: [...formPerms] })
        setMsg('Rôle mis à jour.')
        closeModal()
        await load()
      }
    } catch (err) {
      setModalErr(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onDelete(r: RoleDetailDto) {
    if (SEEDED_NAMES.has(r.name)) return
    if (r.assignedUserCount > 0) {
      setError('Ce rôle est encore attribué à des utilisateurs.')
      return
    }
    if (!confirm(`Supprimer le rôle « ${r.name} » ?`)) return
    setError(null)
    try {
      await deleteRole(r.id)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Suppression impossible')
    }
  }

  if (!canManage) {
    return (
      <div className="dash-page roles-page">
        <p className="dash-back">
          <Link to="/dashboard">← Tableau de bord</Link>
        </p>
        <h1>Rôles & permissions</h1>
        <p className="alert alert--error">Vous n’avez pas la permission ROLE_MANAGE.</p>
      </div>
    )
  }

  return (
    <div className="dash-page roles-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Rôles & permissions</h1>
      <p className="dash-lead">
        Création de rôles personnalisés, édition des descriptions et matrice des permissions.
        Les rôles prédéfinis du système ne peuvent pas être supprimés ni renommés.
      </p>

      {error ? <p className="alert alert--error">{error}</p> : null}
      {msg ? <p className="alert alert--ok">{msg}</p> : null}

      <div className="roles-toolbar">
        <button type="button" className="btn-primary" onClick={openCreate}>
          Nouveau rôle
        </button>
      </div>

      {loading ? (
        <p className="muted">Chargement…</p>
      ) : (
        <div className="roles-grid">
          {roles.map((r) => (
            <article key={r.id} className="role-card card card--flat">
              <header className="role-card__head">
                <h2 className="role-card__title">{r.name}</h2>
                <p className="role-card__meta muted">
                  {r.assignedUserCount} utilisateur(s) · {r.permissionCodes.length} permission(s)
                </p>
              </header>
              {r.description ? <p className="role-card__desc">{r.description}</p> : null}
              <ul className="role-card__perms">
                {r.permissionCodes.slice(0, 8).map((c) => (
                  <li key={c} className="role-card__perm-chip">
                    {permLabel(c)}
                  </li>
                ))}
                {r.permissionCodes.length > 8 ? (
                  <li className="role-card__perm-chip role-card__perm-chip--more">
                    +{r.permissionCodes.length - 8}
                  </li>
                ) : null}
              </ul>
              <div className="role-card__actions">
                <button type="button" className="btn-secondary" onClick={() => openEdit(r)}>
                  Modifier
                </button>
                {!SEEDED_NAMES.has(r.name) ? (
                  <button
                    type="button"
                    className="btn-danger"
                    onClick={() => void onDelete(r)}
                    disabled={r.assignedUserCount > 0}
                  >
                    Supprimer
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      )}

      {modal ? (
        <div className="reject-modal" role="presentation" onClick={closeModal}>
          <div
            className="reject-modal__panel role-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="role-modal-title"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 id="role-modal-title">{modal === 'create' ? 'Nouveau rôle' : 'Modifier le rôle'}</h2>
            <form onSubmit={(e) => void onSubmitModal(e)} className="role-modal__form">
              <label className="field">
                <span>Nom du rôle</span>
                <input
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  disabled={modal === 'edit' && editing ? SEEDED_NAMES.has(editing.name) : false}
                  required={modal === 'create'}
                  maxLength={50}
                  placeholder="EXEMPLE_ROLE"
                  className={
                    modal === 'edit' && editing && SEEDED_NAMES.has(editing.name)
                      ? 'input-readonly'
                      : undefined
                  }
                />
              </label>
              <label className="field">
                <span>Description</span>
                <textarea
                  value={formDesc}
                  onChange={(e) => setFormDesc(e.target.value)}
                  rows={3}
                  maxLength={2000}
                />
              </label>

              <fieldset className="role-matrix">
                <legend>Permissions</legend>
                <div className="role-matrix__grid">
                  {permOptions.map((p) => (
                    <label key={p.code} className="role-matrix__cell">
                      <input
                        type="checkbox"
                        checked={formPerms.has(p.code)}
                        onChange={() => togglePerm(p.code)}
                      />
                      <span className="role-matrix__label">
                        <strong>{p.code}</strong>
                        {p.description ? (
                          <span className="muted role-matrix__hint">{p.description}</span>
                        ) : null}
                      </span>
                    </label>
                  ))}
                </div>
              </fieldset>

              {modalErr ? <p className="alert alert--error">{modalErr}</p> : null}

              <div className="role-modal__buttons">
                <button type="button" className="btn-secondary" onClick={closeModal}>
                  Annuler
                </button>
                <button type="submit" className="btn-primary">
                  Enregistrer
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}
