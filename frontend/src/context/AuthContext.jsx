import { createContext, useContext, useState, useEffect } from 'react'

const AuthContext = createContext(null)

function decodeToken(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4
    const padded = pad ? base64 + '='.repeat(4 - pad) : base64
    return JSON.parse(atob(padded))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token')
    return token ? decodeToken(token) : null
  })

  // Sadece context fonksiyonları
  const handleLogin = (token) => {
    localStorage.setItem('token', token)
    setUser(decodeToken(token))
  }

  const handleLogout = async () => {
    // Önce Backend'e çıkış isteği at (Çerezi silmesi için)
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {
      console.error("Backend çıkış hatası", e);
    }
    
    localStorage.removeItem('token')
    setUser(null)
  }

  // GLOBAL FETCH INTERCEPTOR
  useEffect(() => {
    const originalFetch = window.fetch;
    
    // Refresh işlemi devam ederken aynı anda gelen 5 isteği bekletmek için kilit
    let isRefreshing = false; 
    let refreshSubscribers = [];

    const onRefreshed = (token) => {
      refreshSubscribers.forEach((cb) => cb(token));
      refreshSubscribers = [];
    };

    window.fetch = async (...args) => {
      let [resource, config] = args;

      // 1. İstek bizim backendimize gidiyorsa (ve login/refresh değilse) token'ı ekle
      if (typeof resource === 'string' && resource.startsWith('/api') && !resource.startsWith('/api/auth/')) {
        const token = localStorage.getItem('token');
        if (token) {
          config = config || {};
          config.headers = {
            ...config.headers,
            'Authorization': `Bearer ${token}`
          };
        }
      }

      // 2. Orijinal isteği yap
      let response = await originalFetch(resource, config);

      // 3. Eğer 401 Yetkisiz hatası aldıysak ve bu bir auth isteği değilse (sonsuz döngüyü önle)
      if (response.status === 401 && typeof resource === 'string' && !resource.startsWith('/api/auth/')) {
        
        // Eğer zaten başka bir istek refresh yapıyorsa, onu bekle
        if (isRefreshing) {
          return new Promise((resolve) => {
            refreshSubscribers.push((newToken) => {
              if (newToken) {
                // Token yenilendi, orijinal isteği yeni token ile tekrar yap
                config.headers['Authorization'] = `Bearer ${newToken}`;
                resolve(originalFetch(resource, config));
              } else {
                resolve(response); // Yenilenemediyse orijinal 401'i dön
              }
            });
          });
        }

        // İlk 401 alan istek buraya girer ve Refresh işlemini başlatır
        isRefreshing = true;

        try {
          const refreshResponse = await originalFetch('/api/auth/refresh', {
            method: 'POST',
            // Çerezler tarayıcı tarafından otomatik gönderilir (credentials: 'include' varsayılan değilse bile Spring CookieValue yakalıyor)
          });

          if (refreshResponse.ok) {
            const data = await refreshResponse.json();
            handleLogin(data.token); // Yeni token'ı kaydet
            onRefreshed(data.token); // Bekleyen diğer isteklere haber ver
            
            // Başarısız olan bu orijinal isteği YENİ token ile tekrar et
            config.headers['Authorization'] = `Bearer ${data.token}`;
            response = await originalFetch(resource, config);
          } else {
            // Refresh başarısız olduysa (Çerez de bitmişse/30 gün geçmişse)
            handleLogout();
            onRefreshed(null);
            window.location.href = '/giris'; // Kullanıcıyı girişe at
          }
        } catch (error) {
          handleLogout();
          onRefreshed(null);
        } finally {
          isRefreshing = false;
        }
      }

      return response;
    };

    // Cleanup: Component unmount olursa (neredeyse hiç olmaz ama adettendir) eski fetch'i geri koy
    return () => {
      window.fetch = originalFetch;
    };
  }, []);

  return (
    <AuthContext.Provider value={{ user, login: handleLogin, logout: handleLogout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
