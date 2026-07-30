# ADR-0005 · Kategori Özellikleri İçin Tipli Kolonlu EAV

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |

---

## Bağlam

`BR-C-003` şunu gerektiriyor:

> Her kategori kendine özel alanlar tanımlayabilir. Alan eklemek için **kod değişikliği veya veritabanı migration'ı gerekmez** — admin panelinden tanımlanır.

Somut ihtiyaç: Otomobilin "kilometre"si, telefonun "hafıza"sı, dairenin "oda sayısı" var. Bunların hiçbiri diğerinde anlamlı değil. Üstelik platform büyüdükçe yeni kategoriler eklenecek ve her biri kendi alanlarını getirecek.

Ayrıca bu alanlar üzerinde **filtreleme** yapılabilmeli (`BR-C-005`): "100.000–200.000 km arası, dizel, otomatik" gibi.

---

## Karar

**EAV (Entity-Attribute-Value)** deseni kullanılacak — ancak klasik biçimiyle değil, **tipli değer kolonlarıyla**.

```
categories → attribute_definitions → listing_attribute_values
                     ↓
              attribute_options   (ENUM tipli alanlar için)
```

`listing_attribute_values` tablosu tek bir `value TEXT` kolonu yerine **beş tipli kolon** taşır:

```sql
value_text     TEXT,
value_number   NUMERIC(18,4),
value_bool     BOOLEAN,
value_date     DATE,
option_id      UUID REFERENCES attribute_options(id)

CONSTRAINT ck_lav_exactly_one_value CHECK (
    num_nonnulls(value_text, value_number, value_bool, value_date, option_id) = 1
)
```

---

## Değerlendirilen seçenekler

### A · Her kategori için sabit kolonlar
`listings` tablosuna `mileage_km`, `storage_gb`, `room_count`... eklemek.

**Reddedildi.**
- 20 kategori × ortalama 6 alan = **120 kolon**, her satırda %95'i `NULL`
- Yeni kategori = yeni migration = yeni deploy → `BR-C-003` doğrudan ihlal edilir
- PostgreSQL'in 1600 kolon sınırına uzun vadede yaklaşılır
- Tablo okunamaz hale gelir

### B · Kategori başına ayrı tablo (`car_listings`, `phone_listings`, ...)
**Reddedildi.** Sabit kolonların tüm sorunlarını taşır, üstelik:
- Kategoriler arası arama ("İstanbul'daki tüm ilanlar") N tablolu `UNION` gerektirir
- Her yeni kategori yeni tablo **ve** yeni kod yolu demektir
- Ortak alanlar (başlık, fiyat) her tabloda tekrarlanır

### C · Klasik EAV — tek `value TEXT` kolonu
EAV'ın en yaygın (ve en çok eleştirilen) biçimi.

**Reddedildi — filtreleme çöker.**

```sql
-- "100.000-200.000 km arası" sorgusu:
WHERE CAST(value AS NUMERIC) BETWEEN 100000 AND 200000
```

| Sorun | Sonuç |
|---|---|
| `CAST` index'i kullanılamaz kılar | Her filtre **tüm tabloyu tarar** (Seq Scan) |
| Tek bozuk satır (`value = 'bilinmiyor'`) | `CAST` çalışma zamanında hata verir, **tüm sorgu patlar** |
| Sıralama metin sıralamasıdır | `'9'` > `'100'` — sayısal sıralama imkânsız |
| Tip doğrulaması yok | Sayı alanına metin yazılabilir |

EAV'ın kötü şöhretinin **asıl kaynağı** budur — desenin kendisi değil, bu uygulama biçimi.

### D · JSONB kolonu
`listings` tablosuna tek bir `attributes JSONB` kolonu.

**Reddedildi (en yakın rakip).** Gerçekten cazip:
- Yazması çok hızlı, şema esnek
- GIN index ile filtrelenebilir
- Sorgular okunabilir: `attributes @> '{"fuel_type": "DIESEL"}'`

Ama karşılanamayan gereksinimler:

