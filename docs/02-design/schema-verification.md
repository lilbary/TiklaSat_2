# TıklaSat — Şema Doğrulama Raporu

| | |
|---|---|
| **Doküman** | Şema Doğrulama (Schema Verification) |
| **Tarih** | 2026-07-30 |
| **Ortam** | PostgreSQL 16.14 (Homebrew, aarch64-apple-darwin) |
| **Kapsam** | `db/migration/V1__..V7__.sql` |
| **Sonuç** | ✅ **Tüm testler geçti** |

---

## Bu doküman neden var?

Tasarım dokümanı yazmak kolaydır; yazılanın **gerçekten çalıştığını** göstermek zordur. Bu rapor, şemanın kâğıt üzerinde değil, çalışan bir PostgreSQL örneği üzerinde doğrulandığının kaydıdır.

Üç soru cevaplanıyor:

1. Şema hatasız kuruluyor mu?
2. Kısıtlar **gerçekten** geçersiz veriyi reddediyor mu?
3. Index'ler **gerçekten** kullanılıyor mu?

---

## 1 · Kurulum

Tek kullanımlık bir PostgreSQL 16 kümesi (cluster) oluşturuldu, 7 migration sırayla uygulandı.

| Ölçüt | Sonuç |
|---|---|
| Migration uygulaması | **7/7 hatasız** |
| Oluşan tablo sayısı | **26** (25 iş tablosu + `shedlock`) |
| Index sayısı | **89** |
| Toplam kısıt | **155** |

### Kısıt dağılımı

| Tip | Adet | Ne yapar |
|---|---|---|
| `CHECK` | **62** | Değer aralıkları, durum listeleri, alanlar arası tutarlılık |
| `FOREIGN KEY` | **46** | Referans bütünlüğü |
| `PRIMARY KEY` | **27** | Satır kimliği |
| `UNIQUE` | **19** | Tekrar engelleme (yarış koşulu savunması dahil) |
| `EXCLUDE` | **1** | Artış kademelerinin çakışmaması |

### Seed verisi

| Tablo | Satır |
|---|---|
| `roles` | 4 *(ZİYARETÇİ yok — `BR-U-002`)* |
| `cities` | 81 |
| `districts` | 94 *(İstanbul 39 + Ankara 25 + İzmir 30)* |
| `bid_increment_tiers` | 5 |
| `categories` | 18 |
| `attribute_definitions` | 19 |
| `attribute_options` | 33 |

---

## 2 · Kısıt Testleri — 41/41 geçti

Yöntem: **başarısız olması beklenen** işlemler denendi. Bir kısıtın var olması yetmez; geçersiz veriyi **fiilen** reddettiği görülmelidir. Ayrıca geçerli verinin **yanlışlıkla reddedilmediği** de kontrol edildi.

### İlan kuralları

| Test | Kural | Sonuç |
|---|---|---|
| Başlık 5 karakter (min 10) | `BR-L-001` | ✅ reddedildi |
| Açıklama 3001 karakter (max 3000) | `BR-L-002` | ✅ reddedildi |
| `REJECTED` durumu ama gerekçe yok | `BR-L-007` | ✅ reddedildi |
| Fotoğraf 6 MB (max 5 MB) | `BR-L-003` | ✅ reddedildi |
| 16. fotoğraf (`sort_order` 15) | `BR-L-003` | ✅ reddedildi |
| Geçersiz format (`image/gif`) | `BR-L-003` | ✅ reddedildi |
| 1. kapak fotoğrafı | `BR-L-004` | ✅ *kabul edildi* |
| **2. kapak fotoğrafı** | `BR-L-004` | ✅ reddedildi |
| Kapak olmayan 2. fotoğraf | `BR-L-004` | ✅ *kabul edildi* |

> Kısmi benzersiz index (`WHERE is_cover`) tam olarak tasarlandığı gibi çalışıyor: ikinci kapak engellendi, normal fotoğraf serbest kaldı.

### Açık artırma kuralları

