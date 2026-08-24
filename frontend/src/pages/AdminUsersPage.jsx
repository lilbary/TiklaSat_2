import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

function AdminUsersPage() {
  const [users, setUsers] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')
    fetch('/api/admin/users', {
      headers: { Authorization: 'Bearer ' + token },
    })
      .then((res) => {
        if (!res.ok) throw new Error('Yetkiniz yok veya giriş yapılmamış')
        return res.json()
      })
      .then(setUsers)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <Link to="/" className="text-sm text-blue-600 hover:underline">
        ← Ana sayfaya dön
      </Link>
      <h1 className="mt-3 text-2xl font-bold text-slate-900">Kayıtlı Kullanıcılar</h1>

      {error && <p className="mt-4 text-red-600">{error}</p>}

      <div className="mt-6 overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-6 py-3">İsim</th>
              <th className="px-6 py-3">E-posta</th>
              <th className="px-6 py-3">Telefon</th>
              <th className="px-6 py-3">Kayıt Tarihi</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((u) => (
              <tr key={u.id}>
                <td className="px-6 py-4 font-medium text-slate-900">{u.fullName}</td>
                <td className="px-6 py-4 text-slate-600">{u.email}</td>
                <td className="px-6 py-4 text-slate-600">{u.phone || '—'}</td>
                <td className="px-6 py-4 text-slate-400">
                  {new Date(u.createdAt).toLocaleDateString('tr-TR')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default AdminUsersPage