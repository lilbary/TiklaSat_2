package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AuthResponse;
import com.gib.tiklasat.dto.LoginRequest;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.UserRepository;
import com.gib.tiklasat.security.JwtService;
import com.gib.tiklasat.security.RefreshTokenService;
import com.gib.tiklasat.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gib.tiklasat.dto.ForgotPasswordRequest;
import com.gib.tiklasat.dto.ResetPasswordRequest;
import com.gib.tiklasat.security.PasswordResetTokenService;
import com.gib.tiklasat.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor //gerekli consturctorları ekliyor alttaki final ile olanlar.
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegisterDto request) {
        return ResponseEntity.ok(authService.register(request));
    }








    @PostMapping("/login")//KUllANICI GİRİS YAPA BASTIGINDA BU CALISIR
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        //@RequestBody LoginRequest request = REACTTAN GELEN EPOSTA-SİFRE-BENİHATIRLA BİLGİSİNİ request'e VERİR
        //HttpServletResponse response = TARAYICIYA COOKİE YERLESTİRMEK GEREKTİGİ İCİN response OBJESİNİN YONETİYOZ

        //1-SİFRE KONTROLU
        AuthResponse authResponse = authService.login(request);

        // 2. Refresh Token üret
        String refreshToken = refreshTokenService.createRefreshToken(authResponse.getUser().getId(), request.isRememberMe());

        // 3. Güvenli Cookie'yi oluştur TARAYICIYA

        long maxAge = request.isRememberMe() ? (30L * 24 * 60 * 60) : -1; // 30 gün veya Session
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Kalkan 1: JavaScript bu çerezi okuyamaz!(HACKLENEMEZ)
                .secure(false)  // Kalkan 2: Sadece HTTPS'de çalışır (Canlıda true olmalı)
                .path("/api/auth") // Kalkan 3: Sadece güvenlik (auth) sayfalarına giderken bu çerezi yolla, ilanları çekerken yollama.
                .maxAge(maxAge)// Ömür: 30 Gün veya Tarayıcı kapanana kadar.
                .sameSite("Lax") // Kalkan 4: Başka sitelerden bizim sitemize atılan sahte istekleri engeller
                .build();
        //URETTİGİMİZ BİLETİ ETRAFINI BU KODLARLA(KALKANLARLA) SARIP AŞILMAZ BİR COOKİE YE CEVİRİYORUZ


        //cEREZİ VE COOKİYİ YOLLAMA
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        //response.addHeader= CEREZİ TARAYICIYA FIRLATMAMIZA YARAR
        return ResponseEntity.ok(authResponse); // İçinde kısa ömürlü Access Token var
    }




    @PostMapping("/refresh") //KULLANICIN 15DKLIK TOKENI BİTTİGİNDE BU CALISIR
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
            //@CookieValue = çerezi refreshToken ismiyle otomatik yakalıyoruz.
            //REQUİRED ==  yoksa siteyi cokertme devam et. zorunlu degil anlamında.

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        try {
            // 1. Redisten çerezi verip ID'yi alıyoruz
            UUID userId = refreshTokenService.validateAndGetUserId(refreshToken);

            // 2. Veritabanından o ID'ye ait kullanıcının güncel bilgilerini alıyoruz
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            // 3. O adama YEPYENİ bir 15 dakikalık Access Token üretiyoruz
            String newAccessToken = jwtService.generateToken(user);

            // 4. Yeni bileti JSON olarak React'e fırlatıyoruz
            return ResponseEntity.ok(new AuthResponse(newAccessToken, UserDto.fromEntity(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

//niye cookie kulalnıoz





    //Kullanıcı "Çıkış Yap" dediğinde hem sunucudaki kaydı hem de tarayıcıdaki çerezi yok etmemiz gerekir.

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // Redis'ten sil
        refreshTokenService.deleteRefreshToken(refreshToken);

        // 2. Tarayıcı Temizliği: Tarayıcıya müdahale edip çerezi silmesi için ona
        // "içi boş" ve "ömrü 0 saniye" (maxAge=0) olan sahte bir çerez yollarız.
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Production'da true
                .path("/api/auth")
                .maxAge(0) // *Hemen silinmesini sağlar*
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok().build();
    }

    // ŞİFREMİ UNUTTUM - Kullanıcı e-postasını girer, biz link atarız
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // 1. Bu e-posta sistemde kayıtlı mı?
        var userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            // GÜVENLİK: Kullanıcıya "Bu mail kayıtlı değil" DEMİYORUZ!
            // Çünkü hacker hangi maillerin sistemde olduğunu öğrenmesin.
            // Her durumda aynı mesajı dönüyoruz.
            return ResponseEntity.ok("Eğer bu e-posta kayıtlıysa, şifre sıfırlama bağlantısı gönderildi.");
        }

        // 2. 15 dakikalık token üret ve Redis'e kaydet
        String token = passwordResetTokenService.createToken(request.getEmail());

        // 3. Kullanıcıya mail at
        mailService.sendPasswordResetEmail(request.getEmail(), token);

        return ResponseEntity.ok("Eğer bu e-posta kayıtlıysa, şifre sıfırlama bağlantısı gönderildi.");
    }

    // ŞİFRE SIFIRLAMA - Kullanıcı linke tıklayıp yeni şifresini girer
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        // 1. Token'ı Redis'ten doğrula ve e-posta adresini al
        String email = passwordResetTokenService.validateAndGetEmail(request.getToken());

        // 2. Kullanıcıyı veritabanından bul
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 3. Yeni şifreyi kriptolayarak kaydet
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. Token'ı sil (Aynı link ikinci kez kullanılmasın)
        passwordResetTokenService.deleteToken(request.getToken());

        return ResponseEntity.ok("Şifreniz başarıyla güncellendi. Giriş yapabilirsiniz.");
    }











    //jknx nmli qctj ftnc
}