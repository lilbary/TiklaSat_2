package com.gib.tiklasat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Dosya depolama servisi.
 * Şimdilik dosyaları yerel dosya sistemindeki "uploads" klasörüne kaydeder.
 * İleride AWS S3 veya Google Cloud Storage ile değiştirilebilir.
 */
@Service
public class FileStorageService {

    private final Path storageDirectory = Paths.get("uploads");

    public FileStorageService() {
        try {
            // "uploads" klasörü yoksa oluştur
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Depolama klasörü oluşturulamadı!", e);
        }
    }

    /**
     * Gelen dosyayı kaydeder ve benzersiz bir dosya adı (StorageKey) döndürür.
     */
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Boş dosya yüklenemez!");
        }

        try {
            // Dosya adını güvenli hale getir ve UUID ekle
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path destinationFilePath = storageDirectory.resolve(uniqueFilename);

            // Dosyayı kaydet
            file.transferTo(destinationFilePath.toFile());

            return uniqueFilename;
            
        } catch (IOException e) {
            throw new RuntimeException("Dosya kaydedilirken hata oluştu!", e);
        }
    }
}
