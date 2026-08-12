import { Link } from 'react-router-dom';
import { Clock, TrendingUp, ShieldCheck } from 'lucide-react';
import { motion } from 'framer-motion';

const AuctionCard = ({ auction }) => {
  // Bitiş zamanını formatlamak için basit bir hesaplama
  const calculateTimeLeft = (endTimeStr) => {
    const end = new Date(endTimeStr).getTime();
    const now = new Date().getTime();
    const diff = end - now;
    
    if (diff <= 0) return 'Süresi Doldu';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) return `${days} Gün ${hours} Saat Kaldı`;
    return `${hours} Saat Kaldı`;
  };

  const isEnded = new Date(auction.endTime).getTime() < new Date().getTime();

  return (
    <motion.div 
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      whileHover={{ y: -5 }}
      className="glass-card flex flex-col h-full overflow-hidden relative group"
    >
      {/* Kartın üst kısmındaki dekoratif parlama */}
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-indigo-500 to-emerald-500 opacity-50 group-hover:opacity-100 transition-opacity"></div>
      
      <div className="p-6 flex-grow flex flex-col">
        {/* Ürün İsmi ve Durumu */}
        <div className="flex justify-between items-start mb-4">
          <h3 className="text-xl font-bold text-white line-clamp-2">
            {auction.listingTitle}
          </h3>
          <span className={`px-2 py-1 rounded-md text-xs font-semibold whitespace-nowrap ${isEnded ? 'bg-red-500/20 text-red-400' : 'bg-emerald-500/20 text-emerald-400'}`}>
            {isEnded ? 'Kapandı' : 'Aktif'}
          </span>
        </div>

        {/* Fiyat Bilgisi */}
        <div className="bg-white/5 rounded-xl p-4 mb-4 border border-white/5">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm text-gray-400">Mevcut En Yüksek</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-emerald-400">
            ₺{auction.currentHighestBid ? auction.currentHighestBid.toLocaleString('tr-TR') : auction.startingPrice.toLocaleString('tr-TR')}
          </div>
        </div>

        {/* Zaman ve Satıcı */}
        <div className="mt-auto space-y-3">
          <div className="flex items-center gap-2 text-sm text-gray-300">
            <Clock className="w-4 h-4 text-indigo-400" />
            <span>{calculateTimeLeft(auction.endTime)}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-gray-400">
            <ShieldCheck className="w-4 h-4 text-gray-500" />
            <span>Satıcı: {auction.sellerName}</span>
          </div>
        </div>
      </div>

      {/* Aksiyon Butonu */}
      <div className="p-4 pt-0">
        <Link 
          to={`/auction/${auction.id}`} 
          className="w-full flex items-center justify-center py-3 bg-white/5 hover:bg-indigo-600/20 border border-white/10 rounded-xl text-white font-medium transition-all group-hover:border-indigo-500/50"
        >
          {isEnded ? 'Sonuçları Gör' : 'Teklif Ver'}
        </Link>
      </div>
    </motion.div>
  );
};

export default AuctionCard;
