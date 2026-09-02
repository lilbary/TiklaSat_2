package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AdminDashboardStatsDto;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.service.AdminService;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}