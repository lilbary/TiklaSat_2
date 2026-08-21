package com.gib.tiklasat.service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
@Service
public class FileStorageService {

    // Dosyaların kaydedileceği klasör
    private final String uploadDir = "uploads/";
    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // Klasör yoksa oluştur
            }

            // Çakışmayı önlemek için benzersiz (UUID) dosya adı üret
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath); // Dosyayı kaydet

            // Veritabanına yazılacak URL kısmını dön
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Dosya kaydedilemedi!", e);
        }
    }
}