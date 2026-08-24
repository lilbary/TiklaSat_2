import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

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
  const [subCategories, setSubCategories] = useState({}) // Cache for subcategories
  const [activeCategoryId, setActiveCategoryId] = useState(null)
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  useEffect(() => {
    fetch('/api/categories')
      .then((res) => res.json())
      .then((data) => {
        setCategories(data)
        if (data.length > 0) {
          setActiveCategoryId(data[0].id) // Varsayılan ilk kategoriyi seç
          fetchSubCategories(data[0].id)
        }
      })
  }, [])

  // Alt kategorileri getir ve önbelleğe (cache) al
  const fetchSubCategories = async (parentId) => {
    if (!subCategories[parentId]) {
      try {
        const res = await fetch(`/api/categories/${parentId}/subcategories`)
        const data = await res.json()
        setSubCategories((prev) => ({ ...prev, [parentId]: data }))
      } catch (err) {
        console.error('Alt kategoriler yüklenemedi', err)
      }
    }
  }

  const handleCategoryHover = (catId) => {
    setActiveCategoryId(catId)
    fetchSubCategories(catId)
  }

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
    // 1. DÜZELTME: sticky top-0 z-50 ile navbar'ı her zaman üstte tutuyoruz
    <nav className="sticky top-0 z-50 flex items-center gap-6 border-b border-slate-200 bg-white px-6 py-4">
      <Link to="/" className="text-xl font-bold text-blue-600">TıklaSat</Link>

      <div
        className="relative"
        onMouseEnter={() => setCategoriesOpen(true)}
        onMouseLeave={() => setCategoriesOpen(false)}
      >
        <button
          className="flex items-center gap-1 text-sm font-semibold text-slate-700 hover:text-blue-600 py-2"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          Kategoriler
        </button>

        {/* 2. DÜZELTME: Trendyol Tarzı Mega Menü */}
        {categoriesOpen && (
          <div className="absolute top-full left-[-100px] w-[800px] bg-white shadow-2xl rounded-b-lg border border-slate-100 flex min-h-[400px]">
            {/* Sol Menü: Ana Kategoriler */}
            <div className="w-1/4 bg-slate-50 border-r border-slate-200 py-4">
              {categories.map((cat) => (
                <div
                  key={cat.id}
                  onMouseEnter={() => handleCategoryHover(cat.id)}
                  onClick={() => handleCategoryClick(cat.id)}
                  className={`cursor-pointer px-6 py-3 text-sm font-medium transition-colors ${
                    activeCategoryId === cat.id
                      ? 'bg-white text-blue-600 border-l-4 border-blue-600 font-bold shadow-sm'
                      : 'text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  {cat.name}
                  {activeCategoryId === cat.id && (
                    <span className="float-right text-blue-600">&gt;</span>
                  )}
                </div>
              ))}
            </div>

            {/* Sağ Menü: Alt Kategoriler */}
            <div className="w-3/4 p-8 bg-white">
              <h3 className="text-lg font-bold text-slate-800 mb-6 border-b pb-2">
                {categories.find(c => c.id === activeCategoryId)?.name} Kategorisi
              </h3>
              
              <div className="grid grid-cols-3 gap-6">
                {/* Eğer veritabanında alt kategori yoksa placeholder gösterelim */}
                {(!subCategories[activeCategoryId] || subCategories[activeCategoryId].length === 0) ? (
                  <div className="col-span-3 text-sm text-slate-500 italic">
                    Henüz alt kategori bulunmuyor. Ana kategoriye gitmek için yandaki menüye tıklayabilirsiniz.
                  </div>
                ) : (
                  subCategories[activeCategoryId].map((subCat) => (
                    <div key={subCat.id} className="flex flex-col gap-2">
                      <button
                        onClick={() => handleCategoryClick(subCat.id)}
                        className="text-left text-sm font-bold text-slate-800 hover:text-blue-600"
                      >
                        {subCat.name}
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        onKeyDown={handleSearch}
        placeholder="Marka, ürün veya kategori ara..."
        className="flex-1 rounded-md border border-slate-300 px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
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
              <span className="max-w-[100px] truncate">{user.sub}</span>
              <ChevronDownIcon />
            </button>

            {userMenuOpen && (
              <>
                <div className="fixed inset-0 z-40" onClick={() => setUserMenuOpen(false)} />
                <div className="absolute right-0 top-full z-50 mt-2 w-64 rounded-lg bg-white py-2 shadow-lg ring-1 ring-slate-200">
                  <p className="px-4 pb-1 pt-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    Keşfet
                  </p>
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
                  <Link
                    to="/ayarlar"
                    onClick={() => setUserMenuOpen(false)}
                    className="block w-full px-4 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Ayarlar
                  </Link>
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
          <Link to="/kayit" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors">
            Kayıt Ol
          </Link>
        </>
      )}
    </nav>
  )
}

export default Navbar
