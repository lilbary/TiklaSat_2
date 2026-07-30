# TıklaSat — Terimler Sözlüğü

| | |
|---|---|
| **Doküman** | Sözlük (Glossary) |
| **Sürüm** | 1.0 |
| **Tarih** | 2026-07-30 |

Bu sözlük iki nedenle var:

1. **Ortak dil.** Aynı şeye iki farklı isim verilirse dokümanlar çelişir. Burada bir terimin karşılığı ne ise, kodda ve tabloda da odur.
2. **Savunma hazırlığı.** Staj sunumunda kullanılan her teknik terimin tek cümlelik, ezberlenebilir bir açıklaması burada.

---

## A. Açık Artırma Alan Terimleri

### Açık artırma (auction)
Bir malın, belirli bir süre boyunca alıcıların birbirinin üzerine teklif vermesiyle satıldığı satış yöntemi. TıklaSat **İngiliz tipi** (English auction) kullanır: fiyat aşağıdan yukarı çıkar, süre sonunda en yüksek teklif kazanır. (Alternatifi Hollanda tipidir — fiyat yukarıdan aşağı iner, ilk kabul eden alır. Bizde yok.)

### Başlangıç fiyatı (starting price)
Satıcının belirlediği ve ilk teklifin altına inemeyeceği taban fiyat. **Herkese açıktır.**

### Rezerv fiyat (reserve price)
Satıcının "bunun altına satmam" dediği **gizli** taban. Başlangıç fiyatından farkı: başlangıç fiyatı görünür ve teklif buradan başlar; rezerv fiyat görünmez ve kapanışta devreye girer.

Rezerve ulaşılmadan artırma biterse **satış gerçekleşmez** — en yüksek teklifi veren kişi ürünü almaya hak kazanmaz.

**Neden ikisi birden var?** Satıcı düşük başlangıç fiyatıyla ilgi çekmek ister ("1 TL'den başlıyor!") ama gerçek değerinin altına satmak istemez. Rezerv fiyat bu ikisini uzlaştırır.

### Teklif (bid)
Bir alıcının belirli bir tutarı ödemeyi taahhüt etmesi. TıklaSat'ta **bağlayıcıdır ve geri alınamaz** (`BR-B-005`).

### Geçilmek (outbid)
En yüksek teklif sahibiyken bir başkasının daha yüksek teklif vermesi. Sistem geçilen kullanıcıya derhal bildirim gönderir.

### Teklif artış miktarı (bid increment)
Yeni bir teklifin, mevcut fiyatın en az ne kadar üzerinde olması gerektiği. TıklaSat'ta **kademeli** bir tablodan okunur — fiyat büyüdükçe artış da büyür (`BR-B-003`).

### Sniping / Snipe (son saniye teklifi)
Açık artırmanın **son saniyelerinde** teklif verip diğerlerine karşılık verme fırsatı bırakmama taktiği. "Keskin nişancı" benzetmesinden gelir.

**Neden sorun?** Fiyatın gerçek değerine ulaşmasını engeller ve deneyimsiz kullanıcıyı sistematik olarak dezavantajlı bırakır — kazanan en çok değer veren değil, en hızlı tıklayan olur.

### Anti-sniping / Sniper koruması
Son anlarda gelen teklifin süreyi otomatik uzatması. Böylece **her teklife karşılık verme fırsatı** doğar. TıklaSat kuralı: son 120 saniyede gelen teklif bitişi `teklif_anı + 120 sn` yapar; en fazla 20 uzatma veya toplam 60 dakika (`BR-A-006`).

### Vekil teklif / Otomatik artırma (proxy bidding)
Kullanıcının ödemeye razı olduğu **maksimum** tutarı girmesi ve sistemin onun adına, gerektiği kadar, kademe kademe artırması. eBay'in temel mekanizması.

**v1'de yoktur** — ama şema hazırdır (`BR-B-013`).

### Shill bidding (danışıklı teklif)
Satıcının kendisi veya bir yakını aracılığıyla kendi ilanına teklif vererek fiyatı yapay olarak yükseltmesi. Dolandırıcılıktır. TıklaSat'ta satıcının kendi ilanına teklif vermesi engellenir (`BR-B-002`).

### Emanet / Escrow
Alıcının parasını, mal teslim edilene kadar tutan **güvenilir üçüncü taraf**. TıklaSat v1 kapsamında **yoktur** (`BR-N-005`).

---

## B. Veritabanı Terimleri

### Şema (schema)
Veritabanının yapısı: hangi tablolar var, hangi kolonları taşıyor, aralarındaki ilişkiler ne. Verinin kendisi değil, **verinin kalıbı**.

### ERD (Entity-Relationship Diagram)
Varlık-İlişki Diyagramı. Tabloları kutu, ilişkileri çizgi olarak gösteren şema resmi. `docs/02-design/data-model.md` içinde.

