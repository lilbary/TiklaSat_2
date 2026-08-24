import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const CATEGORY_COLORS = [
  'from-blue-200 to-blue-100 text-blue-700',
  'from-emerald-200 to-emerald-100 text-emerald-700',
  'from-amber-200 to-amber-100 text-amber-700',
  'from-rose-200 to-rose-100 text-rose-700',
]

function HeartIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-5 w-5">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 21s-7.5-4.5-9.5-9C1 8.5 2.5 5 6 5c2 0 3.5 1.2 4 2.5C10.5 6.2 12 5 14 5c3.5 0 5 3.5 3.5 7-2 4.5-9.5 9-9.5 9z" />
    </svg>
  )
}

function BellIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-5 w-5">
      <path strokeLinecap="round" strokeLinejoin="round" d="M6 8a6 6 0 0 1 12 0c0 4 1.5 6 2 7H4c.5-1 2-3 2-7z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M9.5 18a2.5 2.5 0 0 0 5 0" />
    </svg>
  )
}

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-5 w-5">
      <circle cx="12" cy="8" r="3.25" />
      <path strokeLinecap="round" d="M5 20c1-3.5 4-5.5 7-5.5s6 2 7 5.5" />
    </svg>
  )
}

function ChevronDownIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
      <path strokeLinecap="round" strokeLinejoin="round" d="M6 9l6 6 6-6" />
    </svg>
  )
}

function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [searchTerm, setSearchTerm] = useState('')
  const [categoriesOpen, setCategoriesOpen] = useState(false)
  const [categories, setCategories] = useState([])
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  useEffect(() => {
    fetch('/api/categories')
      .then((res) => res.json())
      .then(setCategories)
  }, [])

  function handleCategoryClick(categoryId) {
    setCategoriesOpen(false)
    navigate(`/?kategori=${categoryId}`)
  }

  function handleSearch(e) {
    if (e.key !== 'Enter') return
    navigate(`/?ara=${encodeURIComponent(searchTerm)}`)
  }

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <nav className="flex items-center gap-6 border-b border-slate-200 bg-white px-6 py-4">
      <Link to="/" className="text-xl font-bold text-blue-600">TıklaSat</Link>

      <button
        onClick={() => setCategoriesOpen(true)}
        className="text-sm font-medium text-slate-700 hover:text-blue-600"
      >
        Kategoriler
      </button>

      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        onKeyDown={handleSearch}
        placeholder="Marka, model ara..."
        className="flex-1 rounded-md border border-slate-300 px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />

      {user ? (
        <div className="flex items-center gap-5">
          <Link to="/artirma-olustur" className="text-sm font-medium text-slate-700 hover:text-blue-600">
            Artırma Oluştur
          </Link>

          <button type="button" aria-label="Favoriler" className="text-slate-700 hover:text-blue-600">
            <HeartIcon />
          </button>

          <button type="button" aria-label="Bildirimler" className="text-slate-700 hover:text-blue-600">
            <BellIcon />
          </button>

          <div className="relative">
            <button
              type="button"
              onClick={() => setUserMenuOpen((v) => !v)}
              className="flex items-center gap-1.5 text-sm font-medium text-slate-700 hover:text-blue-600"
            >
              <UserIcon />
              {user.sub}
              <ChevronDownIcon />
            </button>

            {userMenuOpen && (
              <>
                <div className="fixed inset-0 z-40" onClick={() => setUserMenuOpen(false)} />
                <div className="absolute right-0 top-full z-50 mt-2 w-64 rounded-lg bg-white py-2 shadow-lg ring-1 ring-slate-200">
                  <p className="px-4 pb-1 pt-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    Keşfet
                  </p>
                  {/* TODO: sayfaları birlikte kurup buraya link olarak bağlayacağız */}
                  <button
                    type="button"
                    className="block w-full px-4 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Tekliflerim ve Favorilerim
                  </button>
                  <button
                    type="button"
                    className="block w-full px-4 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Aldığım Teklifler
                  </button>

                  <p className="mt-2 border-t border-slate-100 px-4 pb-1 pt-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    Hesabım
                  </p>
                  <button
                    type="button"
                    className="block w-full px-4 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Ayarlar
                  </button>
                  <button
                    type="button"
                    className="block w-full px-4 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Mesajlar
                  </button>

                  <button
                    type="button"
                    onClick={handleLogout}
                    className="mt-2 block w-full border-t border-slate-100 px-4 pt-2 text-left text-sm text-slate-400 hover:text-slate-600"
                  >
                    Çıkış Yap
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      ) : (
        <>
          <Link to="/giris" className="text-sm font-medium text-slate-700 hover:text-blue-600">
            Giriş Yap
          </Link>
          <Link to="/kayit" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
            Kayıt Ol
          </Link>
        </>
      )}

      {categoriesOpen && (
        <div
          className="fixed inset-0 z-50 flex items-start justify-center bg-black/50 p-6 pt-24"
          onClick={() => setCategoriesOpen(false)}
        >
          <div
            className="w-full max-w-3xl rounded-2xl bg-white p-6 shadow-xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">Kategoriler</h2>
              <button
                onClick={() => setCategoriesOpen(false)}
                className="text-2xl text-slate-400 hover:text-slate-700"
              >
                ×
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4">
              {categories.map((cat, i) => (
                <button
                  key={cat.id}
                  onClick={() => handleCategoryClick(cat.id)}
                  className={`flex h-32 items-end rounded-xl bg-gradient-to-br p-4 text-left text-lg font-bold ${CATEGORY_COLORS[i % CATEGORY_COLORS.length]}`}
                >
                  {cat.name}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </nav>
  )
}

export default Navbar
