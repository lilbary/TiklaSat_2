# ADR-0004 · Teklif Yolunda Pesimistik Satır Kilidi

| | |
|---|---|
| **Durum** | Kabul edildi |
| **Tarih** | 2026-07-30 |

---

## Bağlam

`BR-B-007` şunu şart koşuyor:

> Aynı anda gelen tekliflerde sistem kaybolan güncellemeye (lost update) izin vermez. Her istek ya kabul edilir ya net bir hata alır; sessizce yutulmaz.

Bu gereksinimin ihlali, açık artırma platformunda **en ağır** hata türüdür: daha düşük bir teklif kazanan gösterilir, kimse hata mesajı görmez, sorun ancak kullanıcı şikâyetiyle ortaya çıkar.

Trafik profili sıra dışıdır: bir açık artırma günlerce sessiz durur, **son 10 saniyede** tüm teklifler patlar. Yani çakışma olasılığı zamana göre düzgün dağılmaz; sistemin en kritik anında tavan yapar.

---

## Karar

Teklif kabulü, **`auctions` satırı üzerinde pesimistik yazma kilidi** (`SELECT ... FOR UPDATE`) alınarak, tek ve kısa bir transaction içinde yürütülecek.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
@Query("SELECT a FROM Auction a WHERE a.id = :id")
Optional<Auction> findByIdForUpdate(@Param("id") UUID id);
```

Buna ek olarak `auctions.version` (optimistik kilit) kolonu **korunacak** — ancak teklif yolu için değil, teklif **dışı** güncellemeler için.

---

## Değerlendirilen seçenekler

### A · Optimistik kilit (`@Version`) — teklif yolunda
Satırda sürüm numarası tutulur; yazarken sürüm değişmişse işlem reddedilir ve çağıran yeniden dener.

**Reddedildi — retry fırtınası.**

```
20 eşzamanlı teklif, optimistik kilit:
  tur 1 → 1 başarılı, 19 çakışma → 19 yeniden dener
  tur 2 → 1 başarılı, 18 çakışma → 18 yeniden dener
  tur 3 → 1 başarılı, 17 çakışma → ...
  ────────────────────────────────────────────
  toplam ≈ 190 transaction (20 yerine)
