# TıklaSat — Sistem Mimarisi

| | |
|---|---|
| **Doküman** | Sistem Mimarisi (C4 Modeli) |
| **Sürüm** | 1.0 |
| **Tarih** | 2026-07-30 |
| **İlgili ADR** | `ADR-0001`, `ADR-0002`, `ADR-0004` |

---

## C4 modeli nedir?

Yazılım mimarisini **dört zoom seviyesinde** anlatma yöntemi. Bir haritanın ülke → şehir → mahalle → sokak görünümleri gibi:

| Seviye | Sorusu | Okuyucusu |
|---|---|---|
| **1 · Context** | Sistem dış dünyayla nasıl konuşur? | Herkes (teknik olmayanlar dahil) |
| **2 · Container** | Hangi çalıştırılabilir parçalar var? | Geliştiriciler, DevOps |
| **3 · Component** | Bir parçanın içi nasıl bölünmüş? | Geliştiriciler |
| **4 · Code** | Sınıflar ve ilişkileri | (Çizilmez — kod zaten kendisi anlatır) |

Bu doküman ilk üç seviyeyi kapsar.

---

## Seviye 1 · Context

```mermaid
graph TB
    Ziyaretci["👤 Ziyaretçi<br/><i>Kimlik doğrulaması yok</i><br/>İlanları görüntüler, arar"]
    Alici["👤 Alıcı<br/>Teklif verir, ilan takip eder"]
    Satici["👤 Satıcı<br/>İlan açar, artırma başlatır"]
    Admin["👤 Admin / Moderatör<br/>İlan onaylar, artırma iptal eder"]

    TiklaSat["<b>TıklaSat</b><br/>Açık Artırma Platformu<br/><i>İlan yayınlama, gerçek zamanlı<br/>teklif verme, kazanan belirleme</i>"]

    Eposta["✉️ E-posta Sağlayıcı<br/><i>SMTP</i><br/>Doğrulama, bildirim"]
    SMS["📱 SMS Sağlayıcı<br/>Telefon doğrulama"]
    Depo["🗄️ Nesne Depolama<br/><i>S3 uyumlu</i><br/>İlan fotoğrafları"]

    Ziyaretci -->|HTTPS| TiklaSat
    Alici -->|HTTPS + WSS| TiklaSat
    Satici -->|HTTPS| TiklaSat
    Admin -->|HTTPS| TiklaSat

    TiklaSat -->|SMTP| Eposta
    TiklaSat -->|HTTPS| SMS
    TiklaSat -->|HTTPS| Depo
```

### Kapsam sınırı

| İçeride | Dışarıda (`BR-N-005`) |
|---|---|
| İlan yayınlama ve moderasyon | Ödeme altyapısı |
| Gerçek zamanlı teklif motoru | Emanet (escrow) |
| Kazanan belirleme | Kargo ve teslimat takibi |
| İletişim bilgisi ifşası | Fatura kesimi |
| Karşılıklı puanlama | |

> **Neden ödeme dışarıda?** Kapsamı ikiye katlar ve teklif motorunun kalitesinden feragat gerektirirdi. Platform kazananı belirler ve tarafları buluşturur; ticaretin geri kalanı platform dışında yürür — sahibinden.com modelinin aynısı.

---

## Seviye 2 · Container

```mermaid
graph TB
    subgraph İstemci
        SPA["🌐 Web Uygulaması<br/><i>React / Vue</i><br/>Responsive arayüz<br/>@stomp/stompjs ile canlı teklif akışı"]
    end

    subgraph "Sunucu · Spring Boot 3.x / Java 21"
        API["⚙️ REST API<br/><i>Spring Web MVC</i><br/>İlan, teklif, kimlik uçları"]
        WS["🔌 WebSocket Sunucusu<br/><i>Spring WebSocket + STOMP</i><br/>/topic/auction/{id}"]
        JOBS["⏱️ Zamanlanmış İşler<br/><i>Spring Scheduler + ShedLock</i><br/>Kapatma · Outbox · Temizlik"]
    end

    subgraph Veri
        PG[("🐘 PostgreSQL 16<br/><i>Tek doğruluk kaynağı</i><br/>25 tablo")]
        REDIS[("⚡ Redis<br/>Hız sınırlama sayaçları<br/>Önbellek")]
        S3["🗄️ Nesne Depolama<br/>İlan fotoğrafları"]
    end

    SPA -->|"HTTPS / JSON"| API
    SPA -->|"WSS / STOMP"| WS
    SPA -->|"HTTPS (imzalı URL)"| S3

    API -->|JDBC| PG
    API -->|Bucket4j| REDIS
    API -->|S3 API| S3

    JOBS -->|JDBC| PG
    JOBS -.->|"outbox → yayın"| WS

    WS -->|JDBC| PG
```

### Container'lar

