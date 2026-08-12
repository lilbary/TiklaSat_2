import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, User, Sparkles, Gavel } from 'lucide-react';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="glass-panel sticky top-4 z-50 mx-4 mt-4 px-6 py-4 flex justify-between items-center">
      <Link to="/" className="flex items-center gap-2 group">
        <div className="bg-indigo-500/20 p-2 rounded-lg group-hover:bg-indigo-500/40 transition-colors">
          <Gavel className="text-indigo-400 w-6 h-6" />
        </div>
        <span className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
          TıklaSat
        </span>
      </Link>

      <div className="flex items-center gap-4">
        {isAuthenticated ? (
          <>
            <div className="flex items-center gap-2 px-4 py-2 bg-white/5 rounded-full border border-white/5">
              <User className="w-4 h-4 text-emerald-400" />
              <span className="text-sm text-gray-200 font-medium">{user.fullName}</span>
            </div>
            
            <Link to="/create-auction" className="btn-accent text-sm py-2 px-4 flex items-center gap-2">
              <Sparkles className="w-4 h-4" />
              İlan Ver
            </Link>
            
            <button 
              onClick={handleLogout}
              className="p-2 hover:bg-white/10 rounded-full transition-colors text-gray-400 hover:text-red-400"
              title="Çıkış Yap"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="text-gray-300 hover:text-white font-medium px-4 py-2 transition-colors">
              Giriş Yap
            </Link>
            <Link to="/register" className="btn-primary text-sm py-2 px-6">
              Kayıt Ol
            </Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
