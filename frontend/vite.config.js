import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  // sockjs-client, tarayıcıda olmayan Node.js'e özgü "global" nesnesini
  // kullanıyor — burada "global" gördüğün yerde "globalThis" kullan diyoruz.
  define: {
    global: 'globalThis',
  },
})