| Gereksinim | JSONB ile durum |
|---|---|
| `BR-C-004` zorunlu alan denetimi | Veritabanı bilemez; tamamen uygulama katmanına kalır |
| Alan tanımının kendisi veri olmalı | Admin panelinin "hangi alanlar var?" sorusunu sorabileceği bir yer yok — ayrı bir tanım tablosu yine gerekir |
| `ENUM` seçeneklerinde referans bütünlüğü | Yok. `"fuel_type": "dizel"` ve `"DIESEL"` birlikte var olabilir; veri günden güne kirlenir |
| Aralık filtresi tip güvenliği | JSONB'de sayı metin olarak da yazılabilir; `@>` operatörü tip ayrımı yapmaz |
| Alan yeniden adlandırma | Tüm satırlarda JSON güncellemesi gerekir; EAV'da tek satırlık `UPDATE` |

**Özet:** JSONB, şema tanımının **veri olması** gerekmeyen durumlarda mükemmeldir. Burada tanımın kendisi (hangi kategori hangi alanı sorar, zorunlu mu, seçenekleri neler) yönetilebilir veri olmak zorundadır — ve o zaman EAV'ın tanım tarafı zaten yazılmış olur.

---

## Sonuçlar

### Olumlu
- `BR-C-003` tam olarak karşılanır: yeni kategori ve yeni alan, **admin panelinden, deploy'suz** eklenir
- Tipli kolonlar sayesinde aralık filtreleri **index kullanır**. Bu iddia 20.000 satırlık veriyle **ölçülerek doğrulandı** (`docs/02-design/schema-verification.md` §3.1):

  | Tasarım | Plan | Süre | Taranan satır |
  |---|---|---|---|
  | **Tipli kolon** (bu ADR) | `Bitmap Index Scan on ix_lav_number` | **0,318 ms** | 972 |
  | Klasik EAV (`value TEXT` + `CAST`) | `Seq Scan` | **3,066 ms** | **20.000** |

  20.000 satırda fark 10 kat. Ölçek büyüdükçe klasik EAV doğrusal olarak yavaşlarken tipli kolon sabit kalır — çünkü biri index kullanır, diğeri kullanamaz.
- `NUMERIC` kolona sayı olmayan değer **zaten girilemez** — tip güvenliği veritabanından gelir
- `option_id` yabancı anahtarı sayesinde `ENUM` değerleri referans bütünlüğüne sahiptir; yazım hatası veya varyant değer imkânsızdır
- `ck_lav_exactly_one_value` kısıtı, iki farklı kolonda çelişkili değer saklanmasını engeller
- `BR-C-006` (alt kategorinin üst kategorinin alanlarını devralması) `categories.path` üzerinden tek sorguyla çözülür

### Olumsuz / bedeller
- **Bir ilanın tüm özelliklerini okumak N satır okumaktır** — sabit kolonlarda tek satırdı. `ix_lav_listing` bunu ucuzlatır; ilan detay sayfası zaten tek ilan gösterir, N ≈ 6'dır
- **Çoklu filtre = çoklu birleştirme.** "Dizel VE otomatik VE 100-200 bin km" sorgusu üç ayrı satır eşleşmesi gerektirir. Çözüm: `INTERSECT` veya `GROUP BY listing_id HAVING count(*) = :filterCount` deseni
- Sorgular sabit kolonlara göre daha karmaşıktır; bu karmaşıklık bir repository katmanında kapsüllenmelidir — servis kodu ham EAV sorgusu yazmamalıdır
- Zorunlu alan denetimi (`BR-C-004`) yine de servis katmanındadır; veritabanı "bu ilanda şu alan eksik" diyemez

### Performans sınırı ve ileri adım
Kategori arama sayfası çoklu EAV birleştirmesiyle yavaşlarsa, çözüm şemayı değiştirmek değil, **arama için ayrı bir okuma modeli** kurmaktır (örn. Elasticsearch veya materialized view). Yazma modeli (EAV) doğruluk kaynağı olarak kalır. Bu, v1 kapsamında **gerekli değildir** ve ölçüm yapılmadan girişilmeyecektir.

---

## İlgili

- `docs/02-design/data-model.md` → §4
- `db/migration/V2__categories_and_attributes.sql`, `V3__listings_and_images.sql`
- `docs/01-requirements/business-rules.md` → `BR-C-003`, `BR-C-004`, `BR-C-005`, `BR-C-006`
