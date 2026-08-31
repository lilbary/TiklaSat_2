import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'

const TABS = [
  { id: 'account', label: 'Hesap Bilgileri' },
  { id: 'addresses', label: 'Adreslerim' },
  { id: 'payment', label: 'Ödeme' },
  { id: 'emails', label: 'Bildirimler' }
]

export default function SettingsPage() {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('account')
  const [userProfile, setUserProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  // Edit Profile States
  const [isEditingProfile, setIsEditingProfile] = useState(false)
  const [editProfileForm, setEditProfileForm] = useState({ fullName: '', phone: '' })

  // Edit Password States
  const [isEditingPassword, setIsEditingPassword] = useState(false)
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '' })

  // Addresses States
  const [addresses, setAddresses] = useState([])
  const [isAddingAddress, setIsAddingAddress] = useState(false)
  const [addressForm, setAddressForm] = useState({
    title: '', city: '', district: '', fullAddress: '', isDefault: false
  })
  
  const token = localStorage.getItem('token')

  const fetchProfile = async () => {
    if (!token) return
    try {
      const res = await fetch('/api/users/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (res.ok) {
        const data = await res.json()
        setUserProfile(data)
        setEditProfileForm({ fullName: data.fullName || '', phone: data.phone || '' })
      }
    } catch (error) {
      console.error('Failed to fetch user profile', error)
    }
  }

  const fetchAddresses = async () => {
    if (!token) return
    try {
      const res = await fetch('/api/users/me/addresses', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (res.ok) {
        const data = await res.json()
        setAddresses(data)
      }
    } catch (error) {
      console.error('Failed to fetch addresses', error)
    }
  }

  useEffect(() => {
    if (user) {
      setLoading(true)
      Promise.all([fetchProfile(), fetchAddresses()]).finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [user])

  // --- Handlers ---
  const handleUpdateProfile = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/users/me', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(editProfileForm)
      })
      if (res.ok) {
        fetchProfile()
        setIsEditingProfile(false)
        alert("Profil başarıyla güncellendi!")
      } else {
        alert("Profil güncellenemedi.")
      }
    } catch (error) {
      console.error(error)
    }
  }

  const handleChangePassword = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/users/me/password', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(passwordForm)
      })
      if (res.ok) {
        setIsEditingPassword(false)
        setPasswordForm({ oldPassword: '', newPassword: '' })
        alert("Şifre başarıyla güncellendi!")
      } else {
        alert("Eski şifreniz yanlış olabilir.")
      }
    } catch (error) {
      console.error(error)
    }
  }

  const handleAddAddress = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/users/me/addresses', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(addressForm)
      })
      if (res.ok) {
        fetchAddresses()
        setIsAddingAddress(false)
        setAddressForm({ title: '', city: '', district: '', fullAddress: '', isDefault: false })
      }
    } catch (error) {
      console.error(error)
    }
  }

  const handleDeleteAddress = async (id) => {
    if(!confirm('Adresi silmek istediğinize emin misiniz?')) return;
    try {
      const res = await fetch(`/api/users/me/addresses/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (res.ok) {
        fetchAddresses()
      }
    } catch (error) {
      console.error(error)
    }
  }

  const displayName = userProfile?.fullName 
    ? userProfile.fullName.split(' ')[0] 
    : user?.sub?.split('@')[0] || 'Kullanıcı'

  if (loading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 flex justify-center items-center min-h-[50vh]">
        <div className="text-slate-500">Yükleniyor...</div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8 bg-white min-h-[calc(100vh-73px)]">
      <h1 className="mb-10 text-4xl font-bold tracking-tight text-slate-900 font-serif">
        Merhaba {displayName.charAt(0).toUpperCase() + displayName.slice(1)}
      </h1>

      <div className="flex flex-col md:flex-row gap-12">
        <aside className="w-full md:w-64 shrink-0">
          <nav className="flex flex-col gap-1 relative">
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex h-10 items-center px-4 text-sm font-medium transition-colors text-left ${
                  activeTab === tab.id
                    ? 'text-red-600 bg-red-50 font-semibold rounded-md'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </aside>

        <main className="flex-1 max-w-3xl">
          {/* HESAP BİLGİLERİ SEKMESİ */}
          {activeTab === 'account' && (
            <div>
              <h2 className="mb-8 text-2xl font-semibold text-slate-900">Hesap Bilgileri</h2>

              <div className="space-y-8 divide-y divide-slate-200">
                {/* İSİM VE TELEFON GÜNCELLEME */}
                <div className="pt-8 first:pt-0">
                  {!isEditingProfile ? (
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Profil Bilgileri</h3>
                        <p className="text-base text-slate-900 font-medium">{userProfile?.fullName || 'İsim belirtilmemiş'}</p>
                        <p className="text-sm text-slate-600 mt-1">{userProfile?.phone || 'Telefon belirtilmemiş'}</p>
                        <button onClick={() => setIsEditingProfile(true)} className="mt-3 text-sm font-medium text-red-600 hover:text-red-700">
                          Düzenle
                        </button>
                      </div>
                    </div>
                  ) : (
                    <form onSubmit={handleUpdateProfile} className="space-y-4 max-w-sm">
                      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-900 mb-4">Profili Düzenle</h3>
                      <div>
                        <label className="block text-sm text-slate-600 mb-1">Ad Soyad</label>
                        <input type="text" value={editProfileForm.fullName} onChange={e => setEditProfileForm({...editProfileForm, fullName: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm focus:outline-red-500" required />
                      </div>
                      <div>
                        <label className="block text-sm text-slate-600 mb-1">Telefon</label>
                        <input type="tel" value={editProfileForm.phone} onChange={e => setEditProfileForm({...editProfileForm, phone: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm focus:outline-red-500" />
                      </div>
                      <div className="flex gap-2">
                        <button type="submit" className="px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700">Kaydet</button>
                        <button type="button" onClick={() => setIsEditingProfile(false)} className="px-4 py-2 bg-slate-100 text-slate-600 text-sm rounded hover:bg-slate-200">İptal</button>
                      </div>
                    </form>
                  )}
                </div>

                {/* E-POSTA */}
                <div className="pt-8">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">E-posta</h3>
                      <p className="text-base text-slate-900 font-medium">{userProfile?.email || 'Bilinmiyor'}</p>
                    </div>
                  </div>
                </div>

                {/* ŞİFRE GÜNCELLEME */}
                <div className="pt-8">
                  {!isEditingPassword ? (
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Şifre</h3>
                        <p className="text-base text-slate-900 font-medium">********</p>
                        <button onClick={() => setIsEditingPassword(true)} className="mt-3 text-sm font-medium text-red-600 hover:text-red-700">
                          Şifreyi Değiştir
                        </button>
                      </div>
                    </div>
                  ) : (
                    <form onSubmit={handleChangePassword} className="space-y-4 max-w-sm">
                      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-900 mb-4">Şifre Değiştir</h3>
                      <div>
                        <label className="block text-sm text-slate-600 mb-1">Mevcut Şifreniz</label>
                        <input type="password" value={passwordForm.oldPassword} onChange={e => setPasswordForm({...passwordForm, oldPassword: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm focus:outline-red-500" required />
                      </div>
                      <div>
                        <label className="block text-sm text-slate-600 mb-1">Yeni Şifreniz</label>
                        <input type="password" value={passwordForm.newPassword} onChange={e => setPasswordForm({...passwordForm, newPassword: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm focus:outline-red-500" required minLength="6" />
                      </div>
                      <div className="flex gap-2">
                        <button type="submit" className="px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700">Şifreyi Güncelle</button>
                        <button type="button" onClick={() => setIsEditingPassword(false)} className="px-4 py-2 bg-slate-100 text-slate-600 text-sm rounded hover:bg-slate-200">İptal</button>
                      </div>
                    </form>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* ADRESLER SEKMESİ */}
          {activeTab === 'addresses' && (
            <div>
              <div className="flex justify-between items-center mb-8">
                <h2 className="text-2xl font-semibold text-slate-900">Kargo Adreslerim</h2>
                {!isAddingAddress && (
                  <button onClick={() => setIsAddingAddress(true)} className="px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700 font-medium">
                    + Yeni Adres Ekle
                  </button>
                )}
              </div>

              {/* ADRES EKLEME FORMU */}
              {isAddingAddress && (
                <form onSubmit={handleAddAddress} className="mb-8 p-6 border border-slate-200 rounded-lg bg-slate-50 space-y-4">
                  <h3 className="font-medium text-slate-900 border-b pb-2">Yeni Adres Bilgileri</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm text-slate-600 mb-1">Adres Başlığı (Ev, İş vb.)</label>
                      <input type="text" value={addressForm.title} onChange={e => setAddressForm({...addressForm, title: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm" required />
                    </div>
                    <div>
                      <label className="block text-sm text-slate-600 mb-1">İl</label>
                      <input type="text" value={addressForm.city} onChange={e => setAddressForm({...addressForm, city: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm" required />
                    </div>
                    <div>
                      <label className="block text-sm text-slate-600 mb-1">İlçe</label>
                      <input type="text" value={addressForm.district} onChange={e => setAddressForm({...addressForm, district: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm" required />
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm text-slate-600 mb-1">Açık Adres (Mahalle, Sokak, No)</label>
                    <textarea value={addressForm.fullAddress} onChange={e => setAddressForm({...addressForm, fullAddress: e.target.value})} className="w-full rounded-md border border-slate-300 p-2 text-sm" rows="3" required></textarea>
                  </div>
                  <div className="flex gap-2 pt-2">
                    <button type="submit" className="px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700">Kaydet</button>
                    <button type="button" onClick={() => setIsAddingAddress(false)} className="px-4 py-2 border border-slate-300 bg-white text-slate-600 text-sm rounded hover:bg-slate-50">İptal</button>
                  </div>
                </form>
              )}

              {/* ADRES LİSTESİ */}
              {addresses.length === 0 && !isAddingAddress ? (
                <div className="text-center py-10 border-2 border-dashed border-slate-200 rounded-lg text-slate-500">
                  Henüz kayıtlı bir adresiniz bulunmuyor.
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {addresses.map(address => (
                    <div key={address.id} className="p-4 border border-slate-200 rounded-lg hover:border-red-300 transition-colors bg-white shadow-sm">
                      <div className="flex justify-between items-start mb-2">
                        <h4 className="font-semibold text-slate-900">{address.title}</h4>
                        <button onClick={() => handleDeleteAddress(address.id)} className="text-xs text-red-500 hover:text-red-700 font-medium">Sil</button>
                      </div>
                      <p className="text-sm font-medium text-slate-700 mb-1">{address.district}, {address.city}</p>
                      <p className="text-sm text-slate-500 whitespace-pre-wrap">{address.fullAddress}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

                    {/* BİLDİRİMLER SEKMESİ */}
          {activeTab === 'emails' && (
            <div>
              <h2 className="mb-6 text-2xl font-semibold text-slate-900">Bildirim Tercihleri</h2>
              <p className="mb-8 text-sm text-slate-500">
                Hangi durumlarda e-posta veya SMS almak istediğinizi seçin.
              </p>

              <div className="space-y-6">
                <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
                  <div>
                    <h4 className="font-semibold text-slate-900">Teklifim Geçildiğinde</h4>
                    <p className="text-sm text-slate-500">Biri benim verdiğim teklifin üzerine çıkarsa haber ver.</p>
                  </div>
                  <label className="relative inline-flex cursor-pointer items-center">
                    <input type="checkbox" className="peer sr-only" defaultChecked />
                    <div className="peer h-6 w-11 rounded-full bg-slate-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-red-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
                  <div>
                    <h4 className="font-semibold text-slate-900">Açık Artırma Kazandığımda</h4>
                    <p className="text-sm text-slate-500">Takip ettiğim veya katıldığım bir ürün bende kalırsa haber ver.</p>
                  </div>
                  <label className="relative inline-flex cursor-pointer items-center">
                    <input type="checkbox" className="peer sr-only" defaultChecked />
                    <div className="peer h-6 w-11 rounded-full bg-slate-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-red-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
                  <div>
                    <h4 className="font-semibold text-slate-900">İlanım Onaylandığında</h4>
                    <p className="text-sm text-slate-500">Oluşturduğum yeni bir ilan yayına alındığında haber ver.</p>
                  </div>
                  <label className="relative inline-flex cursor-pointer items-center">
                    <input type="checkbox" className="peer sr-only" />
                    <div className="peer h-6 w-11 rounded-full bg-slate-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-red-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300"></div>
                  </label>
                </div>
              </div>
              
              <div className="mt-8 flex justify-end">
                <button className="rounded-lg bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700">
                  Tercihleri Kaydet
                </button>
              </div>
            </div>
          )}
                    {/* ÖDEME YÖNTEMLERİ SEKMESİ */}
          {activeTab === 'payment' && (
            <div>
              <div className="mb-8 flex items-center justify-between">
                <h2 className="text-2xl font-semibold text-slate-900">Kayıtlı Kartlarım</h2>
                <button className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700">
                  + Yeni Kart Ekle
                </button>
              </div>

              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                {/* Mock Kart 1 */}
                <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-800 to-slate-900 p-6 text-white shadow-lg transition-transform hover:-translate-y-1">
                  <div className="absolute right-0 top-0 -mr-8 -mt-8 h-32 w-32 rounded-full bg-white opacity-5"></div>
                  <div className="mb-8 flex items-center justify-between">
                    <span className="font-semibold tracking-wider">Garanti Bonus</span>
                    <svg className="h-8 w-auto text-white opacity-80" viewBox="0 0 36 24" fill="currentColor"><path d="M14.28 2.06c-6.19 0-11.2 5-11.2 11.2s5.01 11.2 11.2 11.2c2.81 0 5.37-1.04 7.34-2.76A11.16 11.16 0 0114.28 2.06z"/><path d="M22.72 2.06c-6.19 0-11.2 5-11.2 11.2s5.01 11.2 11.2 11.2c2.81 0 5.37-1.04 7.34-2.76A11.16 11.16 0 0122.72 2.06z" fillOpacity=".6"/></svg>
                  </div>
                  <div className="mb-2 text-xl font-medium tracking-[0.2em] opacity-90">
                    **** **** **** 4812
                  </div>
                  <div className="flex justify-between text-sm opacity-70">
                    <span>Bayram</span>
                    <span>12/28</span>
                  </div>
                </div>

                {/* Yeni Kart Ekleme Boş Kutusu */}
                <div className="flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 p-6 text-slate-500 transition-colors hover:border-red-400 hover:bg-red-50 hover:text-red-600">
                  <svg className="mb-2 h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  <span className="font-medium">Yeni Kredi Kartı Ekle</span>
                </div>
              </div>
              <p className="mt-6 text-xs text-slate-400">
                Kart bilgileriniz PCI-DSS standartlarında şifrelenerek saklanmaktadır. Güvenliğiniz için kart numaralarınızın tamamı sistemlerimizde tutulmaz.
              </p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
