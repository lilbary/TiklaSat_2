import { useEffect, useState } from 'react'
import { AuctionCard } from './HomePage.jsx'

const TABS = [
  { id: 'teklifler', label: 'Tekliflerim' },
  { id: 'favoriler', label: 'Favorilerim' },
]

function TekliflerimVeFavorilerimPage() {
  const [activeTab, setActiveTab] = useState('teklifler')
  const [myBids, setMyBids] = useState([])
  const [favorites, setFavorites] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const url = activeTab === 'teklifler' ? '/api/bids/mine' : '/api/favorites/mine'

    setLoading(true)
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => res.json())
      .then((data) => {
        if (activeTab === 'teklifler') setMyBids(data)
        else setFavorites(data)
      })
      .finally(() => setLoading(false))
  }, [activeTab])

  return (
    <div className="mx-auto max-w-7xl px-6 py-10">
      <div className="mb-8 flex gap-6 border-b border-slate-200">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`pb-3 text-sm font-semibold ${
              activeTab === tab.id
                ? 'border-b-2 border-red-600 text-red-600'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="text-slate-500">Yükleniyor...</p>
      ) : activeTab === 'teklifler' ? (
        myBids.length === 0 ? (
          <p className="text-slate-500">Henüz hiç teklif vermedin.</p>
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
            {myBids.map((myBid) => (
              <div key={myBid.auction.id} className="relative">
                <span
                  className={`absolute left-2 top-2 z-10 rounded-full px-2 py-0.5 text-xs font-semibold ${
                    myBid.winning ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'
                  }`}
                >
                  {myBid.winning ? 'Önde gidiyorsun' : 'Geride kaldın'}
                </span>
                <AuctionCard auction={myBid.auction} />
                <p className="text-xs text-slate-500">Teklifin: {myBid.myBidAmount.toLocaleString('tr-TR')} TL</p>
              </div>
            ))}
          </div>
        )
      ) : favorites.length === 0 ? (
        <p className="text-slate-500">Henüz favori eklemedin.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {favorites.map((auction) => (
            <AuctionCard key={auction.id} auction={auction} />
          ))}
        </div>
      )}
    </div>
  )
}

export default TekliflerimVeFavorilerimPage
