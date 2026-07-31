package com.gib.tiklasat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String sistemTesti() {
        return "TıklaSat Backend Sistemi Aktif ve Çalışıyor!";
    }
}