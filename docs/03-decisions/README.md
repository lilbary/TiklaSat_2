# Mimari Karar Kayıtları (ADR)

## ADR nedir?

**Architecture Decision Record** — önemli bir teknik kararın *bağlamını, değerlendirilen seçenekleri ve gerekçesini* tek sayfada kaydeden kısa doküman.

**Neden yazılır?** Altı ay sonra "burada neden şunu kullanmışız?" sorusunun cevabı kimsenin aklında kalmaz. Kod *ne* yaptığını gösterir, *neden* yaptığını göstermez. ADR o boşluğu doldurur.

Staj bağlamında ek bir faydası var: savunmada "bu kararı neden verdin?" sorusuna hazır, düşünülmüş bir cevap sunar.

## Format

Her ADR şu bölümleri içerir:

| Bölüm | İçerik |
|---|---|
| **Durum** | Önerildi · **Kabul edildi** · Reddedildi · Yerini aldı: ADR-XXXX |
| **Bağlam** | Hangi problem, hangi kısıtlar altında |
| **Karar** | Ne yapıldı — tek cümlede |
| **Değerlendirilen seçenekler** | Reddedilenler ve **neden** reddedildikleri |
| **Sonuçlar** | Olumlu, olumsuz ve kabul edilen bedeller |

**Altın kural:** ADR **değiştirilmez**. Karar değişirse eski ADR "Yerini aldı" olarak işaretlenir ve yeni bir ADR yazılır. Kararların tarihçesi, kararların kendisi kadar değerlidir.

## Kayıtlar

| # | Başlık | Durum | Tarih |
|---|---|---|---|
| [0001](ADR-0001-java-spring-boot-stack.md) | Java + Spring Boot yığını; SignalR yerine WebSocket/STOMP | Kabul edildi | 2026-07-30 |
| [0002](ADR-0002-postgresql-ve-flyway.md) | PostgreSQL 16 + Flyway; Hibernate `ddl-auto` kullanılmayacak | Kabul edildi | 2026-07-30 |
| [0003](ADR-0003-listing-auction-ayrimi.md) | `listings` ve `auctions` ayrı tablolar (sıcak/soğuk ayrımı) | Kabul edildi | 2026-07-30 |
| [0004](ADR-0004-pesimistik-kilit.md) | Teklif yolunda pesimistik satır kilidi | Kabul edildi | 2026-07-30 |
| [0005](ADR-0005-eav-kategori-attributelari.md) | Kategori özellikleri için tipli kolonlu EAV | Kabul edildi | 2026-07-30 |
| [0006](ADR-0006-sniper-koruma-politikasi.md) | Sniper koruması: 120 sn pencere, `şimdi+120sn`, 20/60dk tavan | Kabul edildi | 2026-07-30 |