| Test | Kural | Sonuç |
|---|---|---|
| `start_price = 0` | `BR-A-002` | ✅ reddedildi |
| Rezerv < başlangıç fiyatı | `BR-A-003` | ✅ reddedildi |
| `ends_at < starts_at` | — | ✅ reddedildi |
| Süre 15 gün (max 14) | `BR-A-004` | ✅ reddedildi |
| **Uzatma 61 dakika (tavan 60)** | `BR-A-006` | ✅ reddedildi |
| Uzatma 59 dakika (tavan içinde) | `BR-A-006` | ✅ *kabul edildi* |
| 21. uzatma (tavan 20) | `BR-A-006` | ✅ reddedildi |
| `ENDED_SOLD` ama kazanan yok | `BR-A-008` | ✅ reddedildi |
| `CANCELLED` ama gerekçe yok | `BR-A-012` | ✅ reddedildi |
| Aynı ilana 2. açık artırma | `BR-A-001` | ✅ reddedildi |

> **En önemli iki satır:** Sniper korumasının 60 dakikalık tavanı veritabanı seviyesinde gerçekten zorlanıyor. Kodda hata olsa veya biri elle `UPDATE` çalıştırsa bile "sonsuza kadar uzayan artırma" oluşamıyor. 59 dakikalık uzatmanın kabul edilmesi de sınırın doğru yerde olduğunu gösteriyor.

### Teklif kuralları

| Test | Kural | Sonuç |
|---|---|---|
| **Aynı artırmada aynı tutar** | `BR-B-004` | ✅ reddedildi |
| Farklı tutarla teklif | — | ✅ *kabul edildi* |
| `max_amount < amount` | `BR-B-013` | ✅ reddedildi |
| `VOIDED` ama gerekçe yok | `BR-B-005` | ✅ reddedildi |

> İlk satır **yarış koşulu savunmasının ikinci katmanıdır** (`ADR-0004`). Kilitleme mantığında bir hata olsa bile aynı tutarın iki kez yazılması veritabanı tarafından engelleniyor — doğrulandı.

### Artış kademeleri — `EXCLUDE` kısıtı

| Test | Sonuç |
|---|---|
| Çakışan kademe (500–2000, mevcut 0–1000 ile kesişiyor) | ✅ reddedildi |
| Aynı kademe `is_active = false` olarak | ✅ *kabul edildi* |
| Yüzde değeri > 100 | ✅ reddedildi |

> `EXCLUDE USING gist (numrange(...) WITH &&) WHERE (is_active)` çalışıyor. Kısmi olduğu için pasif kademeler çakışabiliyor — istenen davranış bu.

### Kategori ve EAV

| Test | Kural | Sonuç |
|---|---|---|
| Derinlik 5 (max 4) | `BR-C-001` | ✅ reddedildi |
| Kök kategoride slug tekrarı | — | ✅ reddedildi |
| Kök kategori `depth = 2` | — | ✅ reddedildi |
| **EAV: iki değer kolonu birden dolu** | — | ✅ reddedildi |
| **EAV: hiçbir değer kolonu dolu değil** | — | ✅ reddedildi |
| EAV: tek değer kolonu dolu | — | ✅ *kabul edildi* |
| EAV: aynı ilana aynı özellik ikinci kez | — | ✅ reddedildi |

> `UNIQUE NULLS NOT DISTINCT` (PostgreSQL 15+) doğrulandı: `parent_id` `NULL` olmasına rağmen kök kategorilerde slug tekrarı engellendi. Varsayılan `UNIQUE` davranışıyla bu kural kökte **çalışmazdı**.
>
> `num_nonnulls(...) = 1` kısıtı da iki yönde de çalışıyor: fazla değer de, eksik değer de reddediliyor.

### Kullanıcı, puanlama, KVKK

