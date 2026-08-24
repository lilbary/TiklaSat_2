package com.gib.tiklasat.controller;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor // UserService'i otomatik bağlamak için gerekli
public class AdminController {
    private final UserService userService;
    // Mevcut test ucu
    @GetMapping("/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Hoş geldin Patron! Bu sayfayı sadece Admin görebilir.");
    }
    // YENİ EKLENEN KISIM: Tüm kullanıcıları getiren API ucu
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}