import { useState, useEffect } from 'react';
import api from '../lib/axios';
import AuctionCard from '../components/AuctionCard';
import { Search, Loader2, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';

const HomePage = () => {
  const [auctions, setAuctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const fetchAuctions = async () => {
      try {
        const response = await api.get('/api/auctions');
        setAuctions(response.data);
      } catch (error) {
        console.error("İlanlar yüklenirken hata oluştu:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchAuctions();
  }, []);

  const filteredAuctions = auctions.filter(auction => 
    auction.listingTitle.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="w-full max-w-7xl mx-auto flex-grow flex flex-col relative z-10">
      
      {/* HERO SECTION */}
      <div className="text-center py-16 px-4">
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 mb-6"
        >
          <Sparkles className="w-4 h-4" />
          <span className="text-sm font-medium">Yeni Nesil Açık Artırma Platformu</span>
        </motion.div>
        
        <motion.h1 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="text-5xl md:text-7xl font-bold mb-6 text-white"
        >
          Değerini Sen Belirle,<br />
          <span className="bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-purple-400 to-emerald-400">
            Zirvede Bırak.
          </span>
        </motion.h1>
        
        <motion.p 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-gray-400 text-lg md:text-xl max-w-2xl mx-auto mb-10"
        >
          Premium ürünler, güvenli teklif sistemi ve rekabet dolu anlar. İstediğin ürüne hemen şimdi teklif ver.
        </motion.p>

        {/* SEARCH BAR */}
        <motion.div 
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.3 }}
          className="relative max-w-xl mx-auto"
        >
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input 
            type="text" 
            placeholder="Ne aramıştınız? (Örn: iPhone 15, Rolex...)" 
            className="premium-input pl-12 py-4 rounded-full text-lg shadow-2xl bg-white/10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </motion.div>
      </div>

      {/* AUCTIONS GRID */}
      <div className="px-4 pb-20">
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-2xl font-bold text-white flex items-center gap-3">
            <span className="w-2 h-8 bg-emerald-500 rounded-full"></span>
            Günün Fırsatları
          </h2>
          <span className="text-gray-400 text-sm">{filteredAuctions.length} sonuç bulundu</span>
        </div>

        {loading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="w-10 h-10 text-indigo-500 animate-spin" />
          </div>
        ) : filteredAuctions.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredAuctions.map((auction) => (
              <AuctionCard key={auction.id} auction={auction} />
            ))}
          </div>
        ) : (
          <div className="text-center py-20 glass-panel">
            <p className="text-gray-400 text-lg">Arama kriterlerinize uygun açık artırma bulunamadı.</p>
          </div>
        )}
      </div>
      
    </div>
  );
};

export default HomePage;
