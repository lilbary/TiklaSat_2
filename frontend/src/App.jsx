import { Routes, Route } from 'react-router-dom'
import AdminUsersPage from './pages/AdminUsersPage'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AuctionDetailPage from './pages/AuctionDetailPage'
import CreateAuctionPage from './pages/CreateAuctionPage'
import SettingsPage from './pages/SettingsPage'
import TekliflerimVeFavorilerimPage from './pages/TekliflerimVeFavorilerimPage'
import AldigimTekliflerPage from './pages/AldigimTekliflerPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminModerationPage from './pages/admin/AdminModerationPage'
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage'
import PublicProfilePage from './pages/PublicProfilePage'

function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/giris" element={<LoginPage />} />
        <Route path="/kayit" element={<RegisterPage />} />
        <Route path="/artirma/:id" element={<AuctionDetailPage />} />
        <Route path="/admin/kullanicilar" element={<AdminUsersPage />} />
        <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
        <Route path="/admin/moderation" element={<AdminModerationPage />} />
        <Route path="/admin/kategoriler" element={<AdminCategoriesPage />} />
        <Route path="/artirma-olustur" element={<CreateAuctionPage />} />
        <Route path="/ayarlar" element={<SettingsPage />} />
        <Route path="/tekliflerim-ve-favorilerim" element={<TekliflerimVeFavorilerimPage />} />
        <Route path="/aldigim-teklifler" element={<AldigimTekliflerPage />} />
        <Route path="/profil/:id" element={<PublicProfilePage />} />
      </Routes>
    </div>
  )
}

export default App
