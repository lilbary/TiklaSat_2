import { Link } from 'react-router-dom'
import FavoriteHeartButton from './FavoriteHeartButton.jsx'

function Placeholder({ letter, from, to, text }) {
  return (
    <div
      className={`flex h-44 items-center justify-center rounded-xl bg-gradient-to-br ${from} ${to} text-4xl font-bold ${text}`}
    >
      {letter}
    </div>
  )
}

// fullWidth: true → sabit genişlik yerine kapsayıcı hücreyi tam doldur (grid içinde
// eşit kenar boşluğu için — MostWantedCarousel gibi sabit sayıda kart gösteren
// grid'lerde kullanılır). false (varsayılan) → yatay kaydırmalı Section listelerinde
// gereken sabit genişlik korunur.
export function AuctionCard({ auction, fullWidth = false }) {
  const endLabel = new Date(auction.endTime).toLocaleString('tr-TR', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })

  const hasImage = auction.imageUrls && auction.imageUrls.length > 0

  return (
    <Link to={`/artirma/${auction.id}`} className={fullWidth ? 'block' : 'block w-[246px] shrink-0'}>
      <div className="relative mb-3">
        {hasImage ? (
          <img
            src={auction.imageUrls[0]}
            alt={auction.listingTitle}
            className="h-44 w-full rounded-xl object-cover"
          />
        ) : (
          <Placeholder
            letter={auction.listingTitle.charAt(0)}
            from="from-red-100"
            to="to-red-50"
            text="text-red-300"
          />
        )}
        <FavoriteHeartButton
          auctionId={auction.id}
          className="absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-white/90 shadow"
        />
      </div>
      <p className="line-clamp-2 text-sm font-semibold text-slate-900">{auction.listingTitle}</p>
      <p className="mt-1 text-sm text-slate-600">{auction.currentPrice.toLocaleString('tr-TR')} TL</p>
      <p className="text-xs text-slate-400">Bitiş: {endLabel}</p>
    </Link>
  )
}

export default AuctionCard