### Birincil anahtar (primary key, PK)
Bir satırı benzersiz olarak tanımlayan kolon. TıklaSat'ta **UUID** kullanılır (sıralı sayı yerine) — sebebi §D'de.

### Yabancı anahtar (foreign key, FK)
Bir tablodaki kolonun başka bir tablonun birincil anahtarına işaret etmesi. `listings.seller_id` → `users.id` gibi. Var olmayan bir kullanıcıya ilan bağlanmasını **veritabanı seviyesinde** imkânsız kılar.

### CHECK kısıtı
Bir kolonun alabileceği değerlere veritabanı seviyesinde konan kural. Örnek: `CHECK (char_length(title) BETWEEN 10 AND 70)`.

**Neden kodda değil de veritabanında?** Uygulama katmanında bir hata olsa, biri elle SQL çalıştırsa veya ileride ikinci bir servis yazılsa bile veri bozulamaz. Veritabanı **son savunma hattıdır**.

### UNIQUE kısıtı
Bir kolonun (veya kolon grubunun) tabloda tekrar edemeyeceği kuralı. TıklaSat'ta `UNIQUE(auction_id, amount)` aynı artırmada iki eşit teklifi imkânsız kılar — ve bu, bir yarış koşulu savunmasıdır.

### Index
Veritabanının aramayı hızlandırmak için tuttuğu yardımcı yapı. Kitabın arkasındaki dizin gibi: indexsiz arama tüm tabloyu satır satır tarar (**Seq Scan**), indexle doğrudan hedefe gider (**Index Scan**).

### EAV (Entity-Attribute-Value)
"Varlık-Özellik-Değer" modeli. Her kategoriye ayrı kolon açmak yerine, özellikleri **satır** olarak saklama yöntemi.

**Neden gerekli?** Otomobilin "km"si var, telefonun "hafıza"sı var, evin "oda sayısı" var. Hepsine kolon açsan tablo yüzlerce kolona çıkar ve %95'i her satırda boş kalır. Yeni kategori = yeni migration olur.

**TıklaSat'ın varyantı:** Değerler tek bir `text` kolonunda değil, **tipli kolonlarda** (`value_number NUMERIC`, `value_date`, `value_bool`...) saklanır. Böylece "100.000–200.000 km arası" gibi aralık sorguları index kullanabilir. Klasik EAV'ın en büyük zaafı budur ve bu şekilde kapatılır.

### Migration
Veritabanı şemasında yapılan, numaralandırılmış ve versiyon kontrolüne giren değişiklik dosyası. TıklaSat **Flyway** kullanır: `V1__...sql`, `V2__...sql`.

**Neden Hibernate'in `ddl-auto` özelliği kullanılmıyor?** Çünkü o, şemayı otomatik ve **öngörülemez** biçimde değiştirir; ne yaptığını göremezsin, geri alamazsın, üretimde veri kaybettirir. Migration dosyaları ise okunabilir, gözden geçirilebilir ve geri alınabilir.

### Soft delete (yumuşak silme)
Kaydı fiziksel olarak silmek yerine "silinmiş" işareti koymak. TıklaSat ilanlarda ve kullanıcılarda bunu kullanır (`BR-L-011`, `BR-K-003`) — geçmiş açık artırma kayıtlarının bütünlüğü için zorunludur.

### Materialized path
Ağaç yapısını (kategori hiyerarşisi) her satırda kökten kendine kadar olan yolu saklayarak hızlandırma tekniği. `"/vasita/otomobil/sedan/"` gibi. "Vasıta ve tüm alt kategorilerindeki ilanlar" sorgusu tek bir `LIKE '/vasita/%'` ile çözülür — yinelemeli sorgu gerekmez.

---

## C. Eşzamanlılık Terimleri

> Bu bölüm projenin teknik kalbidir. Buradaki terimler staj savunmasında en çok sorulacak olanlardır.

### Transaction (işlem)
Ya tamamı gerçekleşen ya da hiçbiri gerçekleşmeyen işlem grubu. "Teklifi kaydet **ve** fiyatı güncelle" ikisi bir transaction'dır: yarısı olmuş bir teklif kabul edilemez.

### Race condition (yarış koşulu)
İki işlemin aynı veriye aynı anda erişmesi sonucu **sonucun hangisinin daha hızlı olduğuna bağlı hale gelmesi**. Açık artırmada kaçınılmaz bir risktir: son saniyede yüzlerce kişi aynı satırı güncellemeye çalışır.

### Lost update (kaybolan güncelleme)
Yarış koşulunun en klasik sonucu. Somut senaryo:

