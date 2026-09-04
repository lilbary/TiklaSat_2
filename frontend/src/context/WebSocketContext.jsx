import { createContext, useContext, useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WebSocketContext = createContext(null)

// Uygulama genelinde TEK bir STOMP bağlantısı — Navbar (bildirimler) ve
// AuctionDetailPage (bid'ler) gibi farklı bileşenler, kendi ayrı bağlantılarını
// açmak yerine bu TEK bağlantı üzerinden istedikleri kanala abone oluyor.
//
// Bağlantının kendisi giriş yapılıp yapılmadığından BAĞIMSIZ açılıyor — çünkü
// canlı bid takibi herkese açık bir özellik (giriş yapmamış bir ziyaretçi de
// bir açık artırmanın fiyatının anlık güncellendiğini görebilmeli). Sadece
// KİŞİYE ÖZEL kanallara (örn. bildirimler) abone olmak giriş gerektiriyor —
// bunu çağıran bileşen (Navbar) kendi tarafında zaten kontrol ediyor.
export function WebSocketProvider({ children }) {
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-auction'),
      reconnectDelay: 5000, // bağlantı koparsa 5 saniyede bir yeniden dene
      onConnect: () => setConnected(true),
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [])

  // Bir kanala abone olmak için: subscribe(destination, callback) çağır,
  // dönen fonksiyonu (temizlik için) useEffect'in return'ünde kullan.
  function subscribe(destination, callback) {
    if (!clientRef.current || !connected) return () => {}

    const subscription = clientRef.current.subscribe(destination, (message) => {
      callback(JSON.parse(message.body))
    })

    return () => subscription.unsubscribe()
  }

  return (
    <WebSocketContext.Provider value={{ subscribe, connected }}>
      {children}
    </WebSocketContext.Provider>
  )
}

export function useWebSocket() {
  return useContext(WebSocketContext)
}
