import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <nav className="flex items-center gap-6 border-b border-slate-200 bg-white px-6 py-4">
      <Link to="/" className="text-xl font-bold text-blue-600">TıklaSat</Link>

      <a href="#" className="text-sm font-medium text-slate-700 hover:text-blue-600">
        Kategoriler
      </a>

      <input
        type="text"
        placeholder="Marka, model ara..."
        className="flex-1 rounded-md border border-slate-300 px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />

      {user ? (
        <div className="flex items-center gap-4">
          <span className="text-sm font-medium text-slate-700">{user.sub}</span>
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
    </nav>
  )
}

export default Navbar