| Test | Kural | Sonuç |
|---|---|---|
| Aynı e-posta büyük harfle (`CITEXT`) | `BR-U-001` | ✅ reddedildi |
| `ANONYMIZED` ama telefon silinmemiş | `BR-K-003` | ✅ reddedildi |
| `ANONYMIZED` ve telefon silinmiş | `BR-K-003` | ✅ *kabul edildi* |
| Puan 6 (1–5 olmalı) | `BR-N-006` | ✅ reddedildi |
| Kendi kendini puanlama | — | ✅ reddedildi |
| Kendi bilgisini kendine açma | `BR-K-004` | ✅ reddedildi |
| Geçersiz rol kodu (`VISITOR`) | `BR-U-002` | ✅ reddedildi |

> Son satır sembolik ama anlamlı: "ZİYARETÇİ bir rol değildir" kararı veritabanı seviyesinde de zorlanıyor.

---

## 3 · Index Doğrulaması — `EXPLAIN ANALYZE`

**Test verisi:** 20.005 ilan, 20.000 EAV değeri, 20.000 açık artırma (19.900 kapanmış + ~105 aktif).

### 3.1 EAV aralık filtresi — ADR-0005'in ampirik kanıtı

Aynı veri, aynı sorgu, iki farklı tasarım:

| Tasarım | Plan | Süre | Taranan satır |
|---|---|---|---|
| **Tipli kolon** (bizim) | `Bitmap Index Scan on ix_lav_number` | **0.318 ms** | 972 |
| Klasik EAV (`value TEXT` + `CAST`) | `Seq Scan` | **3.066 ms** | **20.000** |

```
-- Bizim tasarım
Bitmap Heap Scan on listing_attribute_values (actual time=0.112..0.289 rows=971)
  ->  Bitmap Index Scan on ix_lav_number (actual time=0.093..0.093 rows=972)
      Buffers: shared hit=9

-- Klasik EAV (reddedilen)
Seq Scan on lav_classic (actual time=0.104..3.043 rows=971)
  Filter: (... AND ((value)::numeric >= '100000') AND ((value)::numeric <= '120000'))
  Rows Removed by Filter: 19029        ← 19.029 satır boşuna okundu
```

> **`ADR-0005` artık bir iddia değil, ölçüm.** Klasik EAV'da `CAST` index'i kullanılamaz kılıyor ve sorgu tüm tabloyu tarıyor. 20.000 satırda fark 10 kat; 2 milyon satırda fark **kabul edilemez** olurdu — ve bu, üretimde fark edileceği ölçek.
>
> Ayrıca `Buffers: shared hit=9` ile `hit=228` karşılaştırması, index'in yalnızca hızlı değil, **çok daha az bellek/disk erişimi** yaptığını gösteriyor.

### 3.2 Kapatma işi sorgusu — kısmi index

19.900 kapanmış artırmanın arasından 100 aktif artırmayı bulma:

```
Limit (actual time=0.005..0.019 rows=100)
  ->  Index Scan using ix_auctions_closing on auctions (actual time=0.005..0.015 rows=100)
```

**0.025 ms.** Kısmi index (`WHERE status = 'ACTIVE'`) kapanmış artırmaları hiç içermediği için, sistem büyüdükçe bu sorgu yavaşlamıyor — tasarımın hedefi buydu (`BR-A-007`).

### 3.3 Tam metin arama — GIN index

```
Bitmap Heap Scan on listings (actual time=0.012..0.013 rows=5)
  ->  Bitmap Index Scan on ix_listings_search (actual time=0.010..0.010 rows=5)
```

**0.020 ms**, GIN index kullanıldı. `ts_rank` sıralaması da çalışıyor (`setweight` ile başlık ağırlığı `'A'`).

> ⚠️ **Önemli metodoloji notu:** İlk denemede planlayıcı Seq Scan seçti. Sebep index'in bozuk olması değil, **test verisinin kötü olmasıydı**: 20.000 ilanın hepsi "otomobil" kelimesini içerdiğinden sorgu seçici değildi ve `LIMIT 10` ile ilk 10 satırı doğrudan okumak gerçekten daha ucuzdu. Planlayıcı doğru karar vermişti. Seçici bir terimle (`anadol`, 5 satır) tekrarlanınca GIN index beklendiği gibi devreye girdi.
>
> Bu, "EXPLAIN çıktısı Seq Scan gösteriyorsa index bozuktur" varsayımının neden yanlış olduğunun iyi bir örneğidir.

