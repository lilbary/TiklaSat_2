import React from 'react';
import { ShoppingBag, Search, Bell, User, Menu } from 'lucide-react';

const Navbar = () => {
  return (
    <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          
          {/* Logo */}
          <div className="flex items-center gap-2 cursor-pointer">
            <div className="bg-brand-500 p-2 rounded-xl text-white">
              <ShoppingBag size={24} strokeWidth={2.5} />
            </div>
            <span className="font-bold text-2xl tracking-tight text-slate-900">
              Tıkla<span className="text-brand-500">Sat</span>
            </span>
          </div>

          {/* Search Bar (Desktop) */}
          <div className="hidden md:flex flex-1 max-w-xl mx-8">
            <div className="relative w-full group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={18} className="text-slate-400 group-focus-within:text-brand-500 transition-colors" />
              </div>
              <input
                type="text"
                className="block w-full pl-10 pr-3 py-2.5 border border-slate-200 rounded-xl leading-5 bg-slate-50 placeholder-slate-400 focus:outline-none focus:bg-white focus:ring-2 focus:ring-brand-500/50 focus:border-brand-500 transition-all duration-300"
                placeholder="Açık artırmalarda ara... (Örn: iPhone 15)"
              />
            </div>
          </div>

          {/* Right Actions */}
          <div className="flex items-center gap-4">
            <button className="p-2 text-slate-500 hover:text-brand-600 hover:bg-brand-50 rounded-full transition-colors relative">
              <Bell size={20} />
              <span className="absolute top-1.5 right-1.5 block h-2.5 w-2.5 rounded-full bg-rose-500 ring-2 ring-white"></span>
            </button>
            <div className="hidden md:flex items-center gap-3 border-l border-slate-200 pl-4">
              <button className="text-slate-600 font-medium hover:text-brand-600 transition-colors">Giriş Yap</button>
              <button className="btn-primary flex items-center gap-2">
                <User size={18} />
                <span>Kayıt Ol</span>
              </button>
            </div>
            {/* Mobile menu button */}
            <button className="md:hidden p-2 text-slate-600 rounded-lg hover:bg-slate-100">
              <Menu size={24} />
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
