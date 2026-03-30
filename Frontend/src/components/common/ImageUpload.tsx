import { useRef } from 'react'

type Props = {
  preview: string | null
  onFile: (file: File) => void
}

export function ImageUpload({ preview, onFile }: Props) {
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  function handleFile(file?: File) {
    if (!file) return
    onFile(file)
  }

  return (
    <div>
      <div
        onClick={() => fileInputRef.current?.click()}
        onDrop={(e) => {
          e.preventDefault()
          handleFile(e.dataTransfer.files?.[0])
        }}
        onDragOver={(e) => e.preventDefault()}
        style={{
          border: '2px dashed #ccc',
          borderRadius: 8,
          padding: 20,
          cursor: 'pointer',
          textAlign: 'center',
        }}
      >
        {preview ? (
          <img src={preview} alt="Aperçu" style={{ maxHeight: 180, borderRadius: 8 }} />
        ) : (
          <div>Cliquer ou glisser une photo ici</div>
        )}
      </div>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        hidden
        onChange={(e) => handleFile(e.target.files?.[0])}
      />
    </div>
  )
}

