package com.gib.tiklasat.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    //springbootun redise baglanıp okuma yazma yapmasını saglar
    private final StringRedisTemplate redisTemplate;
    //redis buyuktur icindeki veriler karısmasın diye basına RT koyduk
    private static final String REDIS_PREFIX = "RT:";

    public String createRefreshToken(UUID userId, boolean rememberMe) {
        String refreshToken = UUID.randomUUID().toString();
        // Beni hatırla işaretliyse 30 gün, değilse 1 gün (Session süresi kadar) geçerli

        //eger remember me kutucugu isaretliyse 30 degilse 1 gunluk uret bi tokeni
        long days = rememberMe ? 30 : 1;

        //REDİSE VERİYİ YAZIYORUZ
        redisTemplate.opsForValue().set(
                REDIS_PREFIX + refreshToken, //RT:2190130UUID
                userId.toString(),// GİRİS YAPAN ADAMIN IDSİ
                days,// 30 YA DA 1 GUN SAYISI
                TimeUnit.DAYS//GUN OLDUGUNU BELİRTİYOZ SANİYE DAKİKA FALAN DEGİL YANİ
        );

        return refreshToken;
        //BU RETURNU AuthController sınıfındaki login metodu yakalıyo
    }







    public UUID validateAndGetUserId(String refreshToken) {//15dklı accestoken bittiginde calısır
        String userIdStr = redisTemplate.opsForValue().get(REDIS_PREFIX + refreshToken);
        //cerezden gelen kodun basına rt koyup redisin icinde bu kodu aratıyoruz

        if (userIdStr == null) {
            throw new RuntimeException("Geçersiz veya süresi dolmuş Refresh Token!");
        }
        return UUID.fromString(userIdStr);
        //eger hata donmediyse uuıd formatına cevirip authcontrollera veriyoruz controllerda kim oldugunu anlayıp ona yeni token veriyor.

    }















    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.delete(REDIS_PREFIX + refreshToken);
        }
    }
}