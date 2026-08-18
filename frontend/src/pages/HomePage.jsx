import { useEffect, useState } from 'react'

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
    <div className="w-56 shrink-0">
      <Placeholder
        letter={auction.listingTitle.charAt(0)}
        from="from-blue-100"
        to="to-blue-50"
        text="text-blue-300"
      />
      <p className="line-clamp-2 text-sm font-semibold text-slate-900">{auction.listingTitle}</p>
      <p className="mt-1 text-sm text-slate-600">{auction.startingPrice.toLocaleString('tr-TR')} TL</p>
      <p className="text-xs text-slate-400">Bitiş: {endLabel}</p>
    </div>
  )
}

function ListingCard({ listing }) {
  return (
    <div className="w-56 shrink-0">
      <Placeholder
        letter={listing.title.charAt(0)}
        from="from-slate-200"
        to="to-slate-100"
        text="text-slate-400"
      />
      <p className="line-clamp-2 text-sm font-semibold text-slate-900">{listing.title}</p>
      <p className="mt-1 text-xs text-slate-500">{listing.categoryName}</p>
    </div>
  )
}

function HomePage() {
  const [auctions, setAuctions] = useState([])
  const [listings, setListings] = useState([])

  useEffect(() => {
    fetch('/api/auctions')
      .then((res) => res.json())
      .then(setAuctions)

    fetch('/api/listings')
      .then((res) => res.json())
      .then(setListings)
  }, [])

  const now = Date.now()
  const in24h = now + 24 * 60 * 60 * 1000
  const in7days = now + 7 * 24 * 60 * 60 * 1000

  const endingToday = auctions.filter((a) => new Date(a.endTime).getTime() <= in24h)
  const endingThisWeek = auctions.filter((a) => {
    const t = new Date(a.endTime).getTime()
    return t > in24h && t <= in7days
  })
  const newestListings = [...listings].sort(
    (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
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

      {newestListings.length > 0 && (
        <Section title="Yeni Eklenenler">
          {newestListings.map((l) => (
            <ListingCard key={l.id} listing={l} />
          ))}
        </Section>
      )}

      {listings.length > 0 && (
        <Section title="Tüm İlanlar">
          {listings.map((l) => (
            <ListingCard key={l.id} listing={l} />
          ))}
        </Section>
      )}
    </div>
  )
}

export default HomePage
