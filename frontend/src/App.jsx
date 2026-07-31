import React from 'react'
import Navbar from './components/Navbar'
import Hero from './components/Hero'

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-grow">
        <Hero />
        {/* Gelecek güncellemelerde Canlı Müzayedeler kartları buraya eklenecek */}
      </main>
      
      <footer className="bg-white border-t border-slate-200 mt-20 py-10">
        <div className="max-w-7xl mx-auto px-4 text-center text-slate-500">
          <p>© 2026 TıklaSat. Tüm hakları saklıdır.</p>
          <p className="text-sm mt-2">Bu bir geliştirme projesidir.</p>
        </div>
      </footer>
    </div>
  )
}

export default App