| Container | Teknoloji | Sorumluluk |
|---|---|---|
| **Web Uygulaması** | React/Vue + `@stomp/stompjs` | Arayüz, canlı teklif akışı, responsive tasarım (`BR-S-009`) |
| **REST API** | Spring Web MVC | İş mantığı, doğrulama, yetkilendirme |
| **WebSocket Sunucusu** | Spring WebSocket + STOMP | Artırma başına kanal yayını (`BR-B-009`) |
| **Zamanlanmış İşler** | Spring Scheduler + ShedLock | Artırma kapatma (`BR-A-007`), outbox yayını, veri temizliği (`BR-K-005`) |
| **PostgreSQL** | 16 | **Tek doğruluk kaynağı.** Tüm iş verisi ve kısıtlar |
| **Redis** | 7 | Hız sınırlama sayaçları (`BR-S-002`), kategori/kademe önbelleği |
| **Nesne Depolama** | S3 uyumlu | Fotoğraflar. Görsel **asla** veritabanında saklanmaz |

> **Neden hepsi tek Spring Boot uygulaması?** REST API, WebSocket ve zamanlanmış işler mimari olarak ayrılabilirdi (mikroservis). **Ayrılmadı:** üçü de aynı veriye erişiyor, aynı transaction sınırlarını paylaşıyor ve toplam trafik tek bir uygulamanın kapasitesinin çok altında. Mikroservise bölmek, kazanç getirmeden dağıtık sistem problemlerini (ağ hataları, dağıtık transaction, servis keşfi) davet ederdi.
>
> **Modüler monolit** yaklaşımı benimsenmiştir: paketler alan sınırlarına göre ayrılır, ancak tek süreçte çalışır. İleride bir alan gerçekten ayrı ölçeklenmeye ihtiyaç duyarsa, sınır zaten çizilmiş olur.

### Redis neden var, neden PostgreSQL yetmiyor?

Hız sınırlama sayacı her istekte güncellenir. Bunu PostgreSQL'de tutmak, her HTTP isteğine bir disk yazması eklemek demektir — teklif yolunun kilit bütçesiyle rekabet eder. Ayrıca sayaçlar **kalıcı olmak zorunda değildir**: Redis yeniden başlarsa sayaçlar sıfırlanır, bu kabul edilebilir bir davranıştır.

**İlke:** Kalıcılığı zorunlu olmayan, çok sık yazılan veri PostgreSQL'e konmaz.

---

## Seviye 3 · Component (REST API'nin içi)

```mermaid
graph TB
    subgraph "Web Katmanı"
        AC["AuthController"]
        LC["ListingController"]
        BC["BidController"]
        AdC["AdminController"]
        SC["SearchController"]
    end

    subgraph "Servis Katmanı · İş Mantığı"
        AuthS["AuthService<br/><i>Argon2id, JWT, token rotasyonu</i>"]
        LS["ListingService<br/><i>Durum makinesi, moderasyon</i>"]
        BS["<b>BidService</b><br/><i>⚠ Pesimistik kilit · sıcak yol</i>"]
        IS["BidIncrementService<br/><i>Kademe hesabı</i>"]
        ACS["AuctionClosingService<br/><i>SKIP LOCKED · kazanan belirleme</i>"]
        NS["NotificationService"]
        OS["OutboxService"]
        AudS["AuditService"]
    end

    subgraph "Veri Erişim Katmanı"
        UR["UserRepository"]
        LR["ListingRepository"]
        AR["<b>AuctionRepository</b><br/><i>@Lock(PESSIMISTIC_WRITE)</i>"]
        BR["BidRepository"]
        OR["OutboxRepository"]
    end

    PG[("PostgreSQL")]

    AC --> AuthS
    LC --> LS
    BC --> BS
    AdC --> LS
    AdC --> AudS
    SC --> LR

    BS --> IS
    BS --> OS
    BS --> AR
    BS --> BR
    ACS --> AR
    ACS --> BR
    ACS --> OS
    LS --> LR
    LS --> AudS
    AuthS --> UR
    NS --> OR
    OS --> OR

    UR --> PG
    LR --> PG
    AR --> PG
    BR --> PG
    OR --> PG

    style BS fill:#ff6b6b,color:#fff
    style AR fill:#ff6b6b,color:#fff
```

### Katman kuralları

| Kural | Gerekçe |
|---|---|
| Controller **iş mantığı içermez** | Test edilebilirlik; mantık HTTP'den bağımsız olmalı |
| Transaction sınırı **servis katmanındadır** | Controller'da `@Transactional` = HTTP serileştirmesi transaction içinde kalır, kilit süresi uzar |
| Repository **iş kuralı bilmez** | Sorgu ve kilitleme dışında sorumluluğu yoktur |
| Entity'ler **API'ye sızmaz** | DTO kullanılır; `auctions.reserve_price` gibi gizli alanlar (`BR-A-003`) kazara döndürülemez |
| `BidService` dışındaki hiçbir servis `auctions` satırını **kilitlemez** | Deadlock riskini sıfırda tutar |

### Paket yapısı

