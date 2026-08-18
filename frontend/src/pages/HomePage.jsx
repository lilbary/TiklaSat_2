import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

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
      className={`mb-3 flex h-40 items-center justify-center rounded-xl bg-gradient-to-br ${from} ${to} text-4xl font-bold ${text}`}
    >
      {letter}
    </div>
  )
}

function AuctionCard({ auction }) {
  const endLabel = new Date(auction.endTime).toLocaleString('tr-TR', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })

  return (
    <Link to={`/artirma/${auction.id}`} className="block w-56 shrink-0">
      <Placeholder
        letter={auction.listingTitle.charAt(0)}
        from="from-blue-100"
        to="to-blue-50"
        text="text-blue-300"
      />
      <p className="line-clamp-2 text-sm font-semibold text-slate-900">{auction.listingTitle}</p>
      <p className="mt-1 text-sm text-slate-600">{auction.currentPrice.toLocaleString('tr-TR')} TL</p>
      <p className="text-xs text-slate-400">Bitiş: {endLabel}</p>
    </Link>
  )
}

function HomePage() {
  const [auctions, setAuctions] = useState([])

  useEffect(() => {
    fetch('/api/auctions')
      .then((res) => res.json())
      .then(setAuctions)
  }, [])

  const now = Date.now()
  const in24h = now + 24 * 60 * 60 * 1000
  const in7days = now + 7 * 24 * 60 * 60 * 1000

  // Sadece hâlâ ACTIVE olan VE bitiş zamanı henüz gelmemiş artırmalar —
  // süresi dolmuş (ENDED) olanlar hiçbir bölümde görünmemeli.
  // Kullanıcıya sadece gerçekten teklif verilebilen açık artırmalar gösterilir;
  // henüz artırmaya çıkarılmamış ham "ilan" kayıtları arka planda kalır.
  const activeAuctions = auctions.filter(
    (a) => a.status === 'ACTIVE' && new Date(a.endTime).getTime() > now
  )

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
    </div>
  )
}

export default HomePage
