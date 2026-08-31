import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useFavorites } from '../context/FavoritesContext.jsx'

function HeartIcon({ filled }) {
  return (
    <svg
      viewBox="0 0 24 24"
      className="h-5 w-5"
      fill={filled ? 'currentColor' : 'none'}
      stroke="currentColor"
      strokeWidth="1.5"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z"
      />
    </svg>
  )
}

function FavoriteHeartButton({ auctionId, className = '' }) {
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
      className={`${className} ${isFavorited ? 'text-rose-500' : 'text-slate-600 hover:text-rose-500'}`}
    >
      <HeartIcon filled={isFavorited} />
    </button>
  )
}

export default FavoriteHeartButton
