import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

function CreateAuctionPage() {
  const navigate = useNavigate()

  // SİHİRBAZ ADIMLARI: 1 = Kategori, 2 = İlan Detayları, 3 = Önizleme, 4 = Tebrikler
  const [currentStep, setCurrentStep] = useState(1)

  // --- 1. ADIM: Kategori ---
  const [categoryLists, setCategoryLists] = useState([])
  const [selectedCategoryIds, setSelectedCategoryIds] = useState([])
  const [selectedCategoryNames, setSelectedCategoryNames] = useState([])
  const [isLeafSelected, setIsLeafSelected] = useState(false)
  const [categoryId, setCategoryId] = useState('')

  // --- 2. ADIM: Detaylar ---
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startingPrice, setStartingPrice] = useState('')
  const [reservePrice, setReservePrice] = useState('')
  const [duration, setDuration] = useState('1') 
  const [endingHour, setEndingHour] = useState('20') 

  // Dinamik Özellikler
  const [categoryAttributes, setCategoryAttributes] = useState([])
  const [attributeValues, setAttributeValues] = useState({})

  // Kullanıcının kendi ekleyeceği ekstra özellikler (Anahtar-Değer)
  const [customAttributes, setCustomAttributes] = useState([])

  // Fotoğraflar
  const [selectedFiles, setSelectedFiles] = useState([])
  const [previewUrls, setPreviewUrls] = useState([])

  // Genel Durumlar
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [createdAuctionId, setCreatedAuctionId] = useState(null)

  useEffect(() => {
    fetch('/api/categories')
      .then((res) => res.json())
      .then(data => setCategoryLists([data]))
  }, [])

  // Kategori seçildiğinde dinamik özellikleri çek
  useEffect(() => {
    if (isLeafSelected && categoryId) {
      fetch(`/api/categories/${categoryId}/attributes`)
        .then(res => res.json())
        .then(data => {
          setCategoryAttributes(data)
          // Form alanlarını sıfırla
          const initialValues = {}
          data.forEach(attr => {
            initialValues[attr.name] = ''
          })
          setAttributeValues(initialValues)
        })
        .catch(err => console.error("Özellikler çekilemedi:", err))
    }
  }, [isLeafSelected, categoryId])

  async function handleCategoryChange(levelIndex, selectedId, selectedName) {
    const newSelectedIds = selectedCategoryIds.slice(0, levelIndex)
    const newSelectedNames = selectedCategoryNames.slice(0, levelIndex)
    
    if (selectedId) {
      newSelectedIds.push(selectedId)
      newSelectedNames.push(selectedName)
    }
    
    setSelectedCategoryIds(newSelectedIds)
    setSelectedCategoryNames(newSelectedNames)

    const newCategoryLists = categoryLists.slice(0, levelIndex + 1)
    
    setIsLeafSelected(false)
    setCategoryId('')

    if (!selectedId) {
      setCategoryLists(newCategoryLists)
      return
    }

    try {
      const res = await fetch(`/api/categories/${selectedId}/subcategories`)
      if (res.ok) {
        const subCategories = await res.json()
        if (subCategories.length > 0) {
          setCategoryLists([...newCategoryLists, subCategories])
        } else {
          setCategoryLists(newCategoryLists)
          setIsLeafSelected(true)
          setCategoryId(selectedId)
        }
      }
    } catch (err) {
      console.error('Kategoriler yüklenirken hata oluştu:', err)
    }
  }

  function handleAttributeChange(name, value) {
    setAttributeValues(prev => ({
      ...prev,
      [name]: value
    }))
  }

  function addCustomAttribute() {
    setCustomAttributes(prev => [...prev, { key: '', value: '' }])
  }

  function removeCustomAttribute(index) {
    setCustomAttributes(prev => prev.filter((_, i) => i !== index))
  }

  function handleCustomAttributeChange(index, field, val) {
    setCustomAttributes(prev => {
      const updated = [...prev]
      updated[index] = { ...updated[index], [field]: val }
      return updated
    })
  }

  function handleFileChange(e) {
    const files = Array.from(e.target.files)
    if (files.length > 10) {
      alert('En fazla 10 fotoğraf seçebilirsiniz.')
    }
    const limitedFiles = files.slice(0, 10)
    setSelectedFiles(limitedFiles)
    const urls = limitedFiles.map((file) => URL.createObjectURL(file))
    setPreviewUrls(urls)
  }

  // ADIM İLERLETME
  function goNext() {
    setError('')
    if (currentStep === 1 && !isLeafSelected) {
      setError('Lütfen bir alt kategori seçin.')
      return
    }
    if (currentStep === 2) {
      if (!title || !description || !startingPrice) {
        setError('Lütfen zorunlu alanları (Başlık, Açıklama, Fiyat) doldurun.')
        return
      }
      if (selectedFiles.length === 0) {
        setError('Lütfen en az 1 fotoğraf yükleyin.')
        return
      }
      // Zorunlu alan kontrolü (frontend tarafında)
      for (const attr of categoryAttributes) {
        if (attr.required && !attributeValues[attr.name]) {
          setError(`Lütfen "${attr.label}" alanını doldurun.`)
          return
        }
      }
    }
    setCurrentStep(prev => prev + 1)
  }

  function goBack() {
    setError('')
    setCurrentStep(prev => prev - 1)
  }

  async function handleSubmit() {
    setError('')
    setSubmitting(true)
    const token = localStorage.getItem('token')

    try {
      // Dinamik özellikler + kullanıcının kendi eklediği ekstra özellikleri birleştir
      const finalAttributes = { ...attributeValues }
      customAttributes.forEach(attr => {
        if (attr.key.trim() && attr.value.trim()) {
          finalAttributes[attr.key.trim()] = attr.value.trim()
        }
      })

      // 1. İlanı oluştur
      const listingRes = await fetch('/api/listings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ 
          title, 
          description, 
          categoryId,
          attributes: finalAttributes 
        }),
      })

      if (!listingRes.ok) {
        let msg = `İlan oluşturulamadı (HTTP ${listingRes.status})`
        try { const data = await listingRes.json(); msg = data.message || msg } catch(e) {}
        throw new Error(msg)
      }

      const listing = await listingRes.json()

      // 2. Fotoğrafları yükle
      for (const file of selectedFiles) {
        const formData = new FormData()
        formData.append('file', file)

        const imgRes = await fetch(`/api/listings/${listing.id}/images`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` },
          body: formData,
        })
        
        if (!imgRes.ok) throw new Error(`Fotoğraflar yüklenirken hata oluştu (HTTP ${imgRes.status})`)
      }

      // 3. Açık Artırmayı Başlat
      const endTimeDate = new Date()
      endTimeDate.setDate(endTimeDate.getDate() + parseInt(duration, 10))
      endTimeDate.setHours(parseInt(endingHour, 10), 0, 0, 0)

      const auctionRes = await fetch('/api/auctions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          listingId: listing.id,
          startingPrice: Number(startingPrice),
          endTime: endTimeDate.toISOString(),
          reservePrice: reservePrice ? Number(reservePrice) : null,
        }),
      })

      if (!auctionRes.ok) {
        let msg = `Artırma başlatılamadı (HTTP ${auctionRes.status})`
        try { const data = await auctionRes.json(); msg = data.message || msg } catch(e) {}
        throw new Error(msg)
      }

      const auction = await auctionRes.json()
      setCreatedAuctionId(auction.id)
      setCurrentStep(4) // Tebrikler adımı
      
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
  }

  // --- RENDER YARDIMCILARI ---

  const renderProgressBar = () => (
    <div className="mb-8 flex items-center justify-between border-b pb-4">
      {['Kategori Seçimi', 'İlan Detayları', 'Önizleme', 'Tebrikler'].map((label, index) => {
        const step = index + 1
        const isActive = currentStep === step
        const isCompleted = currentStep > step
        return (
          <div key={step} className="flex flex-col items-center flex-1">
            <div className={`flex h-10 w-10 items-center justify-center rounded-full font-bold ${
              isActive ? 'bg-red-600 text-white' : 
              isCompleted ? 'bg-green-500 text-white' : 'bg-slate-200 text-slate-500'
            }`}>
              {isCompleted ? '✓' : step}
            </div>
            <span className={`mt-2 text-xs font-medium ${isActive ? 'text-slate-900' : 'text-slate-500'}`}>
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )

  const renderStep1 = () => (
    <div className="space-y-6">
      <div className="flex items-center gap-2 text-sm text-slate-600 mb-4 bg-slate-50 p-3 rounded-lg border">
        <span className="font-semibold text-slate-800">Seçiminiz:</span> 
        {selectedCategoryNames.length > 0 ? selectedCategoryNames.join(' > ') : 'Henüz kategori seçilmedi'}
      </div>
      
      <div className="flex gap-4 overflow-x-auto pb-4">
        {categoryLists.map((categories, levelIndex) => (
          <div key={levelIndex} className="min-w-[250px] w-[250px] border rounded-lg bg-white shadow-sm overflow-y-auto max-h-[300px]">
            {categories.map((c) => (
              <button
                key={c.id}
                onClick={() => handleCategoryChange(levelIndex, c.id, c.name)}
                className={`w-full text-left px-4 py-3 text-sm border-b last:border-0 hover:bg-slate-50 transition-colors ${
                  selectedCategoryIds[levelIndex] === c.id ? 'bg-red-50 font-medium text-red-700' : 'text-slate-700'
                }`}
              >
                {c.name}
              </button>
            ))}
          </div>
        ))}

        {isLeafSelected && (
          <div className="flex flex-col items-center justify-center min-w-[250px] w-[250px] border rounded-lg bg-emerald-50 text-emerald-700 p-6 text-center">
            <div className="w-16 h-16 bg-emerald-500 text-white rounded-full flex items-center justify-center text-3xl mb-4">
              ✓
            </div>
            <h3 className="font-bold mb-2">Kategori seçimi tamamlanmıştır.</h3>
            <button 
              onClick={goNext}
              className="mt-4 px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg shadow-sm transition-colors"
            >
              Devam
            </button>
          </div>
        )}
      </div>
    </div>
  )

  const renderStep2 = () => (
    <div className="space-y-8">
      {/* Kategori Bilgisi */}
      <div className="bg-slate-50 p-4 rounded-lg border flex justify-between items-center">
        <div className="text-sm">
          <span className="font-semibold">Kategori: </span>
          <span className="text-slate-600">{selectedCategoryNames.join(' > ')}</span>
        </div>
        <button onClick={() => setCurrentStep(1)} className="text-blue-600 hover:underline text-sm font-medium">Değiştir</button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* SOL KOLON: Temel Bilgiler ve Özellikler */}
        <div className="space-y-5 border-r md:pr-8">
          <h3 className="font-bold text-lg border-b pb-2">İlan Detayları</h3>
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">İlan Başlığı *</label>
            <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none" />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Açıklama *</label>
            <textarea required value={description} onChange={(e) => setDescription(e.target.value)} rows={4}
              className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 outline-none" />
          </div>

          {/* DİNAMİK ÖZELLİKLER RENDER ALANI */}
          {categoryAttributes.length > 0 && (
            <div className="pt-4 border-t mt-6 space-y-4">
              <h4 className="font-semibold text-slate-800">Kategori Özellikleri</h4>
              
              {categoryAttributes.map(attr => (
                <div key={attr.id} className="flex flex-col">
                  <label className="block text-sm font-medium text-slate-700 mb-1">
                    {attr.label} {attr.required && <span className="text-red-500">*</span>}
                  </label>
                  
                  {attr.fieldType === 'SELECT' ? (
                    <select
                      required={attr.required}
                      value={attributeValues[attr.name] || ''}
                      onChange={(e) => handleAttributeChange(attr.name, e.target.value)}
                      className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 outline-none"
                    >
                      <option value="">Seçiniz</option>
                      {attr.options && attr.options.map((opt, i) => (
                        <option key={i} value={opt}>{opt}</option>
                      ))}
                    </select>
                  ) : (
                    <div className="relative">
                      <input
                        type={attr.fieldType === 'NUMBER' ? 'number' : 'text'}
                        required={attr.required}
                        value={attributeValues[attr.name] || ''}
                        onChange={(e) => handleAttributeChange(attr.name, e.target.value)}
                        className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 outline-none"
                      />
                      {attr.unit && (
                        <span className="absolute right-4 top-2.5 text-sm text-slate-400">{attr.unit}</span>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* KULLANICI ÖZEL ÖZELLİK EKLEME ALANI */}
          <div className="pt-4 border-t mt-6 space-y-4">
            <div className="flex justify-between items-center">
              <h4 className="font-semibold text-slate-800">Ekstra Özellik Ekle</h4>
              <button 
                type="button" 
                onClick={addCustomAttribute}
                className="text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1.5 rounded-lg font-medium transition-colors"
              >
                + Yeni Özellik
              </button>
            </div>
            <p className="text-xs text-slate-500">İlanınıza ait ek detayları buradan ekleyebilirsiniz.</p>

            {customAttributes.map((attr, idx) => (
              <div key={idx} className="flex gap-2 items-center">
                <input
                  type="text"
                  placeholder="Özellik (Örn: Renk)"
                  value={attr.key}
                  onChange={(e) => handleCustomAttributeChange(idx, 'key', e.target.value)}
                  className="w-1/2 rounded-lg bg-white border border-slate-300 px-3 py-2 text-sm focus:ring-2 focus:ring-red-500 outline-none"
                />
                <input
                  type="text"
                  placeholder="Değer (Örn: Siyah)"
                  value={attr.value}
                  onChange={(e) => handleCustomAttributeChange(idx, 'value', e.target.value)}
                  className="w-1/2 rounded-lg bg-white border border-slate-300 px-3 py-2 text-sm focus:ring-2 focus:ring-red-500 outline-none"
                />
                <button 
                  type="button" 
                  onClick={() => removeCustomAttribute(idx)}
                  className="text-red-400 hover:text-red-600 hover:bg-red-50 px-2 py-2 rounded-lg font-bold transition-colors"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* SAĞ KOLON: Fiyat, Fotoğraf, Ayarlar */}
        <div className="space-y-6">
          
          <div className="bg-slate-50 p-5 rounded-xl border border-slate-200 space-y-4">
            <h4 className="font-bold text-slate-800">Fiyatlandırma & Süre</h4>
            
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Başlangıç Fiyatı (TL) *</label>
              <input type="number" required value={startingPrice} onChange={(e) => setStartingPrice(e.target.value)}
                className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 outline-none" />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Rezerv Fiyatı (Opsiyonel)</label>
              <input type="number" value={reservePrice} onChange={(e) => setReservePrice(e.target.value)}
                className="w-full rounded-lg bg-white border border-slate-300 px-4 py-2.5 text-sm focus:ring-2 focus:ring-red-500 outline-none" />
            </div>

            <div className="flex gap-3">
              <div className="flex-1">
                <label className="block text-sm font-medium text-slate-700 mb-1">Süre</label>
                <select value={duration} onChange={(e) => setDuration(e.target.value)}
                  className="w-full rounded-lg bg-white border border-slate-300 px-3 py-2.5 text-sm outline-none">
                  <option value="1">1 Gün</option>
                  <option value="3">3 Gün</option>
                  <option value="7">7 Gün</option>
                  <option value="14">14 Gün</option>
                </select>
              </div>
              <div className="flex-1">
                <label className="block text-sm font-medium text-slate-700 mb-1">Bitiş Saati</label>
                <select value={endingHour} onChange={(e) => setEndingHour(e.target.value)}
                  className="w-full rounded-lg bg-white border border-slate-300 px-3 py-2.5 text-sm outline-none">
                  <option value="18">18:00</option>
                  <option value="20">20:00 (Prime)</option>
                  <option value="22">22:00</option>
                </select>
              </div>
            </div>
          </div>

          <div>
            <h4 className="font-bold text-slate-800 mb-3">Fotoğraflar (Max 10) *</h4>
            <input type="file" accept="image/*" multiple onChange={handleFileChange}
              className="w-full text-sm text-slate-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-red-50 file:text-red-700 hover:file:bg-red-100" />
            
            {previewUrls.length > 0 && (
              <div className="mt-4 grid grid-cols-4 gap-2">
                {previewUrls.map((url, i) => (
                  <div key={i} className="relative aspect-square">
                    <img src={url} alt={`Önizleme ${i + 1}`} className="w-full h-full object-cover rounded-lg border" />
                    {i === 0 && <span className="absolute bottom-1 left-1 bg-black/60 text-white text-[10px] px-1.5 py-0.5 rounded">Vitrin</span>}
                  </div>
                ))}
              </div>
            )}
          </div>

        </div>
      </div>

      {error && <p className="text-sm font-medium text-red-600 text-center">{error}</p>}

      <div className="border-t pt-5 flex justify-between">
        <button onClick={goBack} className="px-6 py-2.5 border border-slate-300 rounded-lg text-slate-700 hover:bg-slate-50 font-medium">Geri</button>
        <button onClick={goNext} className="px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium shadow-sm transition-colors">Önizlemeye Geç</button>
      </div>
    </div>
  )

  const renderStep3 = () => (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div className="bg-blue-50 text-blue-800 p-4 rounded-lg flex items-start gap-3">
        <span className="text-xl">ℹ️</span>
        <p className="text-sm">İlanınızı yayınlamadan önce son kez kontrol edin. Hatalı bir bilgi varsa geri dönüp düzeltebilirsiniz.</p>
      </div>

      <div className="bg-white border rounded-xl overflow-hidden shadow-sm">
        {/* Üst Kısım: Foto + Başlık + Fiyat */}
        <div className="flex flex-col md:flex-row gap-6 p-6 border-b">
          <div className="w-full md:w-1/3 aspect-[4/3] bg-slate-100 rounded-lg overflow-hidden border">
            {previewUrls.length > 0 ? (
              <img src={previewUrls[0]} alt="Vitrin" className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-slate-400 text-sm">Fotoğraf Yok</div>
            )}
          </div>
          <div className="w-full md:w-2/3 space-y-3">
            <div className="text-xs font-semibold text-slate-500 tracking-wider uppercase">{selectedCategoryNames.join(' > ')}</div>
            <h2 className="text-2xl font-bold text-slate-900">{title}</h2>
            <div className="text-3xl font-extrabold text-red-600">{Number(startingPrice).toLocaleString('tr-TR')} TL</div>
            
            <div className="flex gap-4 pt-2">
              <div className="bg-slate-50 px-3 py-2 rounded border text-sm">
                <span className="block text-slate-500 text-xs">Açık Artırma Süresi</span>
                <span className="font-semibold text-slate-700">{duration} Gün</span>
              </div>
              <div className="bg-slate-50 px-3 py-2 rounded border text-sm">
                <span className="block text-slate-500 text-xs">Bitiş Saati</span>
                <span className="font-semibold text-slate-700">{endingHour}:00</span>
              </div>
            </div>
          </div>
        </div>

        {/* Alt Kısım: Özellikler + Açıklama */}
        <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-8">
          <div>
            <h3 className="font-bold text-lg mb-4 border-b pb-2">İlan Özellikleri</h3>
            <ul className="space-y-2 text-sm">
              {categoryAttributes.map(attr => (
                <li key={attr.name} className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500">{attr.label}</span>
                  <span className="font-medium text-slate-900">
                    {attributeValues[attr.name] ? `${attributeValues[attr.name]} ${attr.unit || ''}` : '-'}
                  </span>
                </li>
              ))}
              {customAttributes.filter(a => a.key.trim() && a.value.trim()).map((attr, idx) => (
                <li key={`custom-${idx}`} className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500">{attr.key}</span>
                  <span className="font-medium text-slate-900">{attr.value}</span>
                </li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="font-bold text-lg mb-4 border-b pb-2">Açıklama</h3>
            <p className="text-sm text-slate-700 whitespace-pre-line leading-relaxed">{description}</p>
          </div>
        </div>
      </div>

      {error && <p className="text-sm font-medium text-red-600 text-center">{error}</p>}

      <div className="flex justify-between pt-4">
        <button onClick={goBack} disabled={submitting} className="px-6 py-2.5 border border-slate-300 rounded-lg text-slate-700 hover:bg-slate-50 font-medium">Düzenlemeye Dön</button>
        <button onClick={handleSubmit} disabled={submitting} className="px-8 py-3 bg-red-600 hover:bg-red-700 text-white rounded-lg font-bold shadow-md transition-all flex items-center gap-2">
          {submitting ? 'Yayınlanıyor...' : 'İlanı Yayınla'}
        </button>
      </div>
    </div>
  )

  const renderStep4 = () => (
    <div className="max-w-md mx-auto text-center space-y-6 py-10">
      <div className="w-24 h-24 bg-green-100 text-green-600 rounded-full flex items-center justify-center text-5xl mx-auto mb-6">
        🎉
      </div>
      <h2 className="text-3xl font-bold text-slate-900">Tebrikler!</h2>
      <p className="text-slate-600 leading-relaxed">
        İlanınız başarıyla oluşturuldu ve açık artırmaya açıldı. Teklifler gelmeye başladığında size bildirim göndereceğiz.
      </p>
      <div className="pt-6">
        <button 
          onClick={() => navigate(`/artirma/${createdAuctionId}`)}
          className="w-full px-6 py-3 bg-red-600 hover:bg-red-700 text-white font-bold rounded-lg shadow-sm"
        >
          İlanımı Görüntüle
        </button>
      </div>
    </div>
  )

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {renderProgressBar()}
      
      <div className="bg-white rounded-2xl p-6 md:p-8 shadow-sm border border-slate-100 min-h-[500px]">
        {currentStep === 1 && renderStep1()}
        {currentStep === 2 && renderStep2()}
        {currentStep === 3 && renderStep3()}
        {currentStep === 4 && renderStep4()}
      </div>
    </div>
  )
}

export default CreateAuctionPage