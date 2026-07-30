# ADR-0002 · PostgreSQL 16 + Flyway; Hibernate `ddl-auto` Kullanılmayacak

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |

---

## Bağlam

Açık artırma platformunun veritabanından iki sıra dışı beklentisi var:

1. **Eşzamanlılık primitifleri.** Teklif motoru satır seviyesinde kilitleme (`SELECT ... FOR UPDATE`) ve kuyruk işleme (`SKIP LOCKED`) gerektiriyor (`BR-B-007`, `BR-A-007`).
2. **Karmaşık kısıtlar.** İş kurallarının veritabanı seviyesinde zorlanması hedefleniyor: aralık çakışması engelleme, koşullu benzersizlik, çok kolonlu tutarlılık.

Ayrıca Türkçe tam metin arama (`BR-L-010`), dinamik özellik filtreleme (`BR-C-005`) ve JSON gövdeli olay kaydı (outbox, denetim) gerekiyor.

Şema yönetimi tarafında ise bir tercih yapılması gerekiyordu: Hibernate'in şemayı kendisi üretmesi mi, yoksa elle yazılmış migration dosyaları mı?

---

## Karar

**PostgreSQL 16** kullanılacak. Şema **Flyway** ile, elle yazılmış SQL migration dosyalarıyla yönetilecek.

