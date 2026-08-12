import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../lib/axios';
import toast from 'react-hot-toast';
import { Clock, TrendingUp, User, ArrowLeft, Loader2, ShieldCheck, Tag } from 'lucide-react';
import { motion } from 'framer-motion';

const AuctionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  
  const [auction, setAuction] = useState(null);
  const [loading, setLoading] = useState(true);
  
  const [bidAmount, setBidAmount] = useState('');
  const [bidding, setBidding] = useState(false);

  useEffect(() => {
    const fetchAuction = async () => {
      try {
        const response = await api.get(`/api/auctions/${id}`);
        setAuction(response.data);
      } catch (error) {
        toast.error("İlan detayı yüklenemedi!");
        navigate('/');
      } finally {
        setLoading(false);
      }
    };
    fetchAuction();
  }, [id, navigate]);

  const handlePlaceBid = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      toast.error('Teklif vermek için giriş yapmalısınız.');
      navigate('/login');
      return;
    }

    setBidding(true);
    try {
      await api.post('/api/bids', {
        auctionId: id,
        bidderId: user.id, // Güvenlik (Lock) burada çalışacak
        amount: parseFloat(bidAmount)
      });
      
      toast.success('Teklifiniz başarıyla alındı!');
      setBidAmount('');
      
      // Tekliften sonra ilan detayını tekrar çek
      const response = await api.get(`/api/auctions/${id}`);
      setAuction(response.data);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Teklif verirken bir hata oluştu.');
    } finally {
      setBidding(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-grow flex justify-center items-center">
        <Loader2 className="w-12 h-12 text-indigo-500 animate-spin" />
      </div>
    );
  }

  const isEnded = auction.status === 'ENDED' || new Date(auction.endTime).getTime() < new Date().getTime();
  const displayPrice = auction.currentHighestBid || auction.startingPrice;

  return (
    <div className="flex-grow max-w-5xl mx-auto w-full pt-4">
      <button 
        onClick={() => navigate(-1)} 
        className="flex items-center gap-2 text-gray-400 hover:text-white mb-6 transition-colors"
      >
        <ArrowLeft className="w-5 h-5" /> Geri Dön
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* İlan Detayları */}
        <motion.div 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="lg:col-span-2 glass-panel p-8"
        >
          <div className="flex items-center gap-4 mb-6">
            <span className={`px-3 py-1 rounded-full text-sm font-semibold ${isEnded ? 'bg-red-500/20 text-red-400' : 'bg-emerald-500/20 text-emerald-400'}`}>
              {isEnded ? 'Müzayede Kapandı' : 'Aktif Müzayede'}
            </span>
            <span className="text-gray-400 text-sm">ID: {auction.id.substring(0,8)}</span>
          </div>

          <h1 className="text-3xl md:text-4xl font-bold text-white mb-4">{auction.listingTitle}</h1>
          
          <div className="flex items-center gap-6 text-gray-400 mb-8 pb-8 border-b border-white/10">
            <div className="flex items-center gap-2">
              <User className="w-5 h-5 text-indigo-400" />
              <span>Satıcı: {auction.sellerName}</span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-indigo-400" />
              <span>Bitiş: {new Date(auction.endTime).toLocaleString('tr-TR')}</span>
            </div>
          </div>

          <div>
            <h3 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
              <Tag className="w-5 h-5 text-indigo-400" />
              Ürün Açıklaması
            </h3>
            <p className="text-gray-300 leading-relaxed">
              Bu ilan için detaylı açıklama metni. (İleride Listing detayları buraya çekilecek).
            </p>
          </div>
        </motion.div>

        {/* Teklif Verme Paneli */}
        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          className="lg:col-span-1"
        >
          <div className="glass-panel p-6 sticky top-24">
            <div className="mb-6">
              <p className="text-gray-400 text-sm mb-2 uppercase tracking-wider">Mevcut En Yüksek Teklif</p>
              <div className="text-4xl font-bold text-emerald-400 flex items-center gap-2">
                ₺{displayPrice.toLocaleString('tr-TR')}
                <TrendingUp className="w-6 h-6 text-emerald-500/50" />
              </div>
            </div>

            {!isEnded ? (
              <form onSubmit={handlePlaceBid} className="space-y-4">
                <div>
                  <label className="block text-sm text-gray-300 mb-2">Senin Teklifin</label>
                  <div className="relative">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">₺</span>
                    <input 
                      type="number" 
                      min={displayPrice + 1}
                      required
                      placeholder={(displayPrice + 100).toLocaleString('tr-TR')}
                      value={bidAmount}
                      onChange={(e) => setBidAmount(e.target.value)}
                      className="premium-input pl-10 text-xl font-semibold text-emerald-400"
                    />
                  </div>
                </div>

                <button 
                  type="submit" 
                  disabled={bidding}
                  className="btn-accent w-full flex items-center justify-center gap-2 py-4 text-lg mt-4"
                >
                  {bidding ? <Loader2 className="w-6 h-6 animate-spin" /> : (
                    <>Teklifi Gönder <ShieldCheck className="w-5 h-5" /></>
                  )}
                </button>
                <p className="text-xs text-center text-gray-500 mt-4">
                  Teklif verdiğinizde yarış durumu kilidi (Pessimistic Lock) test edilecektir.
                </p>
              </form>
            ) : (
              <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-4 text-center">
                <p className="text-red-400 font-semibold mb-1">Bu Açık Artırma Sona Erdi</p>
                <p className="text-sm text-gray-400">Yeni teklif kabul edilmiyor.</p>
              </div>
            )}
          </div>
        </motion.div>

      </div>
    </div>
  );
};

export default AuctionDetail;
