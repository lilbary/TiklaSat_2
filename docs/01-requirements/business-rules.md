# TıklaSat — İş Kuralları Kataloğu

| | |
|---|---|
| **Doküman** | İş Kuralları (Business Rules) |
| **Sürüm** | 1.0 |
| **Tarih** | 2026-07-30 |
| **Aşama** | Gereksinim Analizi → Tasarım geçişi |
| **Durum** | Onaylı (tasarımın girdisi) |

---

## Bu doküman nasıl okunur?

Her kuralın benzersiz bir **kimliği** vardır (`BR-A-004` gibi). Bu kimlikler tesadüfi değildir:

- Veritabanı şemasında ve kod içinde bu kimliğe atıf yapılır (`-- BR-A-004` yorumu).
- `docs/02-design/traceability.md` içindeki izlenebilirlik matrisi her kuralın nerede uygulandığını gösterir.
- Test isimleri kural kimliğini taşır (`shouldRejectBidBelowIncrement_BR_B_003`).

**Neden böyle yapıyoruz?** Staj savunmasında "şu kuralı nerede uyguladın?" sorusu geldiğinde tek bir grep yeterli olur. Kural kimliği olmayan bir gereksinim, unutulmaya mahkûm bir gereksinimdir.

**Önek anlamları:**

| Önek | Alan |
|---|---|
| `BR-U` | Kullanıcı, kimlik, yetkilendirme |
| `BR-C` | Kategori ve dinamik özellikler |
| `BR-L` | İlan (listing) |
| `BR-A` | Açık artırma yaşam döngüsü |
| `BR-B` | Teklif verme (bidding) |
| `BR-N` | Bildirim ve iletişim |
| `BR-S` | Güvenlik (non-fonksiyonel) |
| `BR-K` | KVKK / kişisel veri |

