import { Link } from 'react-router-dom'

type Props = {
  title: string
  description: string
}

export function PlaceholderPage({ title, description }: Props) {
  return (
    <div className="dash-page">
      <h1>{title}</h1>
      <p className="dash-lead">{description}</p>
      <p>
        <Link to="/dashboard">← Retour au tableau de bord</Link>
      </p>
    </div>
  )
}
