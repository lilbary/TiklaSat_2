package com.gib.tiklasat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Hoş geldin Patron! Bu sayfayı sadece Admin biletine (ROLE_ADMIN) sahip olanlar görebilir. Stateless (Veritabanı bağımsız) sistem başarıyla çalışıyor!");
    }
}