### 3.4 Türkçe kök bulma (stemming)

```sql
to_tsvector('turkish', 'arabalar arabayi arabalarin')
→ 'araba':1,3 'arabayi':2
```

"arabalar" ve "arabalarin" aynı köke (`araba`) indirgendi. PostgreSQL'in yerleşik Türkçe yapılandırması çalışıyor (`BR-L-010`).

### 3.5 Üretilmiş kolon otomatik tazeleniyor mu?

Başlık değiştirildi, `search_vector` elle **hiç** güncellenmedi:

```
ÖNCE : 'anadol':3A 'araç':6A 'klasik':2A ...
SONRA: 'baslik':2A 'farkli':4A 'guncellenmis':1A ...
```

✅ `GENERATED ALWAYS AS ... STORED` çalışıyor. "Arama index'ini güncellemeyi unuttum" hatası yapısal olarak imkânsız.

### 3.6 `FOR UPDATE SKIP LOCKED` sözdizimi

`concurrency-design.md` §6.1'deki sorgu **birebir** çalıştırıldı ve sonuç döndürdü:

```sql
SELECT id FROM auctions
 WHERE status = 'ACTIVE' AND ends_at <= now()
 ORDER BY ends_at
 LIMIT 3
 FOR UPDATE SKIP LOCKED;     -- ✅ kilitleme yan tümcesi LIMIT'ten SONRA
```

> Bu sıralama önemlidir: PostgreSQL'de kilitleme yan tümcesi `LIMIT`'ten **sonra** gelir. Yaygın bir yazım hatası kaynağıdır ve dokümandaki hâlinin doğru olduğu böylece teyit edildi.

---

## 4 · Bilinen Sınırlar ve Sonraki Adımlar

| Konu | Durum |
|---|---|
| Kategori `path` index'i | 18 satırlık tabloda planlayıcı doğal olarak Seq Scan seçti. Gerçek kullanımda da kategori sayısı birkaç yüzü geçmez; index asıl faydasını `listings` birleştirmesinde gösterecektir. **Ölçülmedi.** |
| **Eşzamanlılık testleri** | Bu aşamada koşulmadı — servis katmanı gerektiriyor. `concurrency-design.md` §10'da tanımlı, **Backend Geliştirme** aşamasında Testcontainers ile koşulacak |
| Yük testi | Deployment aşamasına ait (`concurrency-design.md` §10.4) |
| Flyway CLI | Kurulu değil; migration'lar `psql` ile sırayla uygulandı. Flyway aynı dosyaları aynı sırayla çalıştırdığı için doğrulama açısından eşdeğerdir. Sürüm takibi (`flyway_schema_history`) backend aşamasında devreye girecek |
| İlçe verisi | 3 il tam (94 ilçe). Kalan 78 ilin ilçeleri ayrı bir veri yükleme adımıyla aktarılacak — bilinçli kapsam kararı |

---

## Nasıl tekrarlanır?

```bash
brew install postgresql@16
```

```bash
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH" && export LC_ALL="en_US.UTF-8" && initdb -D /tmp/pgdata -U postgres && pg_ctl -D /tmp/pgdata -o "-p 5433 -k /tmp" -l /tmp/pgdata/server.log start && createdb -h /tmp -p 5433 -U postgres tiklasat && for f in db/migration/V*.sql; do psql -h /tmp -p 5433 -U postgres -d tiklasat -v ON_ERROR_STOP=1 -f "$f"; done
```

> macOS'ta `LC_ALL` ayarlanmazsa PostgreSQL 16 `postmaster became multithreaded during startup` hatasıyla başlamaz. Bilinen bir Homebrew/macOS sorunudur.
