import { createContext, useContext, useState, useEffect } from 'react'

const AuthContext = createContext(null)

// Aşağıdaki interceptor window.fetch'i sarmalıyor. Yenileme isteğinin
// SARMALANMAMIŞ fetch'i kullanması şart, yoksa kendi kendini tetikler.
const nativeFetch = window.fetch.bind(window)

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

// Token'ın ömrü bitmek üzere mi?
// exp saniye cinsinden, Date.now() milisaniye — 1000 ile çarpmak şart.
// 60 saniyelik pay bırakıyoruz: sunucu ile tarayıcı saati birkaç saniye
// kayabilir ve token isteğin yolda geçtiği sürede ölmemeli.
function isExpiringSoon(token, marginSeconds = 60) {
  const payload = decodeToken(token)
  if (!payload?.exp) return true
  return payload.exp * 1000 - Date.now() < marginSeconds * 1000
}

// Token hâlâ rahatça kullanılabilir mi?
function isUsable(token) {
  return Boolean(token) && !isExpiringSoon(token)
}

// Aynı anda beş istek birden yenileme tetiklerse /api/auth/refresh'e beş kez
// gidilmesin diye ortak kilit. Modül seviyesinde duruyor çünkü hem açılıştaki
// sessiz giriş hem de interceptor AYNI kilidi paylaşmak zorunda; iki ayrı
// kilit olsaydı açılışta iki yenileme isteği birden giderdi.
let refreshPromise = null

function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = nativeFetch('/api/auth/refresh', { method: 'POST' })
      .then((r) => (r.ok ? r.json() : null))
      .then((data) => data?.token ?? null)
      .catch(() => null)
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

export function AuthProvider({ children }) {
  // Elimizdeki token hâlâ geçerliyse kullanıcıyı ilk render'da kuruyoruz ki
  // sayfa açılışında "giriş yap" yazısı bir an görünüp kaybolmasın.
  // Süresi dolmuşsa null başlıyoruz, kararı aşağıdaki sessiz yenileme veriyor.
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token')
    return isUsable(token) ? decodeToken(token) : null
  })

  // Açılışta sessiz yenileme sürüyor mu? Sayfalar isterse bunu bekleyebilir.
  // Sadece elimizde ESKİMİŞ bir token varsa deniyoruz: bu, kullanıcının daha
  // önce giriş yaptığının işareti ve refresh çerezi hâlâ duruyor olabilir.
  // Hiç token yoksa ziyaretçi zaten girişsiz; boşuna istek atıp konsola
  // kırmızı 401 yazdırmıyoruz.
  const [booting, setBooting] = useState(() => {
    const token = localStorage.getItem('token')
    return Boolean(token) && !isUsable(token)
  })

  const handleLogin = (token) => {
    localStorage.setItem('token', token)
    setUser(decodeToken(token))
  }

  const handleLogout = async () => {
    // Backend'e haber ver: Redis'teki refresh token silinsin, çerez temizlensin.
    // Sarmalanmamış fetch kullanıyoruz ki bu istek interceptor'dan geçmesin.
    try {
      await nativeFetch('/api/auth/logout', { method: 'POST' })
    } catch (e) {
      console.error('Backend çıkış hatası', e)
    }

    localStorage.removeItem('token')
    setUser(null)
  }

  // ---------------------------------------------------------------------
  // AÇILIŞTA SESSİZ GİRİŞ
  // Eskiden localStorage'daki token exp'e bakılmadan çözülüyordu: süresi
  // dolmuş olsa bile kullanıcı "girişli" görünüyor ama her istek patlıyordu.
  // Üstelik 30 günlük "beni hatırla" çerezi hiç kullanılmıyordu — kullanıcı
  // ertesi gün geldiğinde kendini girişli sanıyor, hiçbir şey çalışmıyordu.
  // ---------------------------------------------------------------------
  useEffect(() => {
    if (!booting) return

    let iptalEdildi = false

    // Token yok ya da ölmüş. Tarayıcıda hâlâ httpOnly refresh çerezi
    // olabilir; sessizce yeni bir access token iste.
    refreshAccessToken().then((yeniToken) => {
      if (iptalEdildi) return

      if (yeniToken) {
        localStorage.setItem('token', yeniToken)
        setUser(decodeToken(yeniToken))
      } else {
        // Çerez de yok/bitmiş: ölü token'ı temizle.
        localStorage.removeItem('token')
        setUser(null)
      }
      setBooting(false)
    })

    return () => {
      iptalEdildi = true
    }
  }, [booting])

  // ---------------------------------------------------------------------
  // GLOBAL FETCH INTERCEPTOR
  // ---------------------------------------------------------------------
  useEffect(() => {
    const originalFetch = window.fetch

    window.fetch = async (...args) => {
      let [resource, config] = args

      // Kendi backend'imize giden, auth dışındaki istekler.
      const korumaliUc =
        typeof resource === 'string' &&
        resource.startsWith('/api') &&
        !resource.startsWith('/api/auth/')

      if (korumaliUc) {
        let token = localStorage.getItem('token')

        // ÖNGÖRÜLÜ YENİLEME: token ölmek üzereyse isteği GÖNDERMEDEN önce
        // yenile. Eskiden akış "gönder → patla → yenile → tekrar gönder"
        // olduğu için kullanıcı her 15 dakikada bir başarısız bir istek
        // yaşıyordu. Artık yaşamıyor.
        if (token && isExpiringSoon(token)) {
          const yeniToken = await refreshAccessToken()
          if (yeniToken) {
            localStorage.setItem('token', yeniToken)
            setUser(decodeToken(yeniToken))
            token = yeniToken
          }
        }

        if (token) {
          config = { ...(config || {}) }
          config.headers = { ...config.headers, Authorization: `Bearer ${token}` }
        }
      }

      let response = await originalFetch(resource, config)

      // EMNİYET AĞI: öngörülü yenileme kaçırmış olabilir (saat kayması, başka
      // sekmede çıkış yapılması, sunucunun yeniden başlaması). 401 gelirse
      // bir kez yenileyip isteği tekrarlıyoruz.
      if (response.status === 401 && korumaliUc) {
        const yeniToken = await refreshAccessToken()

        if (yeniToken) {
          localStorage.setItem('token', yeniToken)
          setUser(decodeToken(yeniToken))
          config = { ...(config || {}) }
          config.headers = { ...config.headers, Authorization: `Bearer ${yeniToken}` }
          response = await originalFetch(resource, config)
        } else {
          // Yenileme de başarısız: refresh çerezi bitmiş ya da iptal edilmiş.
          localStorage.removeItem('token')
          setUser(null)
          if (window.location.pathname !== '/giris') {
            window.location.href = '/giris'
          }
        }
      }

      return response
    }

    // Component kalkarsa orijinal fetch'i geri koy.
    return () => {
      window.fetch = originalFetch
    }
  }, [])

  return (
    <AuthContext.Provider value={{ user, booting, login: handleLogin, logout: handleLogout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
