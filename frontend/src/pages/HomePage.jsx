import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useFavorites } from '../context/FavoritesContext.jsx'

function Section({ title, children }) {
  return (
    <section className="mb-10">
      <h2 className="mb-4 text-xl font-bold text-slate-900">{title}</h2>
      <div className="flex gap-4 overflow-x-auto pb-2">{children}</div>
    </section>
  )
}

function Placeholder({ letter, from, to, text }) {
  return (
    <div
      className={`flex h-40 items-center justify-center rounded-xl bg-gradient-to-br ${from} ${to} text-4xl font-bold ${text}`}
    >
      {letter}
    </div>
  )
}

function HeartButton({ auctionId }) {
  const { user } = useAuth()
  const { favoriteIds, toggleFavorite } = useFavorites()
  const navigate = useNavigate()
  const isFavorited = favoriteIds.has(auctionId)

  function handleClick(e) {
    e.preventDefault()
    e.stopPropagation()

    if (!user) {
      navigate('/giris')
      return
    }
    toggleFavorite(auctionId)
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      aria-label={isFavorited ? 'Favorilerden çıkar' : 'Favorilere ekle'}
      className={`absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 shadow ${
        isFavorited ? 'text-rose-500' : 'text-slate-600 hover:text-rose-500'
      }`}
    >
      <svg
        viewBox="0 0 24 24"
        className="h-5 w-5"
        fill={isFavorited ? 'currentColor' : 'none'}
        stroke="currentColor"
        strokeWidth="1.5"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z"
        />
      </svg>
    </button>
  )
}

export function AuctionCard({ auction }) {
  const endLabel = new Date(auction.endTime).toLocaleString('tr-TR', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })

  const hasImage = auction.imageUrls && auction.imageUrls.length > 0

  return (
    <Link to={`/artirma/${auction.id}`} className="block w-56 shrink-0">
      <div className="relative mb-3">
        {hasImage ? (
          <img
            src={auction.imageUrls[0]}
            alt={auction.listingTitle}
            className="h-40 w-full rounded-xl object-cover"
          />
        ) : (
          <Placeholder
            letter={auction.listingTitle.charAt(0)}
            from="from-red-100"
            to="to-red-50"
            text="text-red-300"
          />
        )}
        <HeartButton auctionId={auction.id} />
      </div>
      <p className="line-clamp-2 text-sm font-semibold text-slate-900">{auction.listingTitle}</p>
      <p className="mt-1 text-sm text-slate-600">{auction.currentPrice.toLocaleString('tr-TR')} TL</p>
      <p className="text-xs text-slate-400">Bitiş: {endLabel}</p>
    </Link>
  )
}

function HomePage() {
  const [auctions, setAuctions] = useState([])
  const [searchResults, setSearchResults] = useState([])
  const [searchPage, setSearchPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const [searchParams] = useSearchParams()
  const searchTerm = searchParams.get('ara') || ''
  const categoryFilter = searchParams.get('kategori') || ''
  const isFiltering = Boolean(searchTerm || categoryFilter)

  useEffect(() => {
  if (isFiltering) return
  fetch('/api/auctions')
    .then((res) => res.json())
    .then(setAuctions)
  }, [isFiltering])

  useEffect(() => {
    setSearchPage(0)
  }, [searchTerm, categoryFilter])

  useEffect(() => {
  if (!isFiltering) return

  const params = new URLSearchParams({ page: searchPage, size: 20 })
  if (searchTerm) params.set('ara', searchTerm)
  if (categoryFilter) params.set('kategori', categoryFilter)

  fetch(`/api/auctions/search?${params}`)
    .then((res) => res.json())
    .then((data) => {
      setSearchResults((prev) => (searchPage === 0 ? data.content : [...prev, ...data.content]))
      setHasMore(searchPage < data.totalPages - 1)
    })
  }, [isFiltering, searchTerm, categoryFilter, searchPage])

  const now = Date.now()
  const in24h = now + 24 * 60 * 60 * 1000
  const in7days = now + 7 * 24 * 60 * 60 * 1000

  // Sadece hâlâ ACTIVE olan VE bitiş zamanı henüz gelmemiş artırmalar —
  // süresi dolmuş (ENDED) olanlar hiçbir bölümde görünmemeli.
  // Kullanıcıya sadece gerçekten teklif verilebilen açık artırmalar gösterilir;
  // henüz artırmaya çıkarılmamış ham "ilan" kayıtları arka planda kalır.
  const activeAuctions = auctions.filter((a) => {
    const isActive = a.status === 'ACTIVE' && new Date(a.endTime).getTime() > now
    const matchesSearch = a.listingTitle.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = categoryFilter ? a.categoryId === categoryFilter : true
    return isActive && matchesSearch && matchesCategory
  })

  const endingToday = activeAuctions.filter((a) => new Date(a.endTime).getTime() <= in24h)
  const endingThisWeek = activeAuctions.filter((a) => {
    const t = new Date(a.endTime).getTime()
    return t > in24h && t <= in7days
  })
  const newestAuctions = [...activeAuctions].sort(
    (a, b) => new Date(b.startTime) - new Date(a.startTime)
  )

  return (
    <div className="mx-auto max-w-7xl px-6 py-10">
      {isFiltering ? (
        <section>
          <h2 className="mb-4 text-xl font-bold text-slate-900">Sonuçlar</h2>

          {searchResults.length === 0 ? (
            <p className="text-slate-500">Eşleşen açık artırma bulunamadı.</p>
          ) : (
            <>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {searchResults.map((a) => (
                  <AuctionCard key={a.id} auction={a} />
                ))}
              </div>

              {hasMore && (
                <button
                  onClick={() => setSearchPage((p) => p + 1)}
                  className="mt-6 rounded-lg border border-slate-300 px-6 py-2 text-sm font-semibold text-slate-700 hover:border-red-400"
                >
                  Daha fazla göster
                </button>
              )}
            </>
          )}
        </section>
      ) : (
        <>
          {endingToday.length > 0 && (
            <Section title="Bugün Bitecekler">
              {endingToday.map((a) => (
                <AuctionCard key={a.id} auction={a} />
              ))}
            </Section>
          )}

          {endingThisWeek.length > 0 && (
            <Section title="Bu Hafta Bitecekler">
              {endingThisWeek.map((a) => (
                <AuctionCard key={a.id} auction={a} />
              ))}
            </Section>
          )}

          {newestAuctions.length > 0 && (
            <Section title="Yeni Eklenenler">
              {newestAuctions.map((a) => (
                <AuctionCard key={a.id} auction={a} />
              ))}
            </Section>
          )}

          {activeAuctions.length > 0 && (
            <Section title="Tüm Açık Artırmalar">
              {activeAuctions.map((a) => (
                <AuctionCard key={a.id} auction={a} />
              ))}
            </Section>
          )}
        </>
      )}
    </div>
  )
}

export default HomePage
