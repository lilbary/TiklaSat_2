package com.gib.tiklasat.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * BR-U-006'nın kara liste kısmı.
 *
 * Liste uygulama açılışında BİR KEZ okunup bellekte bir HashSet'te tutuluyor.
 * Böylece her kayıt denemesinde dosya okuma ya da veritabanı sorgusu olmuyor;
 * kontrol O(1). 10.000 parola bellekte yaklaşık 1 MB yer kaplar — kayıt
 * trafiği ne kadar artarsa artsın bu maliyet sabit kalır.
 */
@Slf4j
@Component
public class PasswordBlacklist {

    private static final String RESOURCE = "common-passwords.txt";

    private Set<String> blacklist = Set.of();

    @PostConstruct
    void load() {
        Set<String> loaded = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(RESOURCE).getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String entry = line.trim().toLowerCase();
                if (entry.isEmpty() || entry.startsWith("#")) continue;
                loaded.add(entry);
            }
        } catch (Exception e) {
            // Liste okunamazsa kayıt akışını tamamen kilitlemek yerine boş listeyle
            // devam ediyoruz: uzunluk ve karakter çeşitliliği kuralları hâlâ işliyor.
            log.error("Parola kara listesi yüklenemedi, kara liste kontrolü devre dışı", e);
        }
        this.blacklist = loaded;
        log.info("Parola kara listesi yüklendi: {} kayıt", loaded.size());
    }

    /** Parola listede mi? Karşılaştırma büyük/küçük harf duyarsız. */
    public boolean contains(String password) {
        if (password == null) return false;
        return blacklist.contains(password.toLowerCase());
    }

    public int size() {
        return blacklist.size();
    }
}
