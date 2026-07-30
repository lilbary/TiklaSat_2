# TıklaSat

Açık artırma platformu — staj projesi.

Kullanıcılar ilan açar, diğer kullanıcılar gerçek zamanlı teklif verir, süre sonunda en yüksek teklif kazanır. Kazanan belirlendikten sonra taraflar iletişime geçer.

**Mevcut aşama:** Sistem Mimarisi ve Veritabanı Tasarımı ✅
**Sonraki aşama:** Backend Geliştirme (API + Bidding Engine)

---

## Nereden başlamalı?

Projeye yeni bakan biri şu sırayla okumalı:

| # | Doküman | Ne anlatıyor |
|---|---|---|
| 1 | [Sözlük](docs/01-requirements/glossary.md) | Terimler — sniping, EAV, pesimistik kilit, outbox... |
| 2 | [İş Kuralları](docs/01-requirements/business-rules.md) | 60 numaralı kural. Sistemin **ne** yapması gerektiği |
| 3 | [Mimari](docs/02-design/architecture.md) | C4 modeli — sistem dış dünyayla ve kendi içinde nasıl çalışıyor |
| 4 | [Veri Modeli](docs/02-design/data-model.md) | 25 tablo, ERD'ler, veri sözlüğü, izlenebilirlik matrisi |
| 5 | [Eşzamanlılık Tasarımı](docs/02-design/concurrency-design.md) | **Projenin kalbi** — teklif motoru ve yarış koşulu savunması |
| 6 | [Mimari Kararlar](docs/03-decisions/) | 6 ADR — hangi karar **neden** verildi |
| 7 | [Şema Doğrulama Raporu](docs/02-design/schema-verification.md) | Şemanın çalışan PostgreSQL üzerinde test sonuçları |

> **Şema doğrulandı:** 7 migration hatasız uygulandı, 41/41 kısıt testi geçti, index kullanımı `EXPLAIN ANALYZE` ile teyit edildi. Ayrıntı: [schema-verification.md](docs/02-design/schema-verification.md)

---

## Teknoloji Yığını

| Katman | Seçim |
|---|---|
| Dil / Framework | Java 21 (LTS) · Spring Boot 3.x |
| Veritabanı | PostgreSQL 16 |
| Şema yönetimi | Flyway (`ddl-auto` **kullanılmaz**) |
| Gerçek zamanlı | Spring WebSocket + STOMP + SockJS |
| Güvenlik | Spring Security · Argon2id · JWT |
| Hız sınırlama | Bucket4j + Redis |
| Zamanlanmış işler | Spring Scheduler + ShedLock |
| Test | JUnit 5 · Testcontainers · AssertJ |

> **Not:** İlk taslakta geçen **SignalR**, Microsoft .NET'e özgüdür ve Java'da kullanılamaz. Yerini Spring WebSocket + STOMP aldı → [ADR-0001](docs/03-decisions/ADR-0001-java-spring-boot-stack.md)

---

## Dizin Yapısı

```
TiklaSat/
├── docs/
│   ├── 01-requirements/     İş kuralları, sözlük
│   ├── 02-design/           Mimari, veri modeli, eşzamanlılık
│   └── 03-decisions/        ADR'ler
└── db/migration/            Flyway SQL migration'ları (V1..V7)
```

---

## Veritabanını Kurma

```bash
docker run -d --name tiklasat-db -e POSTGRES_PASSWORD=dev -e POSTGRES_DB=tiklasat -p 5432:5432 postgres:16
```

Migration'ları uygula (Flyway CLI ile):

```bash
flyway -url=jdbc:postgresql://localhost:5432/tiklasat -user=postgres -password=dev -locations=filesystem:db/migration migrate
```

Backend geliştirme başladığında Flyway, Spring Boot açılışında migration'ları kendisi uygulayacaktır.

---

## Tasarımın Dört Ayırt Edici Kararı

**1 · Teklif motoru pesimistik kilit kullanır** ([ADR-0004](docs/03-decisions/ADR-0004-pesimistik-kilit.md))
Optimistik kilit, açık artırmanın son saniyelerinde retry fırtınası üretir. Pesimistik kilit istekleri adil biçimde sıraya dizer.

**2 · `listings` ve `auctions` ayrı tablolar** ([ADR-0003](docs/03-decisions/ADR-0003-listing-auction-ayrimi.md))
Her teklifte 3000 karakterlik açıklamayı yeniden yazmamak için. Sıcak satır dar tutulur, kilit süresi kısalır.

**3 · Sabit %5 artış yerine kademeli tablo** ([business-rules §D-1](docs/01-requirements/business-rules.md))
1.000.000 TL'lik üründe %5 → 50.000 TL sıçrama demektir ve teklif akışını öldürür.

**4 · İş kuralları veritabanında da zorlanır**
60 kuralın 38'i `CHECK`, `UNIQUE`, `EXCLUDE` veya `FOREIGN KEY` ile veritabanı seviyesinde garanti altındadır. Uygulama kodu hata yapsa bile veri bozulamaz.

---

## Kapsam Dışı (v1)

Bilinçli olarak ertelenmiştir; şema bunları engellemeyecek şekilde tasarlanmıştır:

- Ödeme, emanet (escrow), kargo takibi
- Vekil/otomatik teklif *(şema hazır: `bids.max_amount`, `bids.is_proxy`)*
- Site içi mesajlaşma
- Çoklu para birimi *(kolon hazır: `currency`)*