```

Sorun yalnızca sayı değil, **yönü**: yük arttıkça retry artar, retry arttıkça sistem yavaşlar, yavaşladıkça çakışma penceresi genişler ve daha çok retry üretir. Kendini besleyen bir çöküş döngüsüdür ve tam olarak sistemin en yoğun anında tetiklenir.

Ayrıca kullanıcı deneyimi kötüdür: teklif teknik olarak geçerliyken "tekrar deneyin" mesajı almak, açık artırmanın son saniyelerinde kabul edilemez.

### B · Uygulama seviyesinde kilit (`synchronized` / `ReentrantLock`)
**Reddedildi.** Yalnızca **tek JVM içinde** çalışır. Uygulama iki kopya olarak dağıtıldığı anda koruma tamamen ortadan kalkar — ve bu, hiçbir hata vermeden sessizce olur. Ölçeklenebilirlik kararının, doğruluk garantisini bozması kabul edilemez.

### C · Dağıtık kilit (Redis / Redisson)
**Reddedildi.** Çoklu kopyada çalışır, ancak:
- Kilidin sahibi Redis, verinin sahibi PostgreSQL → **iki ayrı doğruluk kaynağı**
- Redis erişilemez olursa ya sistem durur ya da koruma devre dışı kalır
- Kilit süresi zaman aşımına dayanır; "kilit düştü ama işlem hâlâ sürüyor" durumu mümkündür
- Zaten `FOR UPDATE` ile aynı transaction'da ücretsiz alınabilecek bir garanti için harici bağımlılık eklemek gereksizdir

**İlke:** Kilit, korunan verinin bulunduğu yerde alınmalıdır.

### D · `SERIALIZABLE` izolasyon seviyesi
Tüm transaction'ları en katı izolasyonda çalıştırmak. **Reddedildi:** PostgreSQL'de `SERIALIZABLE`, çakışmaları **serileştirme hatası fırlatarak** çözer — yani davranışı optimistik kilide benzer ve aynı retry problemini üretir. Ayrıca tüm sorgulara maliyet biner, sadece sıcak yola değil.

### E · Kuyruk (tek tüketicili teklif kuyruğu)
Teklifleri Kafka/RabbitMQ'ya yazıp tek bir tüketicinin sırayla işlemesi. **Reddedildi:** Doğru çalışır ama kullanıcıya **anında** yanıt veremez ("teklifiniz alındı, sonucu birazdan öğreneceksiniz"). Açık artırmada kullanıcı kabul/ret bilgisini derhal görmelidir. Ayrıca staj kapsamı için ciddi bir altyapı yükü getirir.

---

## Sonuçlar

### Olumlu
- Kaybolan güncelleme **yapısal olarak** imkânsızdır
- 20 istek → 20 transaction. Sabit, öngörülebilir, adil (ilk gelen ilk işlenir)
- Farklı açık artırmalar birbirini **hiç** etkilemez — kilit satır seviyesindedir, tablo seviyesinde değil. Paralellik korunur
- Kullanıcı retry yapmak zorunda kalmaz; ya kabul ya net ret alır
- Harici bağımlılık yok; garanti veritabanının kendisinden gelir

### Olumsuz / bedeller
- Aynı artırmaya gelen teklifler **seri** işlenir → tek artırmanın verimi kilit tutma süresine bağlıdır (bütçe: < 10 ms, ≈ 100 teklif/sn)
- Kilidi tutan transaction takılırsa arkasındakiler bekler → `lock.timeout = 3000ms` ve `@Transactional(timeout = 5)` **zorunludur**. Bunlar olmadan bir yavaş sorgu tüm thread havuzunu tüketebilir (thread starvation)
- Transaction içinde HTTP çağrısı, e-posta gönderimi veya WebSocket yayını **kesinlikle yasaktır** — bunlar outbox üzerinden asenkron yapılır

### Neden `version` kolonu hâlâ duruyor?

Optimistik kilit, teklif **dışı** güncellemeler için kullanılır:

| İşlem | Sıklık | Uygun mekanizma |
|---|---|---|
| Teklif verme | Çok sık, çakışma **kesin** | Pesimistik |
| Admin artırma iptali | Çok nadir | Optimistik |
| Kapatma işi güncellemesi | Artırma başına bir kez | Optimistik |
| Moderasyon değişikliği | Nadir | Optimistik |

**Genel ilke:** Çakışma olasılığı düşükse optimistik, yüksekse pesimistik. İki mekanizmanın aynı tabloda birlikte bulunması çelişki değil, **yerinde araç seçimidir**.

### Deadlock riski

Teklif yolunda her transaction **tek bir** satır kilitler. Ölümcül kilitlenme en az iki kilidin karşılıklı beklenmesini gerektirir → bu tasarımda **yapısal olarak imkânsızdır**.

Kapatma işi çoklu satır kilitler, ancak `SKIP LOCKED` beklemeyi tamamen ortadan kaldırır → orada da deadlock oluşamaz.

### İkinci savunma katmanı

Kilit mekanizmasının kendisi hatalı olursa (unutulmuş `@Transactional`, yanlış repository metodu), `UNIQUE (auction_id, amount)` kısıtı ikinci katman olarak devreye girer ve aynı tutarın iki kez yazılmasını engeller. Kritik sistemlerde savunma tek katmana bırakılmaz.

---

## Doğrulama

Bu karar, `concurrency-design.md` §10.2'deki 50 thread'lik eşzamanlılık testiyle doğrulanacaktır. Test **gerçek PostgreSQL** üzerinde (Testcontainers) koşar — H2 gibi bellek içi veritabanları `FOR UPDATE` semantiğini birebir taklit etmediği için testi anlamsız kılardı.

---

## İlgili

- `docs/02-design/concurrency-design.md` — tam akış, kod ve testler
- [ADR-0002](ADR-0002-postgresql-ve-flyway.md) — `FOR UPDATE`/`SKIP LOCKED` gereksinimi PostgreSQL seçimini belirledi
- [ADR-0003](ADR-0003-listing-auction-ayrimi.md) — dar sıcak satır, kilit süresini kısaltır
