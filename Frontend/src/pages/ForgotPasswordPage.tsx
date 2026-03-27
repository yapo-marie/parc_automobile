import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { requestPasswordReset } from '../api/authPublic'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [done, setDone] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await requestPasswordReset(email)
      setDone(true)
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
        <p className="app__subtitle">Mot de passe oublié</p>
      </header>

      {done ? (
        <div className="card">
          <p>
            Si un compte actif correspond à cette adresse de messagerie, un message avec un lien de
            réinitialisation (valable 24 h) vient d’être envoyé.
          </p>
          <p className="muted">Pensez à vérifier les courriers indésirables.</p>
          <p>
            <Link to="/login">Retour à la connexion</Link>
          </p>
        </div>
      ) : (
        <form className="card" onSubmit={(e) => void onSubmit(e)}>
          <p className="muted" style={{ marginTop: 0 }}>
            Indiquez votre adresse email. Nous vous enverrons un lien pour définir un nouveau mot de
            passe.
          </p>
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? 'Envoi…' : 'Envoyer le lien'}
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
