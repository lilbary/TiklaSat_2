import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

function CreateAuctionPage() {
  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [startingPrice, setStartingPrice] = useState('')
  const [duration, setDuration] = useState('1') // Varsayılan 1 gün
  const [endingHour, setEndingHour] = useState('20') // Varsayılan 20:00
  const [selectedFiles, setSelectedFiles] = useState([])
  const [previewUrls, setPreviewUrls] = useState([])
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    fetch('/api/categories')
      .then((res) => res.json())
      .then(setCategories)
  }, [])

  // Fotoğraf seçildiğinde önizleme oluştur ve limitleri kontrol et
  function handleFileChange(e) {
    const files = Array.from(e.target.files)
    
    if (files.length > 10) {
      alert('En fazla 10 fotoğraf seçebilirsiniz. Yalnızca ilk 10 fotoğraf eklenecek.')
    }
    
    // Maksimum 10 fotoğraf al
    const limitedFiles = files.slice(0, 10)
    setSelectedFiles(limitedFiles)

    // Önizleme URL'lerini oluştur
    const urls = limitedFiles.map((file) => URL.createObjectURL(file))
    setPreviewUrls(urls)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    // Minimum 1 fotoğraf kontrolü
    if (selectedFiles.length === 0) {
      setError('Lütfen ilana en az 1 adet fotoğraf ekleyin.')
      return
    }

    setSubmitting(true)
    const token = localStorage.getItem('token')

    try {
      // 1. Adım: Önce ilanı oluştur
      const listingRes = await fetch('/api/listings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ title, description, categoryId }),
      })

      if (!listingRes.ok) {
        const data = await listingRes.json()
        throw new Error(data.message || 'İlan oluşturulamadı')
      }

      const listing = await listingRes.json()

      // 2. Adım: Seçilen fotoğrafları yükle
      for (const file of selectedFiles) {
        const formData = new FormData()
        formData.append('file', file)

        const imgRes = await fetch(`/api/listings/${listing.id}/images`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
          body: formData,
        })
        
        if (!imgRes.ok) {
          let errMsg = 'Fotoğraflar yüklenirken bir hata oluştu'
          try {
            const errData = await imgRes.json()
            errMsg = errData.message || errMsg
          } catch(e) {}
          throw new Error(errMsg)
        }
      }

      // Seçilen gün ve prime time saatine göre Bitiş Zamanını (endTime) hesapla
      const endTimeDate = new Date()
      endTimeDate.setDate(endTimeDate.getDate() + parseInt(duration, 10))
      endTimeDate.setHours(parseInt(endingHour, 10), 0, 0, 0) // Kullanıcının seçtiği tam saat (Örn: 20:00:00)

      // 3. Adım: İlanı hemen artırmaya çıkar
      const auctionRes = await fetch('/api/auctions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          listingId: listing.id,
          startingPrice: Number(startingPrice),
          endTime: endTimeDate.toISOString(),
        }),
      })

      if (!auctionRes.ok) {
        const data = await auctionRes.json()
        throw new Error(data.message || 'Artırma başlatılamadı')
      }

      const auction = await auctionRes.json()
      navigate(`/artirma/${auction.id}`)
      
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
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
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
        />

        <textarea
          required
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Açıklama"
          rows={4}
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
        />

        <select
          required
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
        >
          <option value="">Kategori seç</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        {/* FOTOĞRAF YÜKLEME ALANI */}
        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Fotoğraflar (En az 1, en fazla 10)
          </label>
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm file:mr-3 file:rounded-lg file:border-0 file:bg-red-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-red-600 hover:file:bg-red-100"
          />
          {/* Fotoğraf Önizleme */}
          {previewUrls.length > 0 && (
            <div className="mt-3 grid grid-cols-4 gap-2">
              {previewUrls.map((url, i) => (
                <div key={i} className="relative aspect-square">
                  <img
                    src={url}
                    alt={`Önizleme ${i + 1}`}
                    className="h-full w-full rounded-lg object-cover ring-1 ring-slate-200"
                  />
                  <div className="absolute top-1 right-1 rounded bg-black/50 px-1.5 py-0.5 text-xs text-white">
                    {i + 1}
                  </div>
                </div>
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
          className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
        />

        {/* SÜRE VE SAAT SEÇİMİ */}
        <div className="flex gap-4">
          <div className="flex-1">
            <label className="mb-2 block text-sm font-medium text-slate-700">
              Açık Artırma Süresi
            </label>
            <select
              required
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
              className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="1">1 Gün</option>
              <option value="3">3 Gün</option>
              <option value="7">1 Hafta (7 Gün)</option>
              <option value="15">15 Gün</option>
              <option value="30">1 Ay (30 Gün)</option>
            </select>
          </div>

          <div className="flex-1">
            <label className="mb-2 block text-sm font-medium text-slate-700">
              Bitiş Saati (Prime Time)
            </label>
            <select
              required
              value={endingHour}
              onChange={(e) => setEndingHour(e.target.value)}
              className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="18">18:00 (Akşam Üstü)</option>
              <option value="19">19:00</option>
              <option value="20">20:00 (Prime Time)</option>
              <option value="21">21:00 (Prime Time)</option>
              <option value="22">22:00 (Gece)</option>
              <option value="23">23:00 (Gece)</option>
            </select>
          </div>
        </div>

        {error && <p className="text-sm font-medium text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-red-600 py-3 text-sm font-semibold text-white transition-colors hover:bg-red-700 disabled:opacity-50"
        >
          {submitting ? 'Oluşturuluyor...' : 'Artırmayı Başlat'}
        </button>
      </form>
    </div>
  )
}

export default CreateAuctionPage