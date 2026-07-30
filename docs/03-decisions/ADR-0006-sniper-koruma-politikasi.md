# ADR-0006 · Sniper Koruma Politikası

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |

---

## Bağlam

**Sniping**, açık artırmanın son saniyelerinde teklif verip diğer katılımcılara karşılık verme fırsatı bırakmama taktiğidir.

Neden bir sorun:
- Fiyatın gerçek değerine ulaşmasını engeller → satıcı zarar eder
- Kazanan, ürüne en çok değer veren değil, **en hızlı tıklayan** olur → mekanizma amacını yitirir
- Deneyimsiz kullanıcıyı sistematik olarak dezavantajlı bırakır → platformdan uzaklaşır

İlk gereksinim taslağında kural şöyleydi:

> "Son 2 dakikada gelen teklif süreyi uzatır."

**Bu cümle koda çevrilemez.** Üç soru cevapsız:
1. Süre **ne kadar** uzar?
2. Uzatma **neye eklenir** — şu ana mı, mevcut bitiş zamanına mı?
3. **Kaç kez** uzayabilir?

3. soru kritiktir: tavan konulmazsa iki inatçı katılımcı açık artırmayı teorik olarak **sonsuza kadar** sürdürebilir.

---

## Karar

| Parametre | Değer |
|---|---|
| Tetikleme penceresi | Bitişe **≤ 120 saniye** kala |
| Yeni bitiş zamanı | **`teklif_anı + 120 saniye`** |
| Uzatma sayısı tavanı | **20** |
| Toplam uzatma tavanı | **60 dakika** (orijinal bitişten itibaren) |
| Tavan davranışı | Hangisi önce dolarsa uzatma durur |
| Denetim | Her uzatma `auction_extensions` tablosuna kaydedilir |

Tavan **veritabanı seviyesinde** de zorlanır:

```sql
CONSTRAINT ck_auctions_extension_window CHECK (
    ends_at >= original_ends_at
    AND ends_at <= original_ends_at + INTERVAL '60 minutes'
),
CONSTRAINT ck_auctions_extension_count CHECK (extension_count BETWEEN 0 AND 20)
```

---

## Kritik karar: uzatma formülü

İki aday formül vardı ve aralarındaki fark ilk bakışta önemsiz görünüyor. Değil.

Senaryo: bitiş `12:00:00`, pencere ve uzatma 120 saniye.

| Teklif zamanı | `eski_bitiş + 120sn` | **`şimdi + 120sn`** ✅ |
|---|---|---|
| 11:59:00 | 12:02:00 | 12:01:00 |
| 11:59:30 | 12:04:00 | 12:01:30 |
| 11:59:50 | 12:06:00 | 12:01:50 |
| 12:00:10 | 12:08:00 | 12:02:10 |

**Sol sütunun sorunu:** Teklifler arasında yalnızca 20-30 saniye geçmesine rağmen bitiş her seferinde **tam 2 dakika** ileri atılıyor. Süre, teklif **sayısıyla** şişiyor — teklif **yoğunluğuyla** değil. 10 hızlı teklif, artırmayı 20 dakika uzatıyor.

**Sağ sütunun garantisi değişmez ve tek cümlede ifade edilebilir:**

> **Son teklifin üzerinden 120 saniye geçmeden açık artırma bitmez.**

Anti-sniping'in amacı tam olarak budur: herkesin son teklife karşılık verebileceği bir sessizlik penceresi bırakmak. Sol formül bu garantiyi vermez; sadece süreyi uzatır.

---

## Değerlendirilen seçenekler

### A · Sniper koruması hiç olmasın (eBay'in v1 davranışı)
**Reddedildi.** eBay bugün de sabit bitiş kullanır ve sniping eBay'de yaygın bir şikâyet konusudur. Yeni bir platformun bilinen bir tasarım kusurunu tekrarlaması için sebep yok.