```
Mevcut fiyat: 1000 TL
  t=0ms  A okur   → 1000 gördü
  t=1ms  B okur   → 1000 gördü          ← ikisi de aynı eski değeri gördü
  t=2ms  A yazar  → 1100 (geçerli)
  t=3ms  B yazar  → 1050 (1000'in üstü sandı)

Sonuç: fiyat 1050. A'nın 1100'lük teklifi kayboldu ve
       daha düşük bir teklif kazanmış göründü.
```

TıklaSat bunu üç katmanlı savunmayla imkânsız kılar (`BR-B-007`).

### Pesimistik kilit (pessimistic lock)
"Çakışma **olacak** varsay, önceden engelle." Satırı okurken kilitler, işi bitene kadar kimseyi sokmaz. SQL karşılığı: `SELECT ... FOR UPDATE`.

TıklaSat teklif yolunda **bunu kullanır.**

### Optimistik kilit (optimistic lock)
"Çakışma **olmayacak** varsay, olursa yakala." Satırda bir sürüm numarası tutar; yazarken sürüm değişmişse işlemi reddeder ve çağıranı **yeniden denemeye** zorlar.

**Neden teklif yolunda kullanılmıyor?** Son saniyelerde çakışma istisna değil **kuraldır**. Her kaybeden yeniden dener, denemeler yeni çakışma üretir — sistemin en yoğun anında **retry fırtınası** oluşur. Pesimistik kilit ise istekleri sıraya dizer; her istek tam olarak bir kez çalışır.

> **Genel kural:** Çakışma nadirse optimistik, sıksa pesimistik. Açık artırmanın son 10 saniyesi "sık"tır.

### `FOR UPDATE`
Okunan satırı, transaction bitene kadar diğer yazıcılara kapatan SQL ifadesi. Diğerleri **bekler** (sıraya girer), hata almaz.

### `SKIP LOCKED`
`FOR UPDATE` ile birlikte kullanıldığında: kilitli satırı beklemek yerine **atla, sıradakine geç**. Birden fazla sunucunun aynı iş kuyruğunu çakışmadan işlemesini sağlar. TıklaSat'ta açık artırma kapatma işinde kullanılır.

### Deadlock (ölümcül kilitlenme)
İki transaction'ın karşılıklı olarak diğerinin kilidini beklemesi; ikisi de sonsuza kadar bekler. **Önlem:** kilitleri her zaman aynı sırayla al ve transaction'ları kısa tut. TıklaSat teklif yolunda tek satır kilitlenir — deadlock yapısal olarak mümkün değildir.

### Idempotency (etkisizlik / tekrar dayanıklılığı)
Aynı isteğin birden fazla kez çalıştırılmasının, bir kez çalıştırılmasıyla **aynı sonucu** vermesi. Kullanıcı butona iki kez basarsa iki teklif kaydedilmemelidir.

### Transactional Outbox
"Veriyi yaz" ile "haber ver" adımlarını güvenilir biçimde birbirine bağlayan desen. Mesaj doğrudan gönderilmez; **aynı transaction içinde** bir tabloya yazılır, ayrı bir süreç oradan okuyup gönderir.

**Çözdüğü iki hata:**
- Mesaj gitti ama transaction geri alındı → kullanıcılar **var olmayan** bir teklif gördü.
- Transaction başarılı ama mesaj kayboldu → ekranlar **donuk** kaldı, kimse yeni fiyatı görmedi.

### `TIMESTAMPTZ` ve UTC
Zaman dilimi bilgisini dikkate alan PostgreSQL zaman tipi. TıklaSat tüm zamanları **UTC** olarak saklar, yalnızca ekrana basarken `Europe/Istanbul`'a çevirir.

**Neden?** Yaz saati uygulaması bir gecede saatleri kaydırır. Yerel saatle saklanan bir açık artırma o gece ya bir saat erken biter ya da bir saatlik bir zaman aralığı iki kez yaşanır. UTC'de böyle bir sıçrama yoktur.

---

## D. Mimari ve Altyapı Terimleri

### Spring Boot
Java'da web uygulaması geliştirmek için kullanılan, endüstri standardı framework. Yapılandırmayı kendisi üstlenir; sen iş mantığına odaklanırsın.

### JPA / Hibernate
Java nesneleri ile veritabanı tabloları arasında çeviri yapan katman (ORM — Object-Relational Mapping). `Auction` sınıfı ile `auctions` tablosunu birbirine bağlar.

**Dikkat:** ORM kolaylık sağlar ama ürettiği SQL'i **görmek zorundasın**. Teklif yolu gibi kritik yerlerde ürettiği sorguyu doğrulamadan güvenme.

### WebSocket
Sunucu ile tarayıcı arasında **çift yönlü ve sürekli açık** bağlantı. Normal HTTP'de tarayıcı sorar, sunucu cevaplar; WebSocket'te sunucu sorulmadan da **kendisi haber verebilir**.

Açık artırmada zorunludur: yeni teklif geldiğinde tüm izleyicilerin ekranı, onlar hiçbir şey yapmadan güncellenmelidir.

