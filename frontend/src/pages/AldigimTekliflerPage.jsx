import { useEffect, useState } from 'react'
import { AuctionCard } from './HomePage.jsx'

function AldigimTekliflerPage() {
  const [receivedBids, setReceivedBids] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    fetch('/api/bids/received', { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => res.json())
      .then(setReceivedBids)
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="mx-auto max-w-7xl px-6 py-10">
      <h2 className="mb-8 text-xl font-bold text-slate-900">Aldığım Teklifler</h2>

      {loading ? (
        <p className="text-slate-500">Yükleniyor...</p>
      ) : receivedBids.length === 0 ? (
        <p className="text-slate-500">İlanlarına henüz hiç teklif gelmedi.</p>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {receivedBids.map((received) => (
            <div key={received.auction.id}>
              <AuctionCard auction={received.auction} />
              <p className="text-xs text-slate-500">
                En yüksek teklif: {received.topBidAmount.toLocaleString('tr-TR')} TL — {received.topBidderName}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default AldigimTekliflerPage
