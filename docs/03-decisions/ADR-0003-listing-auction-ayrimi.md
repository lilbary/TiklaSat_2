# ADR-0003 · `listings` ve `auctions` Ayrı Tablolar

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |

---

## Bağlam

Her ilanın tam olarak bir açık artırması vardır (`BR-A-001`) — yani aralarında **1—1** ilişki var. Veri modelleme geleneğinde 1—1 ilişkiler genellikle tek tabloda birleştirilir; ayrı tutmak "gereksiz normalizasyon" olarak eleştirilir.

Ancak bu iki varlığın **yazma profilleri** taban tabana zıt:

| | İlan içeriği | Artırma durumu |
|---|---|---|
| Ne içerir | Başlık, 3000 karakterlik açıklama, kategori, konum, arama vektörü | Güncel fiyat, bitiş zamanı, teklif sayısı |
| Ne zaman yazılır | İlan oluşturulurken ve düzenlenirken | **Her teklifte** |
| Yazma sıklığı | Ömür boyu birkaç kez | Son saniyelerde onlarca kez/saniye |
| Satır genişliği | Geniş (metin + `tsvector`) | Dar (sayı + zaman damgası) |

---

## Karar

`listings` (içerik) ve `auctions` (mekanizma) **ayrı tablolar** olarak modellenecek; ilişki `auctions.listing_id` üzerindeki `UNIQUE` kısıtıyla 1—1 olarak kurulacak.

---

## Gerekçe: MVCC ve kilit çekişmesi

Gerekçe okuma performansı değil, **yazma yolundaki kilit süresidir**.

PostgreSQL, MVCC (çok sürümlü eşzamanlılık denetimi) gereği bir satırı güncellerken **satırın tamamının yeni bir sürümünü yazar** — yalnızca değişen kolonu değil.

Tek tabloda birleştirilseydi, her `UPDATE current_price = ...` işlemi:

1. 3000 karakterlik `description` alanını yeniden yazardı
2. `search_vector` (`tsvector`) alanını yeniden yazardı
3. `search_vector` üzerindeki **GIN index'ini** güncellemek zorunda kalırdı — GIN index güncellemesi B-tree'ye göre belirgin şekilde pahalıdır
4. Bunların tamamı, **kilit tutulurken** gerçekleşirdi

Kilit süresi, teklif yolunda doğrudan bir **bütçedir** (`concurrency-design.md` §8: hedef < 10 ms). Bekleyen her isteğin gecikmesi bu süreye eşittir. Sıcak satırı dar tutmak, o bütçenin korunmasının en doğrudan yoludur.

### Sayısal karşılaştırma (tahmini)

| | Birleşik tablo | Ayrık tablo |
|---|---|---|
| Teklif başına yazılan satır boyutu | ~3.5 KB | ~200 bayt |
| Teklif başına güncellenen index | GIN + 4 B-tree | 2 B-tree |
| Tahmini kilit tutma süresi | 15–40 ms | **< 10 ms** |
| Tek artırmada teorik teklif verimi | ~25–60/sn | **~100+/sn** |

> Bu değerler ölçüm değil tahmindir; Deployment aşamasındaki yük testinde doğrulanacaktır.

---

## Değerlendirilen seçenekler

### A · Tek tablo (`listings` içinde fiyat/zaman kolonları)
**Reddedildi.** Yukarıdaki MVCC gerekçesi. Ek olarak, ilan içeriği ile ticaret mekanizması kavramsal olarak da farklı alanlardır: biri moderasyon süreçlerine, diğeri fiyat mantığına aittir. Tek tabloda birleşince her iki alanın değişiklikleri aynı satırda çatışır.

### B · Ayrı tablo + `current_price`'ı hiç saklamama (her seferinde `MAX(amount)`)
**Reddedildi.** Denormalizasyondan tamamen kaçınma girişimi. Ancak:
- Her ilan görüntülemesinde `bids` üzerinde toplama (aggregate) sorgusu çalışırdı
- Teklif doğrulaması için de aynı sorgu gerekirdi — ve kilit **hangi satır** üzerinde alınacaktı? Kilitlenecek tek bir "artırma durumu" satırı olmadan pesimistik kilit uygulanamazdı
- Yani bu seçenek yalnızca yavaş değil, **eşzamanlılık tasarımını imkânsız kılıyordu**

### C · Ayrı tablo + `auctions`'ı da ikiye bölme (statik/dinamik)
`start_price`, `starts_at` gibi hiç değişmeyen alanları da ayırmak. **Reddedildi:** kazanç marjinal (satır zaten dar), maliyet ise her okumada fazladan bir birleştirme (join). Optimizasyonun da bir doyum noktası vardır.

---

## Sonuçlar

### Olumlu
- Teklif yolundaki kilit tutma süresi minimumda kalır
- `listings.search_vector` üzerindeki GIN index'e teklif başına dokunulmaz
- Moderasyon alanı (ilan içeriği) ile ticaret alanı (fiyat/zaman) kavramsal olarak ayrışır
- İleride "sabit fiyatlı ilan" gibi farklı bir satış türü eklenirse, `listings` değişmeden yeni bir mekanizma tablosu eklenebilir

### Olumsuz / bedeller
- İlan detay sayfası **bir birleştirme (join)** gerektirir. `auctions.listing_id` üzerindeki `UNIQUE` index sayesinde bu birleştirme birincil anahtar araması kadar ucuzdur — kabul edilebilir bir bedeldir
- İlan + artırma oluşturma **iki `INSERT`** gerektirir; ikisi de aynı transaction'da yapılmalıdır (aksi halde artırmasız ilan oluşabilir)
- Uygulama katmanında iki entity ve iki repository → biraz daha fazla kod

### Uygulanacak koruma
İlan oluşturma servisi `@Transactional` olmalıdır. `auctions.listing_id` üzerindeki `NOT NULL + UNIQUE` kısıtı, artırmanın ilansız var olamayacağını garanti eder; ancak **artırmasız ilan** (yalnızca `listings` satırı) yapısal olarak mümkündür ve servis katmanında engellenmelidir. Bu, tasarımın bilinen ve kabul edilen tek boşluğudur.

---

## İlgili

- `docs/02-design/data-model.md` → §6.0
- `docs/02-design/concurrency-design.md` → §8 Kilit Süresi Bütçesi
- `db/migration/V4__auctions_and_bids.sql`
