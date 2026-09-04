import { useEffect, useRef, useState } from 'react'
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
  const hasImage = auction.imageUrls && auction.imageUrls.length > 0

  if (hasImage) {
    return (
      <img
        src={auction.imageUrls[0]}
        alt={auction.listingTitle}
        className={`h-full w-full rounded-2xl object-cover ${className}`}
      />
    )
  }

  // Fotoğrafı olmayan ilan: AuctionCard'daki ile aynı mantık — baş harf + gradyan
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

  // fillActive: aktif çizginin o an dolmaya BAŞLAMIŞ olup olmadığı (false→true geçişi,
  // requestAnimationFrame ile bir sonraki kareye ertelenip transition'ı GERÇEKTEN tetikliyor —
  // aynı anda hem "0%" hem "10sn'de %100'e geç" yazsak tarayıcı bunu ANİMASYONSUZ uygulardı).
  const [fillActive, setFillActive] = useState(false)

  // Bölüm ekranda değilken sayaç da dolum çubuğu da dursun. IntersectionObserver
  // tarayıcının kendi API'si — scroll dinleyip her karede pozisyon hesaplamaya gerek yok.
  const zoneRef = useRef(null)
  const [onScreen, setOnScreen] = useState(true)

  useEffect(() => {
    const el = zoneRef.current
    if (!el) return
    const observer = new IntersectionObserver(([entry]) => setOnScreen(entry.isIntersecting))
    observer.observe(el)
    return () => observer.disconnect()
  }, [])

  useEffect(() => {
    setFillActive(false) // önce sıfıra dön (bu kare boyunca)
    if (!onScreen) return // ekran dışında: çubuk sıfırda bekler
    const raf = requestAnimationFrame(() => setFillActive(true)) // sonraki karede dolmaya başla
    return () => cancelAnimationFrame(raf)
  }, [activeSlide, onScreen])

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

  // Her 10 saniyede bir, sanki "sonraki bölüm" okuna basılmış gibi otomatik ilerlet.
  // activeSlide'a bağlı: hem otomatik hem elle geçişten sonra sayaç sıfırdan başlıyor,
  // böylece bir geçişin hemen ardından ikinci bir geçiş üst üste binmiyor.
  useEffect(() => {
    if (!onScreen) return // ekran dışında: sayaç hiç kurulmaz
    const timer = setInterval(() => {
      setActiveSlide((prev) => (prev + 1) % SLIDES.length)
    }, 10000)
    return () => clearInterval(timer)
  }, [activeSlide, onScreen])

  const slide = SLIDES[activeSlide]

  if (items.length === 0 && !fading) return null

  return (
    // Tam viewport genişliğine taşan (full-bleed) dış kabuk: mx-[calc(50%-50vw)] + w-screen,
    // HomePage'in kendi kapsayıcısının (max-w-7xl + px-6) genişliğinden bağımsız olarak
    // gri zemini ekranın iki ucuna kadar götürüyor. -mt-10, HomePage kapsayıcısının üst
    // dolgusunu (py-10) iptal edip Navbar'a tam yapışmasını sağlıyor.
    <section className="relative -mt-10 mb-14 w-screen mx-[calc(50%-50vw)]">
      {/* Gri zemin — sadece üst bölüm (başlık + kolaj) kadar yüksek, altına eklenen
          boşluk (pb-[88px]) sayesinde aşağıdaki kartların YARISINA kadar sızıyor.
          Kartlar bu zeminin üstüne -mt-[88px] ile "biniyor" — h-44 (176px) olan kart
          görselinin tam yarısı (88px) gri zeminin içinde, geri kalanı beyaz sayfada kalıyor. */}
      <div ref={zoneRef} className="bg-[#eaeff5] pt-16 pb-[120px]">
        <div className="mx-auto max-w-7xl px-6">
        <div className="grid grid-cols-1 gap-10 lg:grid-cols-2 lg:gap-24 lg:items-center">
        {/* SOL: başlık + açıklama + gezinme */}
        <div>
          <h2 className="font-display text-5xl font-semibold tracking-tight text-slate-900">
            {slide.title}
          </h2>
          <p className="mt-4 max-w-sm text-slate-600">{slide.subtitle}</p>

          <div className="mt-8 flex items-center gap-4">
            <div className="flex gap-2">
              {SLIDES.map((_, i) => {
                const isActive = i === activeSlide
                // Geçilmiş çizgi = index'i aktiften KÜÇÜK olan. Bu formül başa dönünce
                // (activeSlide=0) kendiliğinden sıfırlanıyor — 0'dan küçük index yok.
                const isFilled = isActive ? fillActive : i < activeSlide
                return (
                  <button
                    key={i}
                    type="button"
                    onClick={() => goTo(i)}
                    aria-label={`${i + 1}. bölüm`}
                    className={`h-1.5 overflow-hidden rounded-full bg-slate-300 transition-all ${
                      isActive ? 'w-16' : 'w-8 hover:bg-slate-400'
                    }`}
                  >
                    {/* Dolum çubuğu — width yerine transform: scaleX kullanıyoruz. width
                        animasyonu her karede yeniden yerleşim (layout) hesaplattırıp
                        takılmaya (jank) yol açıyordu; scaleX, GPU'daki compositor katmanında
                        işlendiği için akıcı kalıyor. origin-left, dolumun SOLDAN başlamasını
                        sağlıyor (varsayılan merkez değil). */}
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
        </div>
      </div>

      {/* ALT: 4 kart — gri zeminin üstüne -mt ile biniyor, kaymadan bölüm değişince tamamen değişiyor */}
      <div className="-mt-[88px]">
        <div className="mx-auto max-w-7xl px-6">
        <div
          className={`grid grid-cols-2 gap-6 sm:grid-cols-4 transition-opacity duration-300 ${
            fading ? 'opacity-0' : 'opacity-100'
          }`}
        >
          {items.map((auction) => (
            <AuctionCard key={auction.id} auction={auction} fullWidth />
          ))}
        </div>
        </div>
      </div>
    </section>
  )
}

export default MostWantedCarousel
