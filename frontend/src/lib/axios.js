import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// İstek (Request) Interceptor'ı: Her istekten önce çalışır
api.interceptors.request.use(
  (config) => {
    // LocalStorage'dan bileti (Token) al
    const token = localStorage.getItem('token');
    
    // Eğer bilet varsa ve istek yapılan adres auth (giriş/kayıt) değilse, bileti başlığa (Header) ekle
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
