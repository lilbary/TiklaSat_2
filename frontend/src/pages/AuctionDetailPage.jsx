import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../context/AuthContext.jsx'

function AuctionDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()

  const [auction, setAuction] = useState(null)
  const [currentPrice, setCurrentPrice] = useState(null)
  const [bidAmount, setBidAmount] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  // 1. Sayfa açılınca artırmanın bilgisini çek
  useEffect(() => {
    fetch(`/api/auctions/${id}`)
      .then((res) => res.json())
      .then((data) => {
        setAuction(data)
        setCurrentPrice(data.currentPrice)
      })
  }, [id])

  // 2. WebSocket bağlantısı — bu artırmanın kanalına abone ol
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-auction'),
      onConnect: () => {
        client.subscribe(`/topic/auctions.${id}`, (message) => {
          const newBid = JSON.parse(message.body)
          setCurrentPrice(newBid.amount)
          setNotice(`Yeni teklif: ${newBid.amount.toLocaleString('tr-TR')} TL (${newBid.bidderName})`)
        })
      },
    })

    client.activate()

    // Temizlik: sayfadan ayrılınca bağlantıyı kapat
    return () => {
      client.deactivate()
    }
  }, [id])

  // 3. Teklif verme
  async function handleBid(e) {
    e.preventDefault()
    setError('')

    const token = localStorage.getItem('token')
    const response = await fetch('/api/bids', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ auctionId: id, amount: Number(bidAmount) }),
    })

    if (!response.ok) {
      const data = await response.json()
      setError(data.message || 'Teklif verilemedi')
      return
    }

    setBidAmount('')
  }

  if (!auction) {
    return <div className="mx-auto max-w-2xl px-6 py-16 text-slate-500">Yükleniyor...</div>
  }

  return (
    <div className="mx-auto max-w-2xl px-6 py-10">
      <Link to="/" className="text-sm text-blue-600 hover:underline">
        ← Ana sayfaya dön
      </Link>

      <h1 className="mt-3 text-2xl font-bold text-slate-900">{auction.listingTitle}</h1>
      <p className="mt-1 text-sm text-slate-500">
        Bitiş: {new Date(auction.endTime).toLocaleString('tr-TR')}
      </p>

      <div className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <p className="text-sm text-slate-500">Güncel fiyat</p>
        <p className="text-3xl font-bold text-slate-900">
          {currentPrice.toLocaleString('tr-TR')} TL
        </p>

        {notice && (
          <p className="mt-3 rounded-lg bg-blue-50 px-3 py-2 text-sm text-blue-700">{notice}</p>
        )}

        {user ? (
          <form onSubmit={handleBid} className="mt-6 flex gap-3">
            <input
              type="number"
              required
              value={bidAmount}
              onChange={(e) => setBidAmount(e.target.value)}
              placeholder="Teklifiniz (TL)"
              className="flex-1 rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="submit"
              className="rounded-lg bg-blue-600 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-blue-700"
            >
              Teklif Ver
            </button>
          </form>
        ) : (
          <p className="mt-6 text-sm text-slate-500">
            Teklif verebilmek için{' '}
            <Link to="/giris" className="text-blue-600 underline">
              giriş yapmalısın
            </Link>
            .
          </p>
        )}

        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </div>
    </div>
  )
}

export default AuctionDetailPage
