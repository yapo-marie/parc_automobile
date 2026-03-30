type Props = {
  value: number | null | undefined
}

export function FuelLevelBar({ value }: Props) {
  const v = value == null ? 0 : Math.max(0, Math.min(100, value))
  const color = v > 50 ? '#16a34a' : v >= 20 ? '#f59e0b' : '#dc2626'
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{ width: 140, height: 10, background: '#e5e7eb', borderRadius: 999 }}>
        <div
          style={{
            width: `${v}%`,
            height: '100%',
            background: color,
            borderRadius: 999,
          }}
        />
      </div>
      <span style={{ minWidth: 42 }}>{v}%</span>
    </div>
  )
}

