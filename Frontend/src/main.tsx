import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// sockjs-client référence un identifiant `global` (Node) ; en navigateur Vite, on le mappe.
;(window as any).global = globalThis

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