```
com.tiklasat
├── auth/          · kimlik, JWT, roller
├── user/          · kullanıcı profili, puanlama
├── catalog/       · kategoriler, EAV tanımları
├── listing/       · ilan, fotoğraf, moderasyon
├── auction/       · ⚠ artırma, teklif, kapatma  ← sıcak yol
│   ├── domain/
│   ├── service/       BidService, AuctionClosingService, BidIncrementService
│   ├── repository/    @Lock burada
│   └── web/
├── notification/  · bildirim, outbox yayıncısı
├── search/        · tam metin + EAV filtreleme
├── admin/         · şikayet, denetim, yönetim
└── common/        · ortak yapılandırma, hata yönetimi, saat (Clock)
```

> `auction` paketi projenin **kalbidir** ve en yoğun test kapsamına sahip olmalıdır.

---

## Gerçek Zamanlı Akış

```mermaid
sequenceDiagram
    participant U as Tarayıcı
    participant API as REST API
    participant DB as PostgreSQL
    participant P as OutboxPublisher
    participant WS as WebSocket

    U->>WS: SUBSCRIBE /topic/auction/{id}
    Note over WS: JWT, el sıkışmada doğrulanır

    U->>API: POST /api/auctions/{id}/bids
    API->>DB: BEGIN · SELECT FOR UPDATE
    API->>DB: INSERT bid · UPDATE auction · INSERT outbox
    API->>DB: COMMIT
    API-->>U: 201 Created

    Note over P: her 200 ms
    P->>DB: SELECT outbox WHERE published_at IS NULL
    P->>WS: convertAndSend(/topic/auction/{id})
    WS-->>U: {currentPrice, endsAt, bidCount, serverTime}
    Note over U: Geri sayım serverTime ile<br/>senkronlanır (BR-A-011)
```

**Neden yayın API'den doğrudan yapılmıyor?** Transaction geri alınırsa kullanıcılar var olmayan bir teklif görürdü; yayın kaybolursa ekranlar donuk kalırdı. Outbox her ikisini de çözer (`BR-N-007`, `ADR-0004` ilgili bölüm).

---

## Dağıtım (v1)

```mermaid
graph LR
    subgraph "Uygulama Sunucusu"
        APP["Spring Boot<br/><i>tek kopya</i>"]
    end
    subgraph "Veri"
        PG[("PostgreSQL 16")]
        RD[("Redis 7")]
    end
    NGINX["Nginx<br/>TLS sonlandırma<br/>statik dosyalar"]
    S3["Nesne Depolama"]

    Kullanici["👤"] -->|HTTPS/WSS| NGINX
    NGINX --> APP
    NGINX --> S3
    APP --> PG
    APP --> RD
```

### v1 bilinçli sınırları

| Sınır | Neden kabul edildi | Ne zaman aşılır |
|---|---|---|
| Tek uygulama kopyası | STOMP yayını çoklu kopyada mesaj aracısı gerektirir | Trafik tek sunucuyu doldurunca → RabbitMQ STOMP relay |
| Tek veritabanı düğümü | Yazma yolu bölünemez; okuma yükü henüz düşük | Okuma yükü artınca → okuma replikası |
| Yedekleme: günlük tam yedek + WAL arşivi | Veri kaybı toleransı v1 için yeterli | Sıfır kayıp gerekirse → senkron replikasyon |

> ShedLock ve `SKIP LOCKED` **şimdiden** kurulmuştur (`V7`, `ADR-0004`). Tek kopya çalışırken gerekli değiller; ikinci kopya eklendiği gün **kod değişikliği olmadan** doğru çalışsın diye baştan konmuşlardır. Bu, ucuz ve geri dönüşü yüksek bir hazırlıktır.

---

## Kalite Nitelikleri

| Nitelik | Hedef | Nasıl sağlanıyor |
|---|---|---|
| **Doğruluk** | Yarış koşulundan veri bozulması = 0 | Pesimistik kilit + `UNIQUE` + `CHECK` (üç katman) |
| **Gerçek zamanlılık** | Yayın gecikmesi p95 < 1 sn | WebSocket + outbox (200 ms anketleme) |
| **Güvenlik** | Argon2id, JWT, hız sınırlama, metot seviyesi yetki | Spring Security + Bucket4j |
| **Denetlenebilirlik** | Her kritik işlem izlenebilir | `audit_logs`, `auction_extensions`, `contact_disclosures` |
| **Bakım yapılabilirlik** | Kararlar gerekçesiyle kayıtlı | ADR'ler + izlenebilirlik matrisi |
| **KVKK uyumu** | Anonimleştirme, ifşa izi, saklama süreleri | `BR-K-001..006` |

---

## İlgili

- [ADR-0001](../03-decisions/ADR-0001-java-spring-boot-stack.md) — yığın seçimi
- [ADR-0002](../03-decisions/ADR-0002-postgresql-ve-flyway.md) — veritabanı ve şema yönetimi
- [ADR-0004](../03-decisions/ADR-0004-pesimistik-kilit.md) — kilitleme stratejisi
- [data-model.md](data-model.md) — 25 tablonun veri sözlüğü
- [concurrency-design.md](concurrency-design.md) — teklif motorunun tam akışı
