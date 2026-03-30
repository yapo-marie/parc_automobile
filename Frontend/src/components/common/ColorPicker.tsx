type Props = {
  value: string
  onChange: (value: string) => void
}

export function ColorPicker({ value, onChange }: Props) {
  const color = /^#([A-Fa-f0-9]{6})$/.test(value) ? value : '#2e75b6'
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <input type="color" value={color} onChange={(e) => onChange(e.target.value)} />
      <span>{color}</span>
      <div
        style={{
          width: 20,
          height: 20,
          borderRadius: '50%',
          background: color,
          border: '1px solid #ddd',
        }}
      />
    </div>
  )
}

