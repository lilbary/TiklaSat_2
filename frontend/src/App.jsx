import { Routes, Route } from 'react-router-dom'
import AdminUsersPage from './pages/AdminUsersPage'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AuctionDetailPage from './pages/AuctionDetailPage'
import CreateAuctionPage from './pages/CreateAuctionPage'
import SettingsPage from './pages/SettingsPage'

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
        <Route path="/artirma-olustur" element={<CreateAuctionPage />} />
        <Route path="/ayarlar" element={<SettingsPage />} />
      </Routes>
    </div>
  )
}

export default App
