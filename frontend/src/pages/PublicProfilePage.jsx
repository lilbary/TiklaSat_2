import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { AuctionCard } from '../components/AuctionCard.jsx'
import { useAuth } from '../context/AuthContext.jsx'

function ThreeDotsIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className="h-5 w-5">
      <circle cx="12" cy="5" r="1.5" />
      <circle cx="12" cy="12" r="1.5" />
      <circle cx="12" cy="19" r="1.5" />
    </svg>
  )
}

export default function PublicProfilePage() {
  const { id } = useParams()
  const { user } = useAuth()
  const isOwnProfile = user?.userId === id
  const [profile, setProfile] = useState(null)
  const [activeTab, setActiveTab] = useState('ACTIVE')
  const [auctions, setAuctions] = useState([])
  const [loading, setLoading] = useState(true)
  const [openMenuId, setOpenMenuId] = useState(null)

  useEffect(() => {
    // Profil bilgisini çek
    fetch(`/api/users/${id}/profile`)
      .then(res => res.json())
      .then(data => setProfile(data))
      .catch(err => console.error(err))
  }, [id])

  useEffect(() => {
    // Satıcının ilanlarını çek (ACTIVE veya ENDED)
    setLoading(true)
    fetch(`/api/auctions/seller/${id}?status=${activeTab}`)
      .then(res => res.json())
      .then(data => {
        setAuctions(data)
        setLoading(false)
      })
      .catch(err => {
        console.error(err)
        setLoading(false)
      })
  }, [id, activeTab])

  // "e" parametresi şart: bu buton artık AuctionCard'ın (yani bir <Link>'in)
  // İÇİNDE render ediliyor — durdurmazsak tıklama, kartın kendi navigasyonunu da tetikler.
  function handleDelete(e, auction) {
    e.preventDefault()
    e.stopPropagation()
    setOpenMenuId(null)
    if (!window.confirm(`"${auction.listingTitle}" ilanını silmek istediğinize emin misiniz?`)) return

    const token = localStorage.getItem('token')
    fetch(`/api/listings/${auction.listingId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async (res) => {
        if (!res.ok) {
          const data = await res.json()
          throw new Error(data.message || 'İlan silinemedi')
        }
        setAuctions((prev) => prev.filter((a) => a.id !== auction.id))
      })
      .catch((err) => alert(err.message))
  }

  if (!profile) return <div className="py-20 text-center text-slate-500">Yükleniyor...</div>

  const memberSinceDate = new Date(profile.memberSince).toLocaleDateString('tr-TR', {
    year: 'numeric', month: 'long'
  })

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      {/* Profil Header */}
      <div className="mb-10 flex items-center gap-6 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div className="flex h-24 w-24 items-center justify-center rounded-full bg-red-100 text-4xl font-bold text-red-600">
          {profile.fullName.charAt(0)}
        </div>
        <div>
          <h1 className="text-3xl font-bold text-slate-900">{profile.fullName}</h1>
          <p className="mt-1 text-slate-500">TıklaSat üyesi • {memberSinceDate} tarihinden beri</p>
          <div className="mt-3 inline-flex items-center gap-1 rounded-full bg-green-50 px-3 py-1 text-sm font-medium text-green-700 ring-1 ring-green-600/20">
            <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.403 12.652a3 3 0 000-5.304 3 3 0 00-3.75-3.751 3 3 0 00-5.305 0 3 3 0 00-3.751 3.75 3 3 0 000 5.305 3 3 0 003.75 3.751 3 3 0 005.305 0 3 3 0 003.751-3.75zm-2.546-4.46a.75.75 0 00-1.214-.883l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clipRule="evenodd" />
            </svg>
            Onaylı Satıcı
          </div>
        </div>
      </div>

      {/* Sekmeler */}
      <div className="mb-8 border-b border-slate-200">
        <nav className="-mb-px flex gap-8">
          <button
            onClick={() => setActiveTab('ACTIVE')}
            className={`whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium ${
              activeTab === 'ACTIVE'
                ? 'border-red-500 text-red-600'
                : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
            }`}
          >
            Satıştaki İlanlar
          </button>
          <button
            onClick={() => setActiveTab('ENDED')}
            className={`whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium ${
              activeTab === 'ENDED'
                ? 'border-red-500 text-red-600'
                : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
            }`}
          >
            Önceki Satışlar
          </button>
        </nav>
      </div>

      {/* İçerik */}
      {loading ? (
        <div className="py-10 text-center text-slate-500">İlanlar yükleniyor...</div>
      ) : auctions.length === 0 ? (
        <div className="rounded-xl border-2 border-dashed border-slate-200 py-16 text-center">
          <p className="text-slate-500">Bu satıcıya ait {activeTab === 'ACTIVE' ? 'aktif' : 'geçmiş'} ilan bulunamadı.</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {auctions.map(auction => (
            <AuctionCard
              key={auction.id}
              auction={auction}
              actionSlot={
                isOwnProfile && (
                  <div className="relative shrink-0">
                    <button
                      type="button"
                      onClick={(e) => {
                        e.preventDefault()
                        e.stopPropagation()
                        setOpenMenuId(openMenuId === auction.id ? null : auction.id)
                      }}
                      aria-label="İlan seçenekleri"
                      className="flex h-7 w-7 items-center justify-center rounded-full text-slate-500 hover:bg-slate-100 hover:text-slate-700"
                    >
                      <ThreeDotsIcon />
                    </button>

                    {openMenuId === auction.id && (
                      <>
                        <div
                          className="fixed inset-0 z-10"
                          onClick={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            setOpenMenuId(null)
                          }}
                        />
                        <div className="absolute right-0 top-full z-20 mt-1 w-40 overflow-hidden rounded-lg bg-white py-1 shadow-lg ring-1 ring-slate-200">
                          <button
                            type="button"
                            onClick={(e) => handleDelete(e, auction)}
                            className="block w-full px-4 py-2 text-left text-sm font-medium text-red-600 hover:bg-red-50"
                          >
                            İlanı Sil
                          </button>
                        </div>
                      </>
                    )}
                  </div>
                )
              }
            />
          ))}
        </div>
      )}
    </div>
  )
}
