import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const CATEGORY_COLORS = [
  'from-blue-200 to-blue-100 text-blue-700',
  'from-emerald-200 to-emerald-100 text-emerald-700',
  'from-amber-200 to-amber-100 text-amber-700',
  'from-rose-200 to-rose-100 text-rose-700',
]

function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [searchTerm, setSearchTerm] = useState('')
  const [categoriesOpen, setCategoriesOpen] = useState(false)
  const [categories, setCategories] = useState([])

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
        <div className="flex items-center gap-4">
          <span className="text-sm font-medium text-slate-700">{user.sub}</span>
          <Link to="/artirma-olustur" className="text-sm font-medium text-slate-700 hover:text-blue-600">
            Artırma Oluştur
          </Link>
          <button
            onClick={handleLogout}
            className="text-sm font-medium text-slate-700 hover:text-blue-600"
          >
            Çıkış Yap
          </button>
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
