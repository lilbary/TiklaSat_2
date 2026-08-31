package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.NotificationDto;
import com.gib.tiklasat.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/mine")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getMyNotifications(userEmail));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
    String userEmail = (String) authentication.getPrincipal();
    notificationService.markAllAsRead(userEmail);
    return ResponseEntity.ok().build();
    }
}