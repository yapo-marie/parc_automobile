import { useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPasswordWithToken } from '../api/authPublic'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = useMemo(() => searchParams.get('token')?.trim() ?? '', [searchParams])

  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (!token) {
      setError('Lien incomplet : paramètre token manquant.')
      return
    }
    if (password !== confirm) {
      setError('La confirmation ne correspond pas au nouveau mot de passe.')
      return
    }
    setLoading(true)
    try {
      await resetPasswordWithToken(token, password)
      setDone(true)
      setTimeout(() => navigate('/login', { replace: true }), 2500)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Échec')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app">
      <header className="app__header">
        <h1>FleetPro</h1>
        <p className="app__subtitle">Nouveau mot de passe</p>
      </header>

      {done ? (
        <div className="card">
          <p>Votre mot de passe a été mis à jour. Redirection vers la connexion…</p>
          <p>
            <Link to="/login">Se connecter maintenant</Link>
          </p>
        </div>
      ) : (
        <form className="card" onSubmit={(e) => void onSubmit(e)}>
          <p className="muted" style={{ marginTop: 0 }}>
            Choisissez un mot de passe d’au moins 8 caractères, avec une majuscule, un chiffre et un
            caractère spécial.
          </p>
          {!token ? (
            <p className="alert alert--error">
              Lien invalide. Demandez un nouveau lien depuis la page « Mot de passe oublié ».
            </p>
          ) : null}
          <label className="field">
            <span>Nouveau mot de passe</span>
            <input
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
            />
          </label>
          <label className="field">
            <span>Confirmation</span>
            <input
              type="password"
              autoComplete="new-password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              required
              minLength={8}
            />
          </label>
          <button type="submit" disabled={loading || !token}>
            {loading ? 'Enregistrement…' : 'Enregistrer'}
          </button>
          <p style={{ marginBottom: 0 }}>
            <Link to="/login">Retour à la connexion</Link>
          </p>
        </form>
      )}

      {error ? <p className="alert alert--error">{error}</p> : null}
    </main>
  )
}
