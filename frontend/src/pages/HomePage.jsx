import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AuctionCard } from '../components/AuctionCard.jsx'
import MostWantedCarousel from '../components/MostWantedCarousel.jsx'
import WeeklyCategoryHighlights from '../components/WeeklyCategoryHighlights.jsx'

function Section({ title, children }) {
  return (
    <section className="mb-10">
      <h2 className="mb-4 text-xl font-bold text-slate-900">{title}</h2>
      <div className="flex gap-4 overflow-x-auto pb-2">{children}</div>
    </section>
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
          <MostWantedCarousel />
          <WeeklyCategoryHighlights />

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