**Zorunluluk seviyesi:** `ZORUNLU` (v1'de olmalı) · `OLMALI` (v1 hedefi, feragat edilebilir) · `İLERİ` (v2+)

---

## 0. Gereksinim Analizinde Yapılan 3 Düzeltme

Bu bölüm, ilk gereksinim taslağına göre **değişen** kararları ve gerekçelerini kayda geçirir. Staj raporunda "gereksinimler nasıl olgunlaştı?" sorusunun cevabıdır.

### D-1 · Sabit %5 teklif artışı → Kademeli artış tablosu

**İlk taslak:** "Yeni teklif, mevcut teklifin en az %5 fazlası olmalıdır."

**Tespit edilen sorun:** Oran sabit olduğu için fiyat büyüdükçe minimum sıçrama da orantısız büyür.

| Mevcut fiyat | %5 kuralıyla minimum sıçrama | Sonuç |
|---|---|---|
| 100 TL | 5 TL | Kabul edilebilir |
| 10.000 TL | 500 TL | Sınırda |
| 1.000.000 TL | **50.000 TL** | Teklif akışı durur |

1.000.000 TL'lik bir araçta kimse 50.000 TL'lik bir sıçramayı göze almaz. Açık artırmanın değer keşfi (price discovery) mekanizması çöker ve ürün gerçek değerinin altında satılır.

**Yeni kural:** Artış miktarı, mevcut fiyatın hangi **kademeye** düştüğüne göre belirlenir → `BR-B-003`.

**Geri dönülebilirlik:** Kademeler veritabanında tablo olarak durur, kodda sabit değildir. `bid_increment_tiers` tablosuna tek bir `(0, ∞, PERCENTAGE, 5)` satırı koyulursa sistem orijinal düz %5 kuralına döner. Karar geri alınabilir.

---

### D-2 · Sniper koruması eksik tanımlıydı

**İlk taslak:** "Son 2 dakikada gelen teklif süreyi uzatır."

**Tespit edilen sorun:** Bu cümle koda çevrilemez. Üç soru cevapsız:

1. Süre **ne kadar** uzar?
2. Uzatma **neye göre** hesaplanır — şu ana mı, eski bitiş zamanına mı eklenir?
3. **Kaç kez** uzayabilir?

3. soru kritiktir: tavan konulmazsa, iki inatçı katılımcı açık artırmayı teorik olarak **sonsuza kadar** sürdürebilir.

2. soru ise ince ama önemlidir. İki formül karşılaştırması — bitiş `12:00:00`, pencere ve uzatma 120 sn:

| Olay | `yeni_bitiş = eski_bitiş + 120sn` | `yeni_bitiş = şimdi + 120sn` ✅ |
|---|---|---|
| 11:59:00'da teklif | 12:02:00 | 12:01:00 |
| 11:59:30'da teklif | 12:04:00 | 12:01:30 |
| 11:59:50'de teklif | 12:06:00 | 12:01:50 |

Soldaki formül teklifler arasında sadece 30 saniye geçmesine rağmen bitişi 2'şer dakika ileri atar; süre teklif sayısıyla şişer. Sağdaki formül ise değişmez bir garanti verir: **son teklifin üzerinden 120 saniye geçmeden açık artırma bitmez.** Anti-sniping'in amacı tam olarak budur.

**Yeni kural:** → `BR-A-006`

---

### D-3 · "Ziyaretçi" bir veritabanı rolü değildir

**İlk taslak:** Roller — Ziyaretçi, Alıcı, Satıcı, Admin.

**Tespit edilen sorun:** Ziyaretçi, kimlik doğrulaması **yapılmamış** istektir. Kime ait olduğu bilinmeyen bir oturumu `users` tablosunda satır olarak tutmak anlamsızdır ve her anonim ziyaretçi için gereksiz kayıt üretir.

Ayrıca **Alıcı ve Satıcı ayrı hesap türleri değildir.** sahibinden.com'da da aynı kişi hem ilan verir hem teklif verir. Bunları ayrı hesap yapmak kullanıcıyı iki kez kayıt olmaya zorlar.

**Yeni kural:** → `BR-U-002`, `BR-U-003`

---

## 1. Kullanıcı, Kimlik ve Yetkilendirme (BR-U)

| ID | Kural | Seviye |
|---|---|---|
| **BR-U-001** | Kayıt için e-posta, parola, ad-soyad ve telefon zorunludur. E-posta sistem genelinde benzersizdir ve büyük/küçük harf duyarsız karşılaştırılır (`Ali@x.com` = `ali@x.com`). | ZORUNLU |
| **BR-U-002** | Roller: `BUYER`, `SELLER`, `ADMIN`, `MODERATOR`. **Ziyaretçi bir rol değildir** — kimlik doğrulamasının yokluğudur; veritabanında karşılığı olmaz. | ZORUNLU |
| **BR-U-003** | Bir kullanıcı **aynı anda birden fazla role** sahip olabilir. Kayıt olan herkes otomatik `BUYER` alır; ilk ilanını yayınladığında `SELLER` rolü eklenir. Ayrı "satıcı hesabı" açılmaz. | ZORUNLU |
| **BR-U-004** | E-posta doğrulanmadan teklif verilemez ve ilan yayınlanamaz. Doğrulama bağlantısı **24 saat** geçerlidir. | ZORUNLU |
| **BR-U-005** | İlan yayınlamak için ayrıca **telefon doğrulaması** gerekir. Amaç: sahte ilan maliyetini yükseltmek. | OLMALI |
| **BR-U-006** | Parola en az 10 karakter olmalı; büyük harf, küçük harf ve rakam içermelidir. En yaygın 10.000 parolayı içeren kara listeye karşı kontrol edilir. | ZORUNLU |
| **BR-U-007** | Hesap durumları: `PENDING_VERIFICATION` → `ACTIVE` → (`SUSPENDED` \| `BANNED` \| `ANONYMIZED`). Yalnızca `ACTIVE` kullanıcı teklif verebilir veya ilan açabilir. | ZORUNLU |
| **BR-U-008** | Yalnızca `ADMIN` rol atayabilir/geri alabilir. Bir admin **kendi** admin rolünü kaldıramaz (sistemde admin kalmaması riskine karşı). | ZORUNLU |
| **BR-U-009** | Kullanıcı silme talebi **anonimleştirme** ile karşılanır, kayıt fiziksel olarak silinmez. Gerekçe → `BR-K-003`. | ZORUNLU |

---

## 2. Kategori ve Dinamik Özellikler (BR-C)

| ID | Kural | Seviye |
|---|---|---|
| **BR-C-001** | Kategoriler ağaç yapısındadır (Vasıta → Otomobil → Sedan). Derinlik **en fazla 4** seviyedir. | ZORUNLU |
| **BR-C-002** | İlan yalnızca **yaprak** (alt kategorisi olmayan) kategoriye açılabilir. "Vasıta"ya değil, "Sedan"a ilan verilir. | ZORUNLU |
| **BR-C-003** | Her kategori kendine özel alanlar tanımlayabilir (Otomobil → km, model yılı, yakıt; Telefon → hafıza, renk). Alan eklemek için **kod değişikliği veya veritabanı migration'ı gerekmez** — admin panelinden tanımlanır. | ZORUNLU |
| **BR-C-004** | Bir alan zorunlu işaretlenmişse, o kategoriye ilan açılırken değeri boş bırakılamaz. | ZORUNLU |
| **BR-C-005** | Alan tipleri: `TEXT`, `INTEGER`, `DECIMAL`, `BOOLEAN`, `DATE`, `ENUM` (önceden tanımlı seçenek listesi). Sayısal ve tarih alanlarında **aralık filtresi** desteklenir ("100.000–200.000 km arası"). | ZORUNLU |
| **BR-C-006** | Alt kategori, üst kategorinin alanlarını **devralır**. "Vasıta" seviyesinde tanımlı "Model Yılı", "Sedan" ilanlarında da sorulur. | OLMALI |
| **BR-C-007** | İçinde aktif ilan bulunan kategori silinemez; yalnızca pasife alınabilir (`is_active = false`). | ZORUNLU |

---

## 3. İlan (BR-L)

| ID | Kural | Seviye |
|---|---|---|
| **BR-L-001** | Başlık **10–70 karakter**. Alt sınır "acil satılık" gibi bilgisiz başlıkları, üst sınır tasarım bozulmasını engeller. | ZORUNLU |
| **BR-L-002** | Açıklama **en fazla 3000 karakter**. Alt sınır yoktur. | ZORUNLU |
| **BR-L-003** | Fotoğraf: en az **1**, en fazla **15**. Her dosya ≤ 5 MB, format `JPEG`/`PNG`/`WEBP`. | ZORUNLU |
| **BR-L-004** | Tam olarak **bir** fotoğraf kapak (cover) olarak işaretlenir. Satıcı seçmezse ilk fotoğraf kapak olur. | ZORUNLU |
| **BR-L-005** | İlan konumu il + ilçe olarak zorunludur (arama filtresinin temel kırılımı). | ZORUNLU |
| **BR-L-006** | Yaşam döngüsü: `DRAFT` → `PENDING_REVIEW` → (`APPROVED` \| `REJECTED`) → `ARCHIVED`. | ZORUNLU |
| **BR-L-007** | İlan `MODERATOR` veya `ADMIN` onayından geçmeden yayına çıkmaz. Reddedilen ilan için gerekçe zorunludur. | ZORUNLU |
| **BR-L-008** | Açık artırma **başladıktan sonra** ilanın başlığı, açıklaması, kategorisi ve fotoğrafları **değiştirilemez**. Gerekçe: teklif verenler gördükleri ürüne teklif verir; içeriğin sonradan değişmesi dolandırıcılığın klasik yöntemidir. | ZORUNLU |
| **BR-L-009** | Üzerinde **hiç teklif olmayan** bir açık artırma satıcı tarafından iptal edilebilir. Teklif geldikten sonra iptal yalnızca `ADMIN` yetkisindedir ve gerekçe kaydı zorunludur. | ZORUNLU |
| **BR-L-010** | İlan başlığı ve açıklaması üzerinde tam metin araması (full-text search) yapılabilir; Türkçe karakter ve ek yapısı dikkate alınır. | OLMALI |
| **BR-L-011** | İlanlar fiziksel olarak silinmez, `ARCHIVED` durumuna alınır. Geçmiş açık artırma kayıtlarının bütünlüğü korunmalıdır. | ZORUNLU |

---

## 4. Açık Artırma Yaşam Döngüsü (BR-A)

| ID | Kural | Seviye |
|---|---|---|
| **BR-A-001** | Her ilanın **tam olarak bir** açık artırması vardır (1—1 ilişki). | ZORUNLU |
| **BR-A-002** | Başlangıç fiyatı (`start_price`) sıfırdan büyük olmalıdır. | ZORUNLU |
| **BR-A-003** | Satıcı isteğe bağlı **rezerv fiyat** (`reserve_price`) belirleyebilir: altında satmayı kabul etmediği gizli taban. Rezerv fiyat başlangıç fiyatından küçük olamaz ve **teklif verenlere gösterilmez** — yalnızca "rezerv fiyata ulaşıldı/ulaşılmadı" bilgisi gösterilir. | OLMALI |
| **BR-A-004** | Süre en az **1 saat**, en fazla **14 gün**. Satıcı 1/3/7/14 günlük hazır seçeneklerden birini seçer. | ZORUNLU |
| **BR-A-005** | Durumlar: `SCHEDULED` → `ACTIVE` → (`ENDED_SOLD` \| `ENDED_NO_BIDS` \| `ENDED_RESERVE_NOT_MET` \| `CANCELLED`). | ZORUNLU |
| **BR-A-006** | **Sniper koruması.** Bitişe **120 saniye** veya daha az kala kabul edilen her teklif, bitiş zamanını **`teklif_anı + 120 saniye`** olarak günceller. Tavan: en fazla **20 uzatma** *veya* toplam **60 dakika** — hangisi önce dolarsa uzatma durur. Her uzatma denetim kaydına yazılır. | ZORUNLU |
| **BR-A-007** | Açık artırmayı **sistem** kapatır, kullanıcı isteği değil. Siteye kimse girmese bile artırma zamanında sonlanır. | ZORUNLU |
| **BR-A-008** | Kapanışta kazanan = **en yüksek geçerli teklifin** sahibi. Rezerv fiyat varsa ve en yüksek teklif altında kaldıysa sonuç `ENDED_RESERVE_NOT_MET` olur; kazanan yoktur ve satış zorunluluğu doğmaz. | ZORUNLU |
| **BR-A-009** | Hiç teklif gelmediyse sonuç `ENDED_NO_BIDS` olur. | ZORUNLU |
| **BR-A-010** | Kapanmış bir açık artırmaya teklif verilemez; kapanış sonrası gelen istek `409 Conflict` ile reddedilir. | ZORUNLU |
| **BR-A-011** | Zaman kaynağı **yalnızca sunucudur**. İstemci saati asla güvenilmez; geri sayım sunucudan gelen zaman damgasıyla senkronlanır. | ZORUNLU |
| **BR-A-012** | `ADMIN` aktif bir açık artırmayı iptal edebilir (`CANCELLED`). Tüm teklifler `VOIDED` olur ve teklif verenlere bildirim gider. Gerekçe zorunlu, işlem denetim kaydına yazılır. | ZORUNLU |

---

## 5. Teklif Verme (BR-B) — Sistemin Kalbi

| ID | Kural | Seviye |
|---|---|---|
| **BR-B-001** | Teklif verebilmek için: kullanıcı giriş yapmış, `ACTIVE`, e-postası doğrulanmış olmalı ve artırma `ACTIVE` durumda, `ends_at` henüz geçmemiş olmalıdır. | ZORUNLU |
| **BR-B-002** | **Satıcı kendi ilanına teklif veremez.** (Shill bidding / fiyat şişirme engeli.) İhlal denemesi denetim kaydına yazılır. | ZORUNLU |
| **BR-B-003** | **Minimum teklif:** İlk teklif ≥ `start_price`. Sonraki her teklif ≥ `current_price + artış_miktarı`. Artış miktarı, `current_price`'ın düştüğü kademeden okunur (bkz. §5.1). | ZORUNLU |
| **BR-B-004** | Aynı açık artırmada **iki teklif aynı tutarda olamaz.** Veritabanı seviyesinde benzersizlik kısıtıyla garanti altına alınır. | ZORUNLU |
| **BR-B-005** | Teklifler **değiştirilemez ve geri çekilemez** (append-only). Verilen teklif bağlayıcıdır. İptal yalnızca `ADMIN` tarafından `VOIDED` statüsü ile yapılır; satır silinmez. | ZORUNLU |
| **BR-B-006** | Mevcut en yüksek teklifin sahibi, kendi teklifinin üzerine tekrar teklif veremez (kendini geçemez). | ZORUNLU |
| **BR-B-007** | **Eşzamanlılık garantisi:** Aynı anda gelen tekliflerde sistem kaybolan güncellemeye (lost update) izin vermez. Her istek ya kabul edilir ya net bir hata alır; sessizce yutulmaz. Kabul edilen tekliflerin tutarları kesin artan sıradadır. | ZORUNLU |
| **BR-B-008** | Teklif kabulü ile fiyat güncellemesi **atomiktir** — ikisi birlikte gerçekleşir veya hiçbiri gerçekleşmez. | ZORUNLU |
| **BR-B-009** | Kabul edilen teklif, o açık artırmayı izleyen tüm istemcilere **1 saniye içinde** iletilir (WebSocket). | OLMALI |
| **BR-B-010** | Geçilen (outbid) kullanıcıya derhal bildirim gönderilir. | ZORUNLU |
| **BR-B-011** | Teklif reddedildiğinde hata mesajı **kabul edilebilir minimum tutarı içerir** ("En az 12.500 TL teklif verebilirsiniz"). Kullanıcı deneme-yanılma yapmak zorunda kalmaz. | OLMALI |
| **BR-B-012** | Teklif kayıtları IP adresi ve user-agent ile birlikte saklanır (dolandırıcılık analizi ve `BR-S-004` denetimi için). | ZORUNLU |
| **BR-B-013** | **Vekil (proxy) teklif:** Kullanıcının maksimum limitini girip sistemin onun adına otomatik artırması. **v1'de yoktur.** Şema bunu destekleyecek şekilde hazırlanır (`bids.max_amount`, `bids.is_proxy`) ki v2'de migration gerekmesin. | İLERİ |

### 5.1 Teklif Artış Kademeleri (BR-B-003 detayı)

Artış miktarı **mevcut fiyata** göre belirlenir:

| Mevcut fiyat aralığı (TL) | Artış tipi | Değer | Örnek |
|---|---|---|---|
| 0 – 1.000 | Sabit | 25 TL | 850 TL → min. 875 TL |
| 1.000 – 10.000 | Sabit | 100 TL | 4.200 TL → min. 4.300 TL |
| 10.000 – 100.000 | Sabit | 500 TL | 45.000 TL → min. 45.500 TL |
| 100.000 – 500.000 | Sabit | 2.500 TL | 320.000 TL → min. 322.500 TL |
| 500.000 – ∞ | Yüzde | %1 | 1.000.000 TL → min. 1.010.000 TL |

**Aralık uçları:** Alt sınır dahil, üst sınır hariç (`min ≤ fiyat < max`). Tam 1.000 TL, ikinci kademeye düşer.

**Yuvarlama:** Yüzde ile hesaplanan artış **yukarı yuvarlanarak** en yakın kademe adımına tamamlanır; kullanıcıya küsuratlı minimum tutar gösterilmez.

**Kademeler veritabanındadır, kodda sabit değildir.** Değişiklik için deploy gerekmez.

---

## 6. Bildirim ve İletişim (BR-N)

| ID | Kural | Seviye |
|---|---|---|
| **BR-N-001** | Bildirim tetikleyicileri: teklifin geçildi · açık artırmayı kazandın · ilanın satıldı · ilanın onaylandı/reddedildi · takip ettiğin ilan bitmek üzere (son 1 saat) · artırma iptal edildi. | ZORUNLU |
| **BR-N-002** | Kullanıcı ilanı **takip listesine** (favori) ekleyebilir; bu ilanlar için bitiş uyarısı alır. | OLMALI |
| **BR-N-003** | Açık artırma **kapandıktan sonra** kazanan ile satıcının iletişim bilgileri (telefon, e-posta) **karşılıklı** açılır. Öncesinde hiçbir koşulda gösterilmez. | ZORUNLU |
| **BR-N-004** | Her iletişim bilgisi ifşası kaydedilir: kimin bilgisi, kime, hangi açık artırma nedeniyle, ne zaman açıldı. → `BR-K-004` | ZORUNLU |
| **BR-N-005** | Ödeme, emanet (escrow) ve kargo **v1 kapsamı dışındadır.** Sistem kazananı belirler ve tarafları buluşturur; ticaretin geri kalanı platform dışında yürür. | — |
| **BR-N-006** | Taraflar açık artırma sonrası birbirini 1–5 arası puanlayabilir. Puanlama yalnızca `ENDED_SOLD` durumundaki artırmalar için ve **kapanıştan sonraki 30 gün** içinde mümkündür. | OLMALI |
| **BR-N-007** | Bildirimlerin iletimi ile teklif işlemi **birbirine bağımlı olmamalıdır**: bildirim servisi çökse bile teklif kabulü başarısız olmaz, bildirim sonradan iletilir. | ZORUNLU |

---

## 7. Güvenlik — Non-Fonksiyonel (BR-S)

| ID | Kural | Seviye |
|---|---|---|
| **BR-S-001** | Parolalar **Argon2id** ile hashlenir. Ters çevrilebilir şifreleme veya düz metin saklama kesinlikle yasaktır. Kullanılan algoritma kayıtla birlikte saklanır ki gelecekte algoritma geçişi mümkün olsun. | ZORUNLU |
| **BR-S-002** | **Hız sınırlama (rate limiting):** giriş 5/dk/IP · teklif 10/dk/kullanıcı **ve** 1/sn/artırma/kullanıcı · kayıt 3/saat/IP · arama 60/dk/IP. Aşımda `429 Too Many Requests`. | ZORUNLU |
| **BR-S-003** | Ardışık 5 başarısız girişten sonra hesap kademeli olarak kilitlenir (1dk → 5dk → 15dk → 1sa). | ZORUNLU |
| **BR-S-004** | Yetkilendirme **sunucu tarafında, metot seviyesinde** uygulanır. Arayüzde butonu gizlemek yetkilendirme değildir. | ZORUNLU |
| **BR-S-005** | Tüm trafik HTTPS üzerinden yürür; WebSocket bağlantıları `wss://` kullanır. | ZORUNLU |
| **BR-S-006** | Kritik işlemler (rol değişikliği, ilan onay/ret, artırma iptali, teklif geçersizleştirme, hesap askıya alma) değişiklik öncesi/sonrası değerleriyle denetim kaydına yazılır. | ZORUNLU |
| **BR-S-007** | Yüklenen dosyalar uzantısına göre değil, **gerçek içeriğine** (magic number) göre doğrulanır. EXIF verisi (konum bilgisi dahil) temizlenir. | ZORUNLU |
| **BR-S-008** | Erişim token'ı 15 dakika geçerlidir; yenileme token'ı (refresh token) her kullanımda döndürülür ve veritabanında **hash'lenmiş** saklanır. Çalınmış token'ın yeniden kullanımı tespit edildiğinde o kullanıcının tüm oturumları kapatılır. | ZORUNLU |
| **BR-S-009** | Arayüz mobil, tablet ve masaüstünde çalışır (responsive). Kritik kırılım noktaları: 360px, 768px, 1280px. | ZORUNLU |
| **BR-S-010** | Kullanıcı girdisi arayüzde işlenirken kaçışlanır (XSS); veritabanı erişiminde parametreli sorgu kullanılır (SQL injection). String birleştirerek SQL kurulması yasaktır. | ZORUNLU |

---

## 8. KVKK / Kişisel Veri (BR-K)

> Türkiye'de faaliyet gösteren bir platform için 6698 sayılı KVKK uyumu yasal zorunluluktur. Staj projesinde bunun ele alınmış olması ciddi bir olgunluk göstergesidir.

| ID | Kural | Seviye |
|---|---|---|
| **BR-K-001** | Toplanan kişisel veriler: ad-soyad, e-posta, telefon, IP adresi, konum (il/ilçe). Her biri için toplama amacı `docs/01-requirements/kvkk-veri-envanteri.md` içinde belgelenir. | ZORUNLU |
| **BR-K-002** | Kullanıcı, kendisine ait tüm verileri makine-okunur formatta dışa aktarabilir (veri taşınabilirliği hakkı). | OLMALI |
| **BR-K-003** | **Silme hakkı anonimleştirme ile karşılanır.** Teklif geçmişi silinemez — silinirse geçmiş açık artırmaların bütünlüğü bozulur ve diğer katılımcıların kayıtları anlamsızlaşır. Bunun yerine kişisel alanlar maskelenir (`ad → "Silinmiş Kullanıcı"`, `e-posta → deleted-{id}@tiklasat.local`, telefon → `NULL`); teklif satırları kimliksiz olarak yerinde kalır. | ZORUNLU |
| **BR-K-004** | İletişim bilgisi ifşası kayıt altına alınır (→ `BR-N-004`); kullanıcı kendi bilgisinin kime açıldığını görebilir. | ZORUNLU |
| **BR-K-005** | Denetim kayıtları **2 yıl**, IP adresleri **6 ay** sonra otomatik temizlenir. Süresiz saklama KVKK'nın "ölçülülük" ilkesine aykırıdır. | OLMALI |
| **BR-K-006** | Kayıt sırasında açık rıza alınır; rızanın alındığı zaman, sürüm ve IP saklanır. | ZORUNLU |

---

## 9. Kapsam Dışı (v1)

Bunlar **unutulmuş** değil, **bilinçli olarak ertelenmiş** özelliklerdir. Şema bunları engellemeyecek şekilde tasarlanır.

| Konu | Neden ertelendi | Şemada hazırlık |
|---|---|---|
| Ödeme / emanet / kargo | Kapsamı ikiye katlar, teklif motorunun kalitesinden feragat gerektirirdi | — |
| Vekil (proxy) teklif | Zincirleme otomatik teklif çözümlemesi ayrı bir tasarım problemi | `bids.max_amount`, `bids.is_proxy` kolonları hazır |
| Site içi mesajlaşma | v1'de iletişim bilgisi ifşası yeterli | — |
| Çoklu para birimi | Tek pazar (TR) hedefleniyor | `currency CHAR(3)` kolonu hazır |
| Mobil uygulama | Responsive web yeterli | API zaten istemciden bağımsız |
| Otomatik dolandırıcılık tespiti | ML/kural motoru ayrı proje | `audit_logs` ham veriyi topluyor |

---

## Ek: Hızlı Referans Kartı

| Kural | Değer |
|---|---|
| Başlık | 10–70 karakter |
| Açıklama | ≤ 3000 karakter |
| Fotoğraf | 1–15 adet, ≤ 5 MB, JPEG/PNG/WEBP |
| Kategori derinliği | ≤ 4 seviye |
| Açık artırma süresi | 1 saat – 14 gün |
| Sniper penceresi | Son 120 saniye |
| Sniper uzatması | `şimdi + 120 sn` |
| Uzatma tavanı | 20 kez **veya** toplam 60 dk |
| Teklif artışı | Kademeli tablo (§5.1) |
| Parola | ≥ 10 karakter, Argon2id |
| Erişim token'ı | 15 dakika |
| Para | `NUMERIC(15,2)`, TRY |
| Zaman | `TIMESTAMPTZ`, UTC saklanır |
