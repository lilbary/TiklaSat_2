import { useEffect, useState } from 'react'
import { AuctionCard } from './AuctionCard.jsx'

const TILE_STYLES = [
  'from-red-200 to-red-50 text-red-400',
  'from-amber-200 to-amber-50 text-amber-500',
]

const SLIDES = [
  {
    title: 'En Çok İstenenler',
    subtitle: "TıklaSat'ta en çok favorilenen açık artırmalar.",
    fetchItems: (signal) =>
      fetch('/api/auctions/most-favorited?limit=4', { signal }).then((res) => res.json()),
  },
  {
    title: 'Yeni Eklenenler',
    subtitle: 'Az önce açık artırmaya çıkan ürünler.',
    fetchItems: (signal) =>
      fetch('/api/auctions', { signal })
        .then((res) => res.json())
        .then((all) =>
          all
            .filter((a) => a.status === 'ACTIVE')
            .sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
            .slice(0, 4)
        ),
  },
  {
    title: 'Bitmek Üzere',
    subtitle: 'Kaçırmadan teklif verebileceğin son fırsatlar.',
    fetchItems: (signal) =>
      fetch('/api/auctions', { signal })
        .then((res) => res.json())
        .then((all) =>
          all
            .filter((a) => a.status === 'ACTIVE' && new Date(a.endTime) > new Date())
            .sort((a, b) => new Date(a.endTime) - new Date(b.endTime))
            .slice(0, 4)
        ),
  },
]

function MosaicTile({ auction, styleIndex, className }) {
  return (
    <div
      className={`flex items-center justify-center rounded-2xl bg-gradient-to-br font-display text-5xl font-semibold ${TILE_STYLES[styleIndex % TILE_STYLES.length]} ${className}`}
    >
      {auction.listingTitle.charAt(0)}
    </div>
  )
}

function MostWantedCarousel() {
  const [activeSlide, setActiveSlide] = useState(0)
  const [items, setItems] = useState([])
  const [fading, setFading] = useState(true)

  useEffect(() => {
    const controller = new AbortController()
    setFading(true)
    SLIDES[activeSlide]
      .fetchItems(controller.signal)
      .then((data) => {
        setItems(data)
        requestAnimationFrame(() => setFading(false))
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setFading(false)
      })
    return () => {
      controller.abort()
    }
  }, [activeSlide])

  function goTo(index) {
    setActiveSlide((index + SLIDES.length) % SLIDES.length)
  }

  const slide = SLIDES[activeSlide]

  if (items.length === 0 && !fading) return null

  return (
    <section className="mb-14 rounded-3xl bg-slate-50 px-8 py-12">
      <div className="grid grid-cols-1 gap-10 lg:grid-cols-2 lg:items-center">
        {/* SOL: başlık + açıklama + gezinme */}
        <div>
          <h2 className="font-display text-5xl font-semibold tracking-tight text-slate-900">
            {slide.title}
          </h2>
          <p className="mt-4 max-w-sm text-slate-600">{slide.subtitle}</p>

          <div className="mt-8 flex items-center gap-4">
            <div className="flex gap-2">
              {SLIDES.map((_, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => goTo(i)}
                  aria-label={`${i + 1}. bölüm`}
                  className={`h-1.5 rounded-full transition-all ${
                    i === activeSlide ? 'w-8 bg-red-600' : 'w-4 bg-slate-300 hover:bg-slate-400'
                  }`}
                />
              ))}
            </div>
            <button
              type="button"
              onClick={() => goTo(activeSlide + 1)}
              aria-label="Sonraki bölüm"
              className="flex h-9 w-9 items-center justify-center rounded-full border border-slate-300 text-slate-600 transition-colors hover:border-red-600 hover:text-red-600"
            >
              <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 6l6 6-6 6" />
              </svg>
            </button>
          </div>
        </div>

        {/* SAĞ: mozaik kolaj */}
        <div
          className={`grid h-72 grid-cols-3 grid-rows-2 gap-3 transition-opacity duration-300 ${
            fading ? 'opacity-0' : 'opacity-100'
          }`}
        >
          {items[0] && <MosaicTile auction={items[0]} styleIndex={0} className="col-span-2 row-span-2" />}
          {items[1] && <MosaicTile auction={items[1]} styleIndex={1} className="col-span-1 row-span-1" />}
          <div className="flex flex-col items-center justify-center rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
            <span className="font-display text-xl font-bold text-slate-900">
              Tıkla<span className="text-red-600">Sat</span>
            </span>
            <span className="mt-1 text-[10px] font-semibold uppercase tracking-widest text-slate-400">
              {slide.title}
            </span>
          </div>
        </div>
      </div>

      {/* ALT: 4 kart, kaymadan — bölüm değişince tamamen değişiyor */}
      <div
        className={`mt-10 grid grid-cols-2 gap-6 sm:grid-cols-4 transition-opacity duration-300 ${
          fading ? 'opacity-0' : 'opacity-100'
        }`}
      >
        {items.map((auction) => (
          <AuctionCard key={auction.id} auction={auction} />
        ))}
      </div>
    </section>
  )
}

export default MostWantedCarousel
