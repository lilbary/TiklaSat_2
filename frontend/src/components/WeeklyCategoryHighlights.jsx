import { useEffect, useRef, useState } from 'react'
import { AuctionCard } from './AuctionCard.jsx'

// Sabit kategori ID'si tutmak yerine slug kullanıyoruz — veritabanı sıfırlansa
// (gen_random_uuid() ile) ID'ler değişir ama slug'lar migration'da sabit string.
const CATEGORY_SLUGS = ['otomobil', 'oyun-konsollari', 'mobilya', 'apple-telefonlar']

// Kategori fotoğrafları kullanıcı içeriği değil, sabit bir tasarım varlığı —
// bu yüzden backend/veritabanı yerine frontend'in kendi public/ klasöründe duruyor.
const CATEGORY_IMAGES = {
  otomobil: '/categories/otomobil.jpg',
  'oyun-konsollari': '/categories/oyun-konsollari.jpg',
  mobilya: '/categories/mobilya.webp',
  'apple-telefonlar': '/categories/apple-telefonlar.webp',
}

function WeeklyCategoryHighlights() {
  const [categories, setCategories] = useState([])
  const [activeIndex, setActiveIndex] = useState(0)
  const [items, setItems] = useState([])
  const [fading, setFading] = useState(true)

  // fillActive: aktif çizginin o an dolmaya BAŞLAMIŞ olup olmadığı. false→true geçişini
  // requestAnimationFrame ile bir sonraki kareye erteliyoruz; aynı karede hem "scaleX(0)"
  // hem "10sn'de scaleX(1)" yazsaydık tarayıcı bunu ANİMASYONSUZ uygulardı.
  const [fillActive, setFillActive] = useState(false)

  // Bölüm ekranda değilken sayaç da dolum çubuğu da dursun.
  // Bağımlılık boş [] DEĞİL: bu bileşen categories gelene kadar null döndürüyor, yani
  // mount anında zoneRef.current null olurdu ve observer hiç bağlanmazdı.
  const zoneRef = useRef(null)
  const [onScreen, setOnScreen] = useState(true)

  useEffect(() => {
    const el = zoneRef.current
    if (!el) return
    const observer = new IntersectionObserver(([entry]) => setOnScreen(entry.isIntersecting))
    observer.observe(el)
    return () => observer.disconnect()
  }, [categories.length])

  useEffect(() => {
    setFillActive(false) // önce sıfıra dön (bu kare boyunca)
    if (!onScreen) return // ekran dışında: çubuk sıfırda bekler
    const raf = requestAnimationFrame(() => setFillActive(true)) // sonraki karede dolmaya başla
    return () => cancelAnimationFrame(raf)
  }, [activeIndex, onScreen])

  // Kategorileri bir kere çek, slug listesindeki sıraya göre diz
  useEffect(() => {
    const params = new URLSearchParams()
    CATEGORY_SLUGS.forEach((slug) => params.append('slugs', slug))

    fetch(`/api/categories/by-slugs?${params}`)
      .then((res) => res.json())
      .then((data) => {
        const ordered = CATEGORY_SLUGS.map((slug) => data.find((c) => c.slug === slug)).filter(Boolean)
        setCategories(ordered)
      })
  }, [])

  // Aktif kategori değişince o kategorinin en çok favorilenen 4 ürününü çek
  useEffect(() => {
    if (categories.length === 0) return
    const controller = new AbortController()
    setFading(true)
    fetch(`/api/auctions/most-favorited?limit=4&categoryId=${categories[activeIndex].id}`, {
      signal: controller.signal,
    })
      .then((res) => res.json())
      .then((data) => {
        setItems(data)
        requestAnimationFrame(() => setFading(false))
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setFading(false)
      })
    return () => controller.abort()
  }, [categories, activeIndex])

  function goTo(index) {
    setActiveIndex((index + categories.length) % categories.length)
  }

  // Her 10 saniyede bir, sanki "sonraki kategori" okuna basılmış gibi otomatik ilerlet
  // (MostWantedCarousel'deki ile birebir aynı mantık — activeIndex değişince sıfırlanır).
  useEffect(() => {
    if (categories.length === 0 || !onScreen) return // ekran dışında: sayaç hiç kurulmaz
    const timer = setInterval(() => {
      setActiveIndex((prev) => (prev + 1) % categories.length)
    }, 10000)
    return () => clearInterval(timer)
  }, [activeIndex, categories.length, onScreen])

  if (categories.length === 0) return null
  const activeCategory = categories[activeIndex]

  return (
    <section ref={zoneRef} className="mb-14">
      <div className="mb-6 flex flex-wrap items-center gap-6">
        <div className="flex items-center gap-4">
          <img
            src={CATEGORY_IMAGES[activeCategory.slug]}
            alt={activeCategory.name}
            className="h-14 w-14 shrink-0 rounded-xl object-cover"
          />
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-slate-400">
              Bu Haftanın Kategorileri
            </p>
            <h2 className="font-display text-2xl font-semibold text-slate-900">{activeCategory.name}</h2>
          </div>
        </div>

        <div className="ml-auto flex items-center gap-3">
          <div className="flex gap-1.5">
            {categories.map((cat, i) => {
              const isActive = i === activeIndex
              // Geçilmiş çizgi = index'i aktiften KÜÇÜK olan. Bu formül başa dönünce
              // (activeIndex=0) kendiliğinden sıfırlanıyor — 0'dan küçük index yok.
              const isFilled = isActive ? fillActive : i < activeIndex
              return (
                <button
                  key={cat.id}
                  type="button"
                  onClick={() => goTo(i)}
                  aria-label={cat.name}
                  className="h-1 w-10 overflow-hidden rounded-full bg-slate-200 transition-colors hover:bg-slate-300"
                >
                  {/* Dolum çubuğu — width yerine transform: scaleX. width animasyonu her karede
                      yeniden yerleşim (layout) hesaplattırıp takılmaya yol açıyordu; scaleX
                      GPU compositor katmanında işlendiği için akıcı kalıyor. origin-left,
                      dolumun soldan başlamasını sağlıyor. */}
                  <span
                    className="block h-full origin-left rounded-full bg-red-600"
                    style={{
                      transform: isFilled ? 'scaleX(1)' : 'scaleX(0)',
                      transition: isActive && fillActive ? 'transform 10000ms linear' : 'none',
                    }}
                  />
                </button>
              )
            })}
          </div>
          <button
            type="button"
            onClick={() => goTo(activeIndex + 1)}
            aria-label="Sonraki kategori"
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-slate-300 text-slate-600 transition-colors hover:border-red-600 hover:text-red-600"
          >
            <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 6l6 6-6 6" />
            </svg>
          </button>
        </div>
      </div>

      <div
        className={`grid grid-cols-2 gap-6 sm:grid-cols-4 transition-opacity duration-300 ${
          fading ? 'opacity-0' : 'opacity-100'
        }`}
      >
        {items.length === 0 && !fading ? (
          <p className="col-span-full text-sm text-slate-500">Bu kategoride henüz açık artırma yok.</p>
        ) : (
          items.map((auction) => <AuctionCard key={auction.id} auction={auction} fullWidth />)
        )}
      </div>
    </section>
  )
}

export default WeeklyCategoryHighlights
