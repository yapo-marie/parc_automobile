import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { changeMyPassword, fetchMeProfile, updateMeProfile } from '../api/users'
import { useAuthStore } from '../store/authStore'

export function ProfilePage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [email, setEmail] = useState('')
  const [firstname, setFirstname] = useState('')
  const [lastname, setLastname] = useState('')
  const [phone, setPhone] = useState('')
  const [position, setPosition] = useState('')
  const [roleNames, setRoleNames] = useState<string[]>([])
  const [mustChangePassword, setMustChangePassword] = useState(false)

  const [profileMsg, setProfileMsg] = useState<string | null>(null)
  const [pwdMsg, setPwdMsg] = useState<string | null>(null)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const load = useCallback(async () => {
    setError(null)
    setLoading(true)
    try {
      const u = await fetchMeProfile()
      setEmail(u.email)
      setFirstname(u.firstname)
      setLastname(u.lastname)
      setPhone(u.phone ?? '')
      setPosition(u.position ?? '')
      setRoleNames(u.roleNames)
      setMustChangePassword(u.mustChangePassword)
      useAuthStore.getState().setMustChangePassword(u.mustChangePassword)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Chargement impossible')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function onProfileSubmit(e: FormEvent) {
    e.preventDefault()
    setProfileMsg(null)
    try {
      await updateMeProfile({
        firstname: firstname.trim(),
        lastname: lastname.trim(),
        phone: phone.trim() || null,
        position: position.trim() || null,
      })
      setProfileMsg('Profil enregistré.')
      await load()
    } catch (err) {
      setProfileMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  async function onPasswordSubmit(e: FormEvent) {
    e.preventDefault()
    setPwdMsg(null)
    if (newPassword !== confirmPassword) {
      setPwdMsg('La confirmation ne correspond pas au nouveau mot de passe.')
      return
    }
    try {
      await changeMyPassword(currentPassword, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setPwdMsg('Mot de passe mis à jour.')
      await load()
    } catch (err) {
      setPwdMsg(err instanceof Error ? err.message : 'Échec')
    }
  }

  return (
    <div className="dash-page users-page">
      <p className="dash-back">
        <Link to="/dashboard">← Tableau de bord</Link>
      </p>
      <h1>Mon profil</h1>
      <p className="dash-lead">Coordonnées personnelles et mot de passe.</p>

      {error ? <p className="alert alert--error">{error}</p> : null}
      {mustChangePassword ? (
        <p className="alert alert--error">
          Vous devez changer votre mot de passe avant de poursuivre normalement.
        </p>
      ) : null}

      {loading ? (
        <p className="muted">Chargement…</p>
      ) : (
        <>
          <section className="card card--flat">
            <h2 className="users-h2">Informations</h2>
            <form className="user-form" onSubmit={(e) => void onProfileSubmit(e)}>
              <div className="form-grid form-grid--vehicle">
                <label className="field">
                  <span>Email</span>
                  <input value={email} readOnly disabled className="input-readonly" />
                </label>
                <label className="field">
                  <span>Prénom *</span>
                  <input
                    required
                    value={firstname}
                    onChange={(e) => setFirstname(e.target.value)}
                    maxLength={100}
                  />
                </label>
                <label className="field">
                  <span>Nom *</span>
                  <input
                    required
                    value={lastname}
                    onChange={(e) => setLastname(e.target.value)}
                    maxLength={100}
                  />
                </label>
                <label className="field">
                  <span>Téléphone</span>
                  <input value={phone} onChange={(e) => setPhone(e.target.value)} maxLength={20} />
                </label>
                <label className="field field--full">
                  <span>Fonction</span>
                  <input
                    value={position}
                    onChange={(e) => setPosition(e.target.value)}
                    maxLength={100}
                  />
                </label>
              </div>
              <button type="submit">Enregistrer le profil</button>
              {profileMsg ? <p className="muted">{profileMsg}</p> : null}
            </form>
            <p className="muted" style={{ marginTop: '1rem' }}>
              Rôles : {roleNames.length ? roleNames.join(', ') : '—'}
            </p>
          </section>

          <section className="card">
            <h2 className="users-h2">Changer le mot de passe</h2>
            <p className="muted">
              Au moins 8 caractères, une majuscule, un chiffre et un caractère spécial.
            </p>
            <form className="user-form" onSubmit={(e) => void onPasswordSubmit(e)}>
              <div className="form-grid form-grid--vehicle">
                <label className="field field--full">
                  <span>Mot de passe actuel *</span>
                  <input
                    type="password"
                    autoComplete="current-password"
                    required
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                  />
                </label>
                <label className="field">
                  <span>Nouveau *</span>
                  <input
                    type="password"
                    autoComplete="new-password"
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                  />
                </label>
                <label className="field">
                  <span>Confirmation *</span>
                  <input
                    type="password"
                    autoComplete="new-password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                  />
                </label>
              </div>
              <button type="submit">Mettre à jour le mot de passe</button>
              {pwdMsg ? <p className="muted">{pwdMsg}</p> : null}
            </form>
          </section>
        </>
      )}
    </div>
  )
}
