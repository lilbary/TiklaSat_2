package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AdminDashboardStatsDto;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.service.AdminService;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // Kayıtlı tüm kullanıcıları listeler — AdminUsersPage.jsx bunu kullanıyor
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/auctions/pending")
    public ResponseEntity<List<com.gib.tiklasat.dto.AuctionDto>> getPendingAuctions() {
        List<com.gib.tiklasat.dto.AuctionDto> dtos = adminService.getPendingAuctions().stream()
            .map(auction -> com.gib.tiklasat.dto.AuctionDto.fromEntity(auction, auction.getStartingPrice()))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/auctions/{id}/approve")
    public ResponseEntity<String> approveAuction(@PathVariable java.util.UUID id) {
        adminService.approveAuction(id);
        return ResponseEntity.ok("İlan onaylandı");
    }

    @PostMapping("/auctions/{id}/reject")
    public ResponseEntity<String> rejectAuction(@PathVariable java.util.UUID id) {
        adminService.rejectAuction(id);
        return ResponseEntity.ok("İlan reddedildi");
    }

    // ------------------------------------------------------------------
    // BR-U-008 — Rol yönetimi
    //
    // Bu iki endpoint /api/admin/** altında olduğu için SecurityConfig
    // tarafından zaten hasRole("ADMIN") ile korunuyor; ayrıca bir yetki
    // kontrolü yazmaya gerek yok.
    //
    // Authentication parametresini Spring kendisi dolduruyor; getName()
    // JWT'deki e-postayı veriyor. "Kendi rolünü kaldıramaz" kuralı için
    // işlemi yapanın kim olduğunu bilmek gerekiyor.
    // ------------------------------------------------------------------

    @PostMapping("/users/{userId}/roles/{role}")
    public ResponseEntity<UserDto> addRole(@PathVariable UUID userId,
                                           @PathVariable Role role,
                                           Authentication authentication) {
        return ResponseEntity.ok(adminService.addRole(userId, role, authentication.getName()));
    }

    @DeleteMapping("/users/{userId}/roles/{role}")
    public ResponseEntity<UserDto> removeRole(@PathVariable UUID userId,
                                              @PathVariable Role role,
                                              Authentication authentication) {
        return ResponseEntity.ok(adminService.removeRole(userId, role, authentication.getName()));
    }
}
