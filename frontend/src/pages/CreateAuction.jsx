import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../lib/axios';
import toast from 'react-hot-toast';
import { Package, AlignLeft, DollarSign, Clock, ArrowRight, Loader2, Info } from 'lucide-react';
import { motion } from 'framer-motion';

const CreateAuction = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(false);
  const [categoryId, setCategoryId] = useState('');
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    startingPrice: '',
    durationDays: '3', // Varsayılan 3 gün
  });

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('İlan vermek için giriş yapmalısınız.');
      navigate('/login');
      return;
    }

    // Kategori kontrolü (Eğer veritabanı boşsa otomatik Genel kategori oluştur)
    const fetchOrSeedCategory = async () => {
      try {
        let res = await api.get('/api/categories');
        let categories = res.data;
        
        if (categories.length === 0) {
          const createRes = await api.post('/api/categories', {
            name: 'Genel',
            description: 'Genel kategorisi'
          });
          setCategoryId(createRes.data.id);
        } else {
          setCategoryId(categories[0].id);
        }
      } catch (error) {
        console.error("Kategori hatası", error);
      }
    };
    
    fetchOrSeedCategory();
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!categoryId) {
      toast.error('Sistem henüz hazır değil, lütfen bekleyin.');
      return;
    }
    
    setLoading(true);
    
    try {
      // 1. Önce İlanı (Listing) Oluştur
      const listingRes = await api.post('/api/listings', {
        title: formData.title,
        description: formData.description,
        startingPrice: parseFloat(formData.startingPrice),
        categoryId: categoryId,
        sellerId: user.id
      });
      
      const listingId = listingRes.data.id;
      
      // 2. Bitiş zamanını hesapla (Şu an + seçilen gün)
      const endTime = new Date();
      endTime.setDate(endTime.getDate() + parseInt(formData.durationDays));
      
      // 3. Açık Artırmayı Başlat
      const auctionRes = await api.post('/api/auctions', {
        listingId: listingId,
        startingPrice: parseFloat(formData.startingPrice),
        endTime: endTime.toISOString()
      });
      
      toast.success('Müzayede başarıyla başlatıldı!');
      navigate(`/auction/${auctionRes.data.id}`);
      
    } catch (error) {
      toast.error('İlan oluşturulurken bir hata oluştu.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center p-4">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass-panel w-full max-w-2xl p-8 relative overflow-hidden"
      >
        <div className="absolute -top-20 -right-20 w-40 h-40 bg-indigo-500/20 rounded-full blur-[50px]"></div>

        <div className="mb-8">
          <h2 className="text-3xl font-bold text-white mb-2">Yeni Açık Artırma Başlat</h2>
          <p className="text-gray-400 text-sm">Ürününü binlerce alıcıyla buluştur.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
          
          <div className="space-y-2">
            <label className="text-sm text-gray-300 ml-1">Ürün Başlığı</label>
            <div className="relative">
              <Package className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Örn: 2023 Model Temiz MacBook Pro"
                required
                className="premium-input pl-12"
                value={formData.title}
                onChange={(e) => setFormData({...formData, title: e.target.value})}
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm text-gray-300 ml-1">Ürün Açıklaması</label>
            <div className="relative">
              <AlignLeft className="absolute left-4 top-4 w-5 h-5 text-gray-400" />
              <textarea
                placeholder="Ürününüzü detaylıca anlatın..."
                required
                rows={4}
                className="premium-input pl-12 py-4 resize-none"
                value={formData.description}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm text-gray-300 ml-1">Başlangıç Fiyatı (₺)</label>
              <div className="relative">
                <DollarSign className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-emerald-400" />
                <input
                  type="number"
                  min="1"
                  required
                  placeholder="1000"
                  className="premium-input pl-12 text-emerald-400 font-semibold"
                  value={formData.startingPrice}
                  onChange={(e) => setFormData({...formData, startingPrice: e.target.value})}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm text-gray-300 ml-1">Müzayede Süresi</label>
              <div className="relative">
                <Clock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-indigo-400" />
                <select
                  className="premium-input pl-12 appearance-none bg-[#1e293b]"
                  value={formData.durationDays}
                  onChange={(e) => setFormData({...formData, durationDays: e.target.value})}
                >
                  <option value="1">1 Gün (Hızlı Satış)</option>
                  <option value="3">3 Gün (Standart)</option>
                  <option value="7">7 Gün (Uzun Süreli)</option>
                </select>
              </div>
            </div>
          </div>

          <div className="bg-indigo-500/10 border border-indigo-500/20 rounded-xl p-4 flex gap-3 mt-4">
            <Info className="w-5 h-5 text-indigo-400 flex-shrink-0" />
            <p className="text-xs text-indigo-200/80 leading-relaxed">
              İlanınız anında yayına alınacaktır. Açık artırma süresi dolduğunda otomatik çalışan Cron Job sayesinde sistem teklifleri kapatıp kazananı belirleyecektir.
            </p>
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className="btn-primary w-full flex items-center justify-center gap-2 py-4 mt-6 text-lg"
          >
            {loading ? <Loader2 className="w-6 h-6 animate-spin" /> : (
              <>
                Müzayedeyi Başlat <ArrowRight className="w-5 h-5" />
              </>
            )}
          </button>
        </form>
      </motion.div>
    </div>
  );
};

export default CreateAuction;
