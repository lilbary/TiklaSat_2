package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AdminDashboardStatsDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/auctions/pending")
    public ResponseEntity<List<Auction>> getPendingAuctions() {
        // İsteğe bağlı: Auction'ı doğrudan dönmek yerine AdminPendingAuctionDto'ya çevirip dönebilirsiniz.
        return ResponseEntity.ok(adminService.getPendingAuctions());
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