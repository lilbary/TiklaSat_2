import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })

    if (!response.ok) {
      const data = await response.json()
      setError(data.message || 'Giriş başarısız')
      return
    }

    const data = await response.json()
    localStorage.setItem('token', data.token)
    navigate('/')
  }

  return (
    <div className="flex min-h-[80vh] items-center justify-center bg-slate-50 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-lg font-semibold text-slate-900">Giriş Yap</h1>
          <Link to="/kayit" className="text-sm font-medium text-blue-600 hover:underline">
            Hesap oluştur
          </Link>
        </div>

        <p className="mb-6 text-2xl font-bold text-slate-900">Tekrar hoş geldin!</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="E-posta adresi"
            className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm text-slate-900 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />

          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Şifre"
            className="w-full rounded-lg bg-slate-100 px-4 py-3 text-sm text-slate-900 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />

          {error && <p className="text-sm text-red-600">{error}</p>}

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center gap-2 text-slate-600">
              <input type="checkbox" className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500" />
              Beni hatırla
            </label>
            <a href="#" className="font-medium text-blue-600 hover:underline">
              Şifremi unuttum
            </a>
          </div>

          <p className="text-xs text-slate-500">
            Giriş yaparak <a href="#" className="underline">Kullanım Şartları</a>'nı kabul etmiş olursun.
          </p>

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 py-3 text-sm font-semibold text-white transition-colors hover:bg-blue-700"
          >
            Giriş Yap
          </button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
