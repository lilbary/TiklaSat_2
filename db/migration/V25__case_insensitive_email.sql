-- BR-U-001: e-posta büyük/küçük harf duyarsız benzersiz olmalı.

-- 1) Mevcut kayıtları normalize et. Bu satır bizim veritabanımızda bir şey
--    değiştirmiyor (hepsi zaten küçük harf) ama migration'ın çalıştığı her
--    ortamda aynı sonucu vermesi için gerekli.
UPDATE users SET email = lower(email) WHERE email <> lower(email);

-- 2) Asıl garanti: benzersizlik artık e-postanın küçük harfli hâli üzerinden.
--    Kod normalize etmeyi unutsa ya da biri doğrudan SQL çalıştırsa bile
--    'Ali@x.com' ile 'ali@x.com' aynı adres sayılıp ikincisi reddedilir.
--
--    NOT: Bu index oluşturulurken çakışan kayıt varsa migration PATLAR.
--    Kasıtlı — hangi kaydın kalacağına sessizce karar vermek yerine
--    insan müdahalesi istiyoruz.
CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email));
