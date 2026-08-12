import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../lib/axios';
import toast from 'react-hot-toast';
import { Mail, Lock, ArrowRight, Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';

const LoginPage = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      // Backend'e istek at
      const response = await api.post('/api/auth/login', formData);
      const { token, user } = response.data;
      
      // Token'ı ve kullanıcıyı belleğe yaz
      login(user, token);
      
      toast.success(`Hoş geldin, ${user.fullName}!`);
      navigate('/');
    } catch (error) {
      toast.error('Giriş başarısız. E-posta veya şifre hatalı.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-grow flex items-center justify-center p-4">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass-panel w-full max-w-md p-8 relative overflow-hidden"
      >
        {/* Dekoratif Işık */}
        <div className="absolute -top-20 -right-20 w-40 h-40 bg-indigo-500/20 rounded-full blur-[50px]"></div>

        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-white mb-2">Hoş Geldin</h2>
          <p className="text-gray-400 text-sm">Devam etmek için hesabına giriş yap.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 relative z-10">
          <div>
            <div className="relative">
              <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="email"
                placeholder="E-posta adresi"
                required
                className="premium-input pl-12"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
              />
            </div>
          </div>

          <div>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="password"
                placeholder="Şifre"
                required
                className="premium-input pl-12"
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
              />
            </div>
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className="btn-primary w-full flex items-center justify-center gap-2 mt-4"
          >
            {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : (
              <>
                Giriş Yap <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-sm text-gray-400">
          Hesabın yok mu?{' '}
          <Link to="/register" className="text-indigo-400 hover:text-indigo-300 font-medium">
            Hemen Kayıt Ol
          </Link>
        </div>
      </motion.div>
    </div>
  );
};

export default LoginPage;
