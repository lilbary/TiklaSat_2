import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../context/AuthContext.jsx'
import FavoriteHeartButton from '../components/FavoriteHeartButton.jsx'

// Backend'deki BidService.calculateMinIncrement ile AYNI merdiven — burada sadece
// "önerilen hazır teklif" butonlarını göstermek için kullanılıyor. Gerçek doğrulama
// her zaman sunucuda yapılıyor, bu sadece bir kullanıcı deneyimi kolaylığı.
function calculateMinIncrement(price) {
  if (price < 100) return 1
  if (price < 500) return 5
  if (price < 1000) return 10
  if (price < 5000) return 50
  if (price < 10000) return 100
  if (price < 50000) return 500
  if (price < 100000) return 1000
  return 5000
}

function quickBidOptions(currentPrice) {
  const step = calculateMinIncrement(currentPrice)
  return [currentPrice + step, currentPrice + step * 2, currentPrice + step * 3]
}

function useCountdown(endTime) {
  const [remainingMs, setRemainingMs] = useState(() =>
    Math.max(0, new Date(endTime).getTime() - Date.now())
  )

  useEffect(() => {
    const tick = () => setRemainingMs(Math.max(0, new Date(endTime).getTime() - Date.now()))
    tick()
    const interval = setInterval(tick, 1000)
    return () => clearInterval(interval)
  }, [endTime])

  const totalSeconds = Math.floor(remainingMs / 1000)
  return {
    days: Math.floor(totalSeconds / 86400),
    hours: Math.floor((totalSeconds % 86400) / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
    ended: remainingMs <= 0,
  }
}

function timeAgo(isoDate) {
  const seconds = Math.floor((Date.now() - new Date(isoDate).getTime()) / 1000)
  if (seconds < 60) return 'az önce'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes} dakika önce`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} saat önce`
  const days = Math.floor(hours / 24)
  return `${days} gün önce`
}

function CountdownBox({ value, label }) {
  return (
    <div className="flex flex-1 flex-col items-center rounded-xl bg-slate-900 py-3 text-white">
      <span className="text-2xl font-bold tabular-nums">{String(value).padStart(2, '0')}</span>
      <span className="text-[11px] uppercase tracking-wide text-slate-400">{label}</span>
    </div>
  )
}

// Fotoğraf yoksa başlığın ilk harfini gösteren placeholder
function ImagePlaceholder({ letter }) {
  return (
    <div className="flex aspect-square items-center justify-center rounded-2xl bg-gradient-to-br from-red-100 to-red-50 text-8xl font-bold text-red-300">
      {letter}
    </div>
  )
}

function AuctionDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()

  const [auction, setAuction] = useState(null)
  const [currentPrice, setCurrentPrice] = useState(null)
  const [bidHistory, setBidHistory] = useState([])
  const [bidAmount, setBidAmount] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [selectedImage, setSelectedImage] = useState(0) // Hangi fotoğraf büyük gösteriliyor

  // 1. Sayfa açılınca artırmanın bilgisini ve teklif geçmişini çek
  useEffect(() => {
    fetch(`/api/auctions/${id}`)
      .then((res) => res.json())
      .then((data) => {
        setAuction(data)
        setCurrentPrice(data.currentPrice)
      })

    fetch(`/api/bids/auction/${id}`)
      .then((res) => res.json())
      .then(setBidHistory)
  }, [id])

  // 2. WebSocket bağlantısı — bu artırmanın kanalına abone ol
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-auction'),
      onConnect: () => {
        client.subscribe(`/topic/auctions.${id}`, (message) => {
          const newBid = JSON.parse(message.body)
          setCurrentPrice(newBid.amount)
          setBidHistory((prev) => [newBid, ...prev])
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
  async function submitBid(amount) {
    setError('')

    const token = localStorage.getItem('token')
    const response = await fetch('/api/bids', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ auctionId: id, amount: Number(amount) }),
    })

    if (!response.ok) {
      const data = await response.json()
      setError(data.message || 'Teklif verilemedi')
      return
    }

    setBidAmount('')
  }

  function handleBid(e) {
    e.preventDefault()
    submitBid(bidAmount)
  }

  const countdown = useCountdown(auction?.endTime ?? Date.now())
  const isActive = auction?.status === 'ACTIVE' && !countdown.ended

  if (!auction) {
    return <div className="mx-auto max-w-2xl px-6 py-16 text-slate-500">Yükleniyor...</div>
  }

  // Fotoğraf listesi (API'den gelen veya boş)
  const images = auction.imageUrls || []
  const hasImages = images.length > 0

  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <Link to="/" className="text-sm text-red-600 hover:underline">
        ← Ana sayfaya dön
      </Link>

      <h1 className="mt-3 text-2xl font-bold text-slate-900">{auction.listingTitle}</h1>

      <div className="mt-6 grid grid-cols-1 gap-8 lg:grid-cols-[1fr_380px]">
        {/* SOL KOLON: fotoğraflar + açıklama */}
        <div>
          {/* ANA FOTOĞRAF */}
          <div className="relative">
            {hasImages ? (
              <img
                src={images[selectedImage]}
                alt={auction.listingTitle}
                className="aspect-square w-full rounded-2xl object-cover"
              />
            ) : (
              <ImagePlaceholder letter={auction.listingTitle.charAt(0)} />
            )}
            <FavoriteHeartButton
              auctionId={auction.id}
              className="absolute right-3 top-3 flex h-10 w-10 items-center justify-center rounded-full bg-white/90 shadow"
            />
          </div>

          {/* KÜÇÜK FOTOĞRAF GALERİSİ */}
          {hasImages && images.length > 1 && (
            <div className="mt-3 grid grid-cols-4 gap-3">
              {images.map((url, i) => (
                <img
                  key={i}
                  src={url}
                  alt={`Fotoğraf ${i + 1}`}
                  onClick={() => setSelectedImage(i)}
                  className={`aspect-square cursor-pointer rounded-xl object-cover transition-all ${
                    selectedImage === i
                      ? 'ring-2 ring-red-500'
                      : 'opacity-70 ring-1 ring-slate-200 hover:opacity-100'
                  }`}
                />
              ))}
            </div>
          )}

          {/* FOTOĞRAF YOKSA PLACEHOLDER KUTULARI */}
          {!hasImages && (
            <div className="mt-3 grid grid-cols-4 gap-3">
              {[0, 1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="flex aspect-square items-center justify-center rounded-xl bg-gradient-to-br from-red-100 to-red-50 text-2xl font-bold text-red-300"
                >
                  {auction.listingTitle.charAt(0)}
                </div>
              ))}
            </div>
          )}

          {auction.listingDescription && (
            <div className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">Açıklama</h2>
              <p className="whitespace-pre-line text-sm text-slate-600">
                {auction.listingDescription}
              </p>
            </div>
          )}
        </div>

        {/* SAĞ KOLON: süre, satıcı, fiyat, teklif geçmişi */}
        <div>
          {isActive ? (
            <div className="flex gap-2">
              <CountdownBox value={countdown.days} label="Gün" />
              <CountdownBox value={countdown.hours} label="Saat" />
              <CountdownBox value={countdown.minutes} label="Dakika" />
              <CountdownBox value={countdown.seconds} label="Saniye" />
            </div>
          ) : (
            <div className="rounded-xl bg-slate-100 p-4">
              <p className="text-sm font-semibold text-red-500">Bu açık artırma sona erdi.</p>
              {auction.winnerName ? (
                <p className="mt-1 text-sm text-slate-700">
                  Kazanan: <span className="font-semibold">{auction.winnerName}</span>
                </p>
              ) : (
                <p className="mt-1 text-sm text-slate-500">Hiç teklif almadan sona erdi.</p>
              )}
            </div>
          )}

          <div className="mt-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs text-slate-500">
              İlan sahibi: <span className="font-medium text-slate-700">{auction.sellerName}</span>
            </p>

            <p className="mt-4 text-sm text-slate-500">Güncel fiyat</p>
            <p className="text-3xl font-bold text-slate-900">
              {currentPrice.toLocaleString('tr-TR')} TL
            </p>

            {notice && (
              <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{notice}</p>
            )}

            {isActive && user && (
              <>
                <div className="mt-6 flex gap-2">
                  {quickBidOptions(currentPrice).map((amount) => (
                    <button
                      key={amount}
                      type="button"
                      onClick={() => setBidAmount(String(amount))}
                      className={`flex-1 rounded-lg border px-2 py-2 text-sm font-semibold transition-colors ${
                        Number(bidAmount) === amount
                          ? 'border-red-600 bg-red-50 text-red-700'
                          : 'border-slate-200 text-slate-700 hover:border-red-300'
                      }`}
                    >
                      {amount.toLocaleString('tr-TR')} TL
                    </button>
                  ))}
                </div>

                <form onSubmit={handleBid} className="mt-3 flex gap-3">
                  <input
                    type="number"
                    required
                    value={bidAmount}
                    onChange={(e) => setBidAmount(e.target.value)}
                    placeholder="Teklifiniz (TL)"
                    className="flex-1 rounded-lg bg-slate-100 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                  />
                  <button
                    type="submit"
                    className="rounded-lg bg-red-600 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-red-700"
                  >
                    Teklif Ver
                  </button>
                </form>
              </>
            )}

            {isActive && !user && (
              <p className="mt-6 text-sm text-slate-500">
                Teklif verebilmek için{' '}
                <Link to="/giris" className="text-red-600 underline">
                  giriş yapmalısın
                </Link>
                .
              </p>
            )}

            {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
          </div>

          {bidHistory.length > 0 && (
            <div className="mt-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">
                Teklif Geçmişi ({bidHistory.length})
              </h2>
              <ul className="divide-y divide-slate-100">
                {bidHistory.map((bid) => (
                  <li key={bid.id} className="flex items-center justify-between py-2 text-sm">
                    <span className="text-slate-700">{bid.bidderName}</span>
                    <span className="text-slate-400">{timeAgo(bid.createdAt)}</span>
                    <span className="font-semibold text-slate-900">
                      {bid.amount.toLocaleString('tr-TR')} TL
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AuctionDetailPage
