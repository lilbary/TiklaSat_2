package com.gib.tiklasat.service;


import com.gib.tiklasat.dto.AdminDashboardStatsDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public AdminDashboardStatsDto getDashboardStats(){
        Long totalUsers = userRepository.count();
        Long activeAuctions = auctionRepository.countByStatus("ACTIVE");
        Long dailyBids = bidRepository.countDailyBids();

        AdminDashboardStatsDto dto = new AdminDashboardStatsDto();
        dto.setTotalSales(BigDecimal.ZERO); // İleride ciro hesaplaması ekleyebilirsiniz
        dto.setTotalUsers(totalUsers);
        dto.setActiveAuctions(activeAuctions);
        dto.setDailyBids(dailyBids);
        return dto;
    }

    public List<Auction> getPendingAuctions() {
        return auctionRepository.findByStatus("PENDING");
    }
    public void approveAuction(java.util.UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı"));
        auction.setStatus("ACTIVE");
        auctionRepository.save(auction);
    }
    public void rejectAuction(java.util.UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı"));
        auction.setStatus("REJECTED");
        auctionRepository.save(auction);
    }
}