### B · Sabit sayıda uzatma, pencere yok
"Her artırma tam 3 kez, 5'er dakika uzar." **Reddedildi:** teklif gelmese bile uzatma yapılırdı; artırmalar gereksiz yere sürerdi.

### C · `eski_bitiş + uzatma` formülü
**Reddedildi.** Yukarıdaki tablo. Süre teklif sayısıyla orantısız şişer ve öngörülemez hale gelir.

### D · Tavansız uzatma
**Reddedildi.** İki inatçı katılımcı artırmayı süresiz sürdürebilirdi. Ayrıca kapatma işinin hiç tamamlanmayan bir kaydı olması operasyonel bir problemdir.

### E · Daha uzun pencere (5 veya 10 dakika)
**Reddedildi (yakın rakip).** Daha uzun pencere daha güçlü koruma sağlar. Ancak:
- Artırmaların ne zaman biteceği belirsizleşir; kullanıcı "10 dakika daha bekleyeyim mi?" ikilemine düşer
- 60 dakikalık tavana çok daha hızlı ulaşılır (10 dk pencere × 6 uzatma = tavan)
- 120 saniye, çevrimiçi bir kullanıcının karşı teklif vermesi için yeterli süredir

**Not:** Pencere ve uzatma süresi şu an kodda sabittir. İleride kategori bazlı yapılandırma gerekirse (`Vasıta` için 5 dk, `Elektronik` için 2 dk), `bid_increment_tiers` benzeri bir yapılandırma tablosu eklenecektir. v1'de gereksiz bir esnekliktir.

---

## Sonuçlar

### Olumlu
- Sniping ekonomik olarak anlamsızlaşır: son saniye teklifi karşılıksız kalmaz
- Kural tek cümlede ifade edilebilir → kullanıcıya açıklanabilir, arayüzde gösterilebilir
- Tavan sayesinde artırmanın **en geç** ne zaman biteceği baştan bellidir (`original_ends_at + 60 dk`)
- Tavan veritabanı kısıtıyla da zorlandığı için, kodda hata olsa veya biri elle `UPDATE` çalıştırsa bile "sonsuza kadar uzayan artırma" **yapısal olarak imkânsızdır**
- `auction_extensions` tablosu sayesinde her uzatma şeffaftır; kullanıcı itirazında hangi teklifin ne zaman uzatma yaptığı satır satır gösterilebilir

### Olumsuz / bedeller
- Bitiş zamanı **değişkendir** → arayüzün geri sayımı WebSocket ile güncellenmek zorundadır; statik bir geri sayım yanlış bilgi gösterir
- `ends_at` değiştiği için `BR-A-004`'ün süre denetimi `original_ends_at` üzerinden yapılmalıdır — bu ayrım gözden kaçarsa 14 günlük sınır uzatmalarla ihlal edilmiş görünür
- Kapatma işi, kilidi aldıktan sonra `ends_at`'i **tekrar** kontrol etmelidir: sorgu ile kilit arasında bir uzatma gerçekleşmiş olabilir. Bu klasik bir "check-then-act" tuzağıdır ve `concurrency-design.md` §6.2'de açıkça ele alınmıştır
- Kullanıcı "artırma 12:00'de bitecek" diye planlayamaz; bu, korumanın kaçınılmaz bedelidir ve arayüzde "son teklifle uzayabilir" uyarısıyla telafi edilir

### Arayüz gereksinimi (UI/UX aşamasına aktarılır)
- Son 2 dakikaya girildiğinde geri sayım görsel olarak vurgulanmalı ("Uzatma sürüyor")
- Uzatma gerçekleştiğinde kullanıcıya anlık bildirim gösterilmeli
- Artırmanın kaç kez uzadığı ve tavanın nerede olduğu görünür olmalı

---

## İlgili

- `docs/01-requirements/business-rules.md` → `BR-A-006`, §D-2
- `docs/02-design/concurrency-design.md` → §5.3, §6.2
- `db/migration/V4__auctions_and_bids.sql` → `ck_auctions_extension_window`