**Hibernate `spring.jpa.hibernate.ddl-auto` ayarı `validate` dışında hiçbir değere ayarlanmayacaktır.**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate   # ASLA update, create veya create-drop DEĞİL
```

---

## Değerlendirilen seçenekler

### Veritabanı

#### A · MySQL 8
**Reddedildi.** Kritik eksikler:
- `EXCLUDE` kısıtı **yok** → teklif artış kademelerinin çakışmaması veritabanında garanti edilemez, uygulama katmanına bırakılırdı
- **Kısmi index yok** (`WHERE` yan tümceli index) → "her ilanın tek kapak fotoğrafı" kuralı trigger gerektirirdi; kapatma işinin index'i tüm kapanmış artırmaları da içerir ve zamanla yavaşlardı
- Türkçe tam metin arama desteği zayıf
- `INET` gibi özel tipler yok

`SKIP LOCKED` MySQL 8'de vardır — tek başına yeterli değildir.

#### B · SQL Server
**Reddedildi.** Teknik olarak yeterli (`UPDLOCK`, `READPAST`, filtered index). Ancak lisans maliyeti var, Express sürümünün 10 GB sınırı bulunuyor ve macOS'ta yalnızca konteyner üzerinden çalışıyor. Java projesi için ekosistem uyumu da PostgreSQL'e göre zayıf.

#### C · MongoDB
**Reddedildi.** Açık artırma **son derece ilişkisel** bir alandır: kullanıcı→ilan→artırma→teklif zinciri, yabancı anahtar bütünlüğü ve çok satırlı transaction gerektirir. Belge veritabanı burada avantaj değil, kayıp getirir. Ayrıca teklif motorunun ihtiyaç duyduğu satır kilitleme semantiği çok daha zayıftır.

#### D · H2 (yalnızca geliştirme için)
**Reddedildi — özellikle testlerde.** H2'nin `FOR UPDATE` ve `SKIP LOCKED` davranışı PostgreSQL ile birebir aynı değildir. Eşzamanlılık testini H2'de koşmak testi **anlamsız** kılar: yeşil geçer, üretimde koruma çalışmaz. Testler Testcontainers ile **gerçek PostgreSQL** üzerinde koşacaktır.

### Şema yönetimi

#### E · Hibernate `ddl-auto: update`
**Reddedildi.** Öğrenci projelerinde çok yaygın, üretimde ise kabul edilemez:

| Sorun | Sonuç |
|---|---|
| Ne yaptığı görülemez | Şema değişikliği gözden geçirilemez, kod incelemesine (code review) giremez |
| Geri alınamaz | Yanlış bir değişiklik geri sarılamaz |
| Kolon **silmez**, tip **daraltmaz** | Şema zamanla ölü kolonlarla dolar |
| `CHECK`, `EXCLUDE`, kısmi index üretemez | Bu projedeki kısıtların **çoğu** hiç oluşmazdı |
| Ortamlar ayrışır | Geliştirme ve üretim şemaları sessizce farklılaşır |

Bu projenin tasarım felsefesi "iş kuralları veritabanında da zorlanır" (`data-model.md` İ-7). `ddl-auto` bu felsefeyle **temelden** çelişir.

#### F · Liquibase
**Reddedildi (yakın rakip).** Yetenek olarak Flyway'e denk, hatta XML/YAML tabanlı değişiklik günlüğüyle daha fazla soyutlama sunuyor. Ancak bu proje **ham SQL** yazmayı tercih ediyor: PostgreSQL'e özgü `EXCLUDE USING gist`, `GENERATED ALWAYS AS ... STORED`, `UNIQUE NULLS NOT DISTINCT` gibi ifadeler Liquibase'in soyutlamasında yine ham SQL bloğu olarak yazılırdı — yani soyutlamanın faydası kaybolurdu. Flyway daha basit ve bu projede daha dürüst bir araçtır.

---

## Sonuçlar

### Olumlu — PostgreSQL'in bu projede fiilen kullanılan özellikleri

| Özellik | Nerede kullanılıyor | Olmasaydı |
|---|---|---|
| `SELECT ... FOR UPDATE` | Teklif yolu | Kaybolan güncelleme kaçınılmaz |
| `SKIP LOCKED` | Kapatma işi | Çoklu sunucuda artırma iki kez kapanırdı |
| `EXCLUDE USING gist` | `bid_increment_tiers` | Çakışan kademeler eklenebilirdi |
| Kısmi index (`WHERE`) | Kapak fotoğrafı, kapatma kuyruğu, outbox, okunmamış bildirim | Trigger'lar ve yavaşlayan sorgular |
| `GENERATED ALWAYS ... STORED` | `listings.search_vector` | Arama index'ini elle güncelleme (ve unutma) riski |
| `UNIQUE NULLS NOT DISTINCT` | `categories(parent_id, slug)` | Kök kategorilerde slug tekrarı |
| `to_tsvector('turkish', ...)` | Tam metin arama | Türkçe ek yapısı için elle çözüm |
| `num_nonnulls()` | EAV değer tutarlılığı | Karmaşık `CASE` ifadeleri |
| `CITEXT` | E-posta karşılaştırması | Her sorguda `lower()` sarmalayıcısı, index kaybı |
| `INET` | IP adresleri | Metin olarak saklama, ağ sorgusu yapılamaz |
| `JSONB` | Outbox ve denetim gövdeleri | Yapısal olmayan veri için ayrı tablolar |

Bu liste, seçimin gerekçesidir: **PostgreSQL burada "varsayılan tercih" değil, gereksinimlerin dayattığı seçimdir.**

### Olumsuz / bedeller
- Yerel geliştirme için PostgreSQL kurulumu gerekir (Docker veya Homebrew) — H2 gibi "sıfır kurulum" kolaylığı yok
- Migration dosyaları **elle** yazılır; şema değişikliği Hibernate'e göre daha fazla emek ister
- Entity sınıfları ile migration'lar arasındaki uyum manuel korunur — bu yüzden `ddl-auto: validate` açık bırakılır: uyumsuzluk varsa uygulama **açılışta** hata verir, üretimde sürpriz yaşanmaz

### Kabul edilen kural
> Şemaya dokunan hiçbir değişiklik, yeni bir `V{n}__aciklama.sql` dosyası olmadan yapılmaz. Uygulanmış bir migration dosyası **asla düzenlenmez** — Flyway sağlama toplamını (checksum) doğrular ve değişmişse uygulamayı başlatmaz.

---

## İlgili

- `db/migration/V1__..V7__.sql`
- `docs/02-design/data-model.md` → §2 Tasarım İlkeleri
- [ADR-0004](ADR-0004-pesimistik-kilit.md) — kilitleme stratejisi
