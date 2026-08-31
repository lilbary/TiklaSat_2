import { useEffect, useState } from 'react'
import { AuctionCard } from './AuctionCard.jsx'

// Sabit kategori ID'si tutmak yerine slug kullanıyoruz — veritabanı sıfırlansa
// (gen_random_uuid() ile) ID'ler değişir ama slug'lar migration'da sabit string.
const CATEGORY_SLUGS = ['otomobil', 'oyun-konsollari', 'mobilya', 'apple-telefonlar']

const TILE_STYLES = [
  'from-slate-700 to-slate-500',
  'from-red-600 to-red-400',
  'from-amber-500 to-amber-300',
  'from-emerald-600 to-emerald-400',
]

function WeeklyCategoryHighlights() {
  const [categories, setCategories] = useState([])
  const [activeIndex, setActiveIndex] = useState(0)
  const [items, setItems] = useState([])
  const [fading, setFading] = useState(true)

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

  if (categories.length === 0) return null
  const activeCategory = categories[activeIndex]

  return (
    <section className="mb-14">
      <div className="mb-6 flex flex-wrap items-center gap-6">
        <div className="flex items-center gap-4">
          <div
            className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br font-display text-xl font-bold text-white ${
              TILE_STYLES[activeIndex % TILE_STYLES.length]
            }`}
          >
            {activeCategory.name.charAt(0)}
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-slate-400">
              Bu Haftanın Kategorileri
            </p>
            <h2 className="font-display text-2xl font-semibold text-slate-900">{activeCategory.name}</h2>
          </div>
        </div>

        <div className="ml-auto flex items-center gap-3">
          <div className="flex gap-1.5">
            {categories.map((cat, i) => (
              <button
                key={cat.id}
                type="button"
                onClick={() => goTo(i)}
                aria-label={cat.name}
                className={`h-1 w-10 rounded-full transition-colors ${
                  i === activeIndex ? 'bg-red-600' : 'bg-slate-200 hover:bg-slate-300'
                }`}
              />
            ))}
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
