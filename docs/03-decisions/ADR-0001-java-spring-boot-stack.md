# ADR-0001 · Java + Spring Boot Yığını; SignalR Yerine WebSocket/STOMP

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |
| **Karar veren** | Proje sahibi (stajyer) + teknik danışma |

---

## Bağlam

İlk gereksinim taslağında non-fonksiyonel gereksinimler arasında *"eşzamanlı teklif güncellemeleri için WebSocket/SignalR kullanımı"* yazıyordu. Backend dili ise henüz kararlaştırılmamıştı.

Tasarım aşamasına geçerken backend dili **Java** olarak belirlendi. Bu, taslakla doğrudan bir çelişki yarattı:

> **SignalR, Microsoft .NET platformuna özgü bir kütüphanedir. Java'da kullanılamaz.**

SignalR bir protokol değil, bir **uygulamadır** (implementation). İstemci tarafında `@microsoft/signalr` paketi vardır ama sunucu tarafı ASP.NET Core'a bağlıdır. Java sunucusuyla konuşan bir SignalR hub'ı yoktur.

Ek kısıtlar:
- Proje **staj süresi** içinde tamamlanmalı → öğrenme eğrisi dik olmamalı
- Çıktı **savunulabilir** olmalı → yaygın, endüstri standardı araçlar tercih edilmeli
- Gerçek zamanlı teklif yayını **zorunlu** bir gereksinimdir (`BR-B-009`)

---

## Karar

**Java 21 (LTS) + Spring Boot 3.x** yığını kullanılacak. Gerçek zamanlı iletişim, SignalR yerine **Spring'in yerleşik WebSocket + STOMP** desteğiyle (SockJS geri düşüşü ile birlikte) sağlanacak.

### Bileşenler

| Katman | Seçim |
|---|---|
| Dil / Runtime | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Web | Spring Web MVC |
| Güvenlik | Spring Security (Argon2id, JWT) |
| Veri erişimi | Spring Data JPA + Hibernate |
| Gerçek zamanlı | **Spring WebSocket + STOMP + SockJS** |
| Zamanlanmış işler | Spring Scheduler + ShedLock |
| Hız sınırlama | Bucket4j + Redis |
| Test | JUnit 5, Testcontainers, Awaitility, AssertJ |
| Derleme | Maven |

### Gereksinim dokümanında yapılan düzeltme

`docs/01-requirements/` altındaki tüm dokümanlarda **"SignalR"** ibaresi **"WebSocket (STOMP)"** ile değiştirildi.

---

## Değerlendirilen seçenekler

### A · ASP.NET Core + SignalR (taslaktaki varsayım)
SignalR'ı olduğu gibi kullanabilmenin tek yolu. **Reddedildi:** backend dili Java olarak kararlaştırıldı; dil kararı SignalR'a uyum sağlamak için değiştirilmez. Araç, kararı yönetmez.

### B · Java + ham WebSocket API (`jakarta.websocket`)
Protokolü doğrudan kullanmak. **Reddedildi:** kanal/abonelik (topic) mantığı, oturum yönetimi, yayın (broadcast) ve yeniden bağlanma davranışının tamamı elle yazılırdı. STOMP bunların hepsini hazır getirir. Staj süresinde elde edilecek kazanç, harcanacak zamanı karşılamaz.

### C · Java + Socket.IO (netty-socketio)
Socket.IO'nun resmî olmayan Java sunucu uygulaması. **Reddedildi:** Spring ekosisteminin dışında, güvenlik entegrasyonu (Spring Security ile oturum paylaşımı) elle kurulmalı, topluluk desteği ve sürüm devamlılığı belirsiz.

### D · Kısa aralıklı sorgulama (polling)
İstemcinin her 1–2 saniyede bir fiyatı sorması. **Reddedildi:** 1000 izleyicili bir artırmada saniyede 1000 gereksiz HTTP isteği demektir; bunların %99'u "değişiklik yok" döner. Ayrıca `BR-B-009`'un 1 saniyelik hedefini ancak sunucuyu boş yere zorlayarak tutturur.

### E · Server-Sent Events (SSE)
Tek yönlü sunucu→istemci akışı. **Reddedildi (ama yakın rakipti):** Teklif yayını gerçekten tek yönlüdür, dolayısıyla SSE teknik olarak yeterliydi ve daha basittir. Ancak ileride site içi mesajlaşma (`BR-N-005` sonrası) ve "şu an kaç kişi izliyor" gibi çift yönlü özellikler planlanabilir; WebSocket bu kapıyı açık bırakır. Ayrıca STOMP'un kanal soyutlaması SSE'de yoktur.

---

## Sonuçlar

### Olumlu
- Yığının tamamı tek ekosistemde (Spring); güvenlik, veri erişimi ve WebSocket aynı yapılandırma altında çalışır
- STOMP kanal mantığı (`/topic/auction/{id}`) doğrudan iş modeline oturur: her artırma bir kanaldır
- Java 21 **virtual thread** desteğiyle gelir; teklif yolundaki bloklayan veritabanı çağrıları için thread havuzu baskısını azaltır
- Spring Security ile WebSocket el sıkışması (handshake) aynı JWT doğrulamasını kullanır — ikinci bir kimlik doğrulama mekanizması yazmaya gerek kalmaz
- Testcontainers ve JPA kilitleme anotasyonları hazır gelir; eşzamanlılık testleri için ek altyapı gerekmez

### Olumsuz / bedeller
- SignalR'ın istemci tarafındaki otomatik yeniden bağlanma davranışı hazır gelmez; `@stomp/stompjs` ile yeniden bağlanma **elle yapılandırılmalıdır**
- Spring Boot'un öğrenme eğrisi ilk hafta hissedilir (dependency injection, anotasyon tabanlı yapılandırma)
- Birden fazla sunucu kopyası çalıştığında STOMP yayınının tüm kopyalara ulaşması için harici bir mesaj aracısı (RabbitMQ/Redis relay) gerekir. **v1'de tek kopya çalışacağı için bu ertelenmiştir** ve bilinen bir sınırdır

### Doğrulanacak
- WebSocket bağlantısı kurumsal güvenlik duvarlarının arkasından geçemezse SockJS geri düşüşünün gerçekten devreye girdiği test edilmeli

---

## İlgili

- `docs/01-requirements/business-rules.md` → `BR-B-009`, `BR-N-007`
- `docs/02-design/concurrency-design.md` → §7 Outbox Yayıncısı
- [ADR-0002](ADR-0002-postgresql-ve-flyway.md) — veritabanı seçimi