### STOMP
WebSocket üzerinde çalışan basit mesajlaşma protokolü. Kanal (topic) mantığı getirir: istemci `/topic/auction/{id}` kanalına abone olur, o artırmanın mesajlarını alır.

### SignalR
Microsoft **.NET**'e özgü real-time kütüphanesi. **Java'da kullanılamaz.** İlk gereksinim taslağında geçiyordu; Java kararı sonrası yerini Spring WebSocket + STOMP aldı → `ADR-0001`.

### SockJS
WebSocket'i desteklemeyen ortamlarda (bazı kurumsal güvenlik duvarları) otomatik olarak alternatif yönteme düşen yedekleme katmanı. Kullanıcı farkı hissetmez.

### Rate limiting (hız sınırlama)
Bir kullanıcının veya IP'nin belirli sürede yapabileceği istek sayısını sınırlama. Kötüye kullanımı ve kaba kuvvet saldırılarını engeller (`BR-S-002`).

### Bucket4j
Java'da hız sınırlama kütüphanesi. "Token bucket" algoritmasını uygular: her kullanıcının bir jeton kovası vardır, her istek bir jeton harcar, kova sabit hızla dolar.

### Redis
Bellekte çalışan, çok hızlı veri deposu. TıklaSat'ta hız sınırlama sayaçları ve önbellek için kullanılır. **Neden veritabanı değil?** Her istekte disk yazmak sistemi yavaşlatır; sayaçlar da kalıcı olmak zorunda değildir.

### ShedLock
Uygulamanın **birden fazla kopyası** çalışırken, zamanlanmış bir işin yalnızca birinde çalışmasını sağlayan kilit kütüphanesi. Olmazsa 3 sunucu aynı açık artırmayı 3 kez kapatmaya çalışır.

### Argon2id
Modern parola hashleme algoritması. Kasıtlı olarak **yavaş** ve **çok bellek tüketir** — böylece saldırganın ekran kartıyla saniyede milyarlarca deneme yapması ekonomik olmaktan çıkar. 2015 Password Hashing Competition kazananıdır.

### JWT (JSON Web Token)
Kullanıcının kimliğini taşıyan, imzalı metin parçası. Sunucu her istekte oturum tablosuna bakmak yerine imzayı doğrular.

**Zaafı:** İptal edilemez — süresi dolana kadar geçerlidir. Bu yüzden ömrü **kısa** tutulur (15 dk) ve yanına iptal edilebilir bir yenileme token'ı konur (`BR-S-008`).

### Testcontainers
Testler sırasında **gerçek** bir veritabanını Docker konteynerinde ayağa kaldıran kütüphane. Sahte/bellek-içi veritabanı yerine gerçeğini kullanmayı sağlar.

**Neden önemli?** H2 gibi bellek-içi veritabanları `FOR UPDATE` ve `SKIP LOCKED` davranışını PostgreSQL ile birebir aynı taklit etmez. Eşzamanlılık testini sahte veritabanında yapmak, testi anlamsız kılar.

### ADR (Architecture Decision Record)
Önemli bir teknik kararın **bağlamını, değerlendirilen seçenekleri ve gerekçesini** tek sayfada kaydeden kısa doküman. `docs/03-decisions/` altında.

**Neden yazılır?** Altı ay sonra "burada neden şunu kullanmışız?" sorusunun cevabı kimsenin aklında kalmaz. ADR o cevabı kalıcılaştırır — ve staj savunmasında hazır malzemedir.

### C4 modeli
Yazılım mimarisini dört zoom seviyesinde anlatma yöntemi: **Context** (sistem dış dünyayla nasıl konuşur) → **Container** (hangi çalıştırılabilir parçalar var) → **Component** (bir parçanın içi) → **Code** (sınıflar). TıklaSat ilk üç seviyeyi çizer.

### UUID
128 bitlik, evrensel olarak benzersiz kimlik (`a3f8...` gibi). TıklaSat birincil anahtar olarak bunu kullanır.

**Neden 1, 2, 3 diye artan sayı değil?**
- Artan sayı **bilgi sızdırır**: `/ilan/1834` adresini gören rakip, sitede kaç ilan olduğunu öğrenir.
- Artan sayı **tahmin edilebilir**: `/ilan/1835`, `/ilan/1836` denenerek içerik taranabilir.
- UUID kimliği **veritabanına yazmadan önce** üretilebilir; bu, ilişkili kayıtları tek transaction'da kurmayı kolaylaştırır.

**Bedeli:** 16 bayt (integer 4 bayttır) ve rastgele oldukları için index'te daha fazla dağılma yaratır. TıklaSat ölçeğinde bu bedel önemsizdir; **UUIDv7** kullanılarak (zaman sıralı UUID) dağılma sorunu da büyük ölçüde giderilir.
