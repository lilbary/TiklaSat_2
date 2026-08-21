import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

function CreateAuctionPage() {
  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [startingPrice, setStartingPrice] = useState('')
  const [endTime, setEndTime] = useState('')
  const [selectedFiles, setSelectedFiles] = useState([])
  const [previewUrls, setPreviewUrls] = useState([])
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    fetch('/api/categories')
      .then((res) => res.json())
      .then(setCategories)
  }, [])

  // Fotoğraf seçildiğinde önizleme oluştur
  function handleFileChange(e) {
    const files = Array.from(e.target.files)
    setSelectedFiles(files)

    // Önizleme URL'lerini oluştur
    const urls = files.map((file) => URL.createObjectURL(file))
    setPreviewUrls(urls)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)

    const token = localStorage.getItem('token')

    // 1. Adım: Önce ilanı oluştur
    const listingRes = await fetch('/api/listings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({ title, description, categoryId }),
    })

    if (!listingRes.ok) {
      const data = await listingRes.json()
      setError(data.message || 'İlan oluşturulamadı')
      setSubmitting(false)
      return
    }

    const listing = await listingRes.json()

    // 2. Adım: Seçilen fotoğrafları yükle
    for (const file of selectedFiles) {
      const formData = new FormData()
      formData.append('file', file)

      await fetch(`/api/listings/${listing.id}/images`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      })
    }

    // 3. Adım: İlanı hemen artırmaya çıkar
    const auctionRes = await fetch('/api/auctions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        listingId: listing.id,
        startingPrice: Number(startingPrice),
        endTime: new Date(endTime).toISOString(),
      }),
    })

    if (!auctionRes.ok) {
      const data = await auctionRes.json()
      setError(data.message || 'Artırma başlatılamadı')
      setSubmitting(false)
      return
    }

    const auction = await auctionRes.json()
    navigate(`/artirma/${auction.id}`)
  }

  return (
    <div className="mx-auto max-w-xl px-6 py-10">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">Açık Artırma Oluştur</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <input
          type="text"
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Ürün başlığı"
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <textarea
          required
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Açıklama"
          rows={4}
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <select
          required
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Kategori seç</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        {/* FOTOĞRAF YÜKLEME ALANI */}
        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Fotoğraflar
          </label>
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm file:mr-3 file:rounded-lg file:border-0 file:bg-blue-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-blue-600 hover:file:bg-blue-100"
          />
          {/* Fotoğraf Önizleme */}
          {previewUrls.length > 0 && (
            <div className="mt-3 grid grid-cols-4 gap-2">
              {previewUrls.map((url, i) => (
                <img
                  key={i}
                  src={url}
                  alt={`Önizleme ${i + 1}`}
                  className="aspect-square rounded-lg object-cover ring-1 ring-slate-200"
                />
              ))}
            </div>
          )}
        </div>

        <input
          type="number"
          required
          value={startingPrice}
          onChange={(e) => setStartingPrice(e.target.value)}
          placeholder="Başlangıç fiyatı (TL)"
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <input
          type="datetime-local"
          required
          value={endTime}
          onChange={(e) => setEndTime(e.target.value)}
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-blue-600 py-3 text-sm font-semibold text-white transition-colors hover:bg-blue-700 disabled:opacity-50"
        >
          {submitting ? 'Oluşturuluyor...' : 'Artırmayı Başlat'}
        </button>
      </form>
    </div>
  )
}

export default CreateAuctionPage