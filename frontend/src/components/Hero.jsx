import React from 'react';
import { ArrowRight, Gavel, Clock, ShieldCheck } from 'lucide-react';

const Hero = () => {
  return (
    <div className="relative overflow-hidden bg-white">
      {/* Background decoration */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[500px] opacity-30 pointer-events-none">
        <div className="absolute inset-0 bg-gradient-to-r from-brand-200 to-teal-100 rounded-full blur-3xl"></div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-24 relative z-10">
        <div className="text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-brand-50 text-brand-600 font-medium text-sm mb-6 border border-brand-100 shadow-sm">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-brand-500"></span>
            </span>
            Canlı Müzayedeler Başladı!
          </div>
          
          <h1 className="text-5xl md:text-6xl font-extrabold text-slate-900 tracking-tight leading-tight mb-6">
            Hayalindeki Ürünü <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-500 to-teal-400">
              Teklifinle Yakala
            </span>
          </h1>
          
          <p className="text-lg md:text-xl text-slate-500 mb-10 max-w-2xl mx-auto leading-relaxed">
            TıklaSat, Türkiye'nin en güvenilir ve hızlı açık artırma platformudur. İstediğin ürüne teklif ver, son saniye heyecanını yaşa.
          </p>
          
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <button className="w-full sm:w-auto btn-primary px-8 py-3.5 text-lg flex items-center justify-center gap-2 group">
              Müzayedeleri Keşfet
              <ArrowRight size={20} className="group-hover:translate-x-1 transition-transform" />
            </button>
            <button className="w-full sm:w-auto btn-outline px-8 py-3.5 text-lg bg-white">
              Hemen İlan Ver
            </button>
          </div>
        </div>

        {/* Feature Highlights */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-24">
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <div className="w-14 h-14 bg-brand-50 rounded-2xl flex items-center justify-center text-brand-500 mb-4 shadow-sm border border-brand-100">
              <Gavel size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">Adil Teklif Sistemi</h3>
            <p className="text-slate-500">Milisaniyelik kilit sistemi ile teklifiniz asla çakışmaz, hakkınız yenmez.</p>
          </div>
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <div className="w-14 h-14 bg-rose-50 rounded-2xl flex items-center justify-center text-rose-500 mb-4 shadow-sm border border-rose-100">
              <Clock size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">Anti-Sniper Koruması</h3>
            <p className="text-slate-500">Son saniye hırsızlarına son! Kapanışa yakın teklif gelirse süre uzar.</p>
          </div>
          <div className="glass-card p-6 flex flex-col items-center text-center">
            <div className="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-500 mb-4 shadow-sm border border-blue-100">
              <ShieldCheck size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">%100 Güvenli</h3>
            <p className="text-slate-500">Admin onayından geçmeyen hiçbir ilan açık artırmaya çıkamaz.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Hero;
