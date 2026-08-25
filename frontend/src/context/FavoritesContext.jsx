import { createContext, useContext, useEffect, useState } from 'react'
import { useAuth } from './AuthContext.jsx'

const FavoritesContext = createContext(null)

export function FavoritesProvider({ children }) {
  const { user } = useAuth()
  const [favoriteIds, setFavoriteIds] = useState(new Set())

  useEffect(() => {
    if (!user) {
      setFavoriteIds(new Set())
      return
    }

    const token = localStorage.getItem('token')
    fetch('/api/favorites/mine', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setFavoriteIds(new Set(data.map((a) => a.id))))
  }, [user])

  async function toggleFavorite(auctionId) {
    const token = localStorage.getItem('token')
    const isFav = favoriteIds.has(auctionId)

    // Optimistic update: sunucudan cevap beklemeden ekranı hemen güncelle
    setFavoriteIds((prev) => {
      const next = new Set(prev)
      isFav ? next.delete(auctionId) : next.add(auctionId)
      return next
    })

    const res = await fetch(`/api/favorites/${auctionId}`, {
      method: isFav ? 'DELETE' : 'POST',
      headers: { Authorization: `Bearer ${token}` },
    })

    // Sunucu hata döndürdüyse, yaptığımız değişikliği geri al
    if (!res.ok) {
      setFavoriteIds((prev) => {
        const next = new Set(prev)
        isFav ? next.add(auctionId) : next.delete(auctionId)
        return next
      })
    }
  }

  return (
    <FavoritesContext.Provider value={{ favoriteIds, toggleFavorite }}>
      {children}
    </FavoritesContext.Provider>
  )
}

export function useFavorites() {
  return useContext(FavoritesContext)
}