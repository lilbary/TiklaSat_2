package com.gib.tiklasat.service;


import com.gib.tiklasat.dto.AdminDashboardStatsDto;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ForbiddenActionException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;

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
    @Transactional
    public void approveAuction(java.util.UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı"));
        auction.setStatus("ACTIVE");
        auctionRepository.save(auction);

        String message = "'" + auction.getListing().getTitle() + "' ilanınız onaylandı ve yayına girdi!";
        try {
            notificationService.createNotification(auction.getListing().getSeller(), auction, message);
        } catch (Exception e) {
            log.error("Bildirim oluşturulamadı, onay işlemi devam ediyor", e);
        }
    }
    @Transactional
    public void rejectAuction(java.util.UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı"));
        auction.setStatus("REJECTED");
        auctionRepository.save(auction);

        String message = "'" + auction.getListing().getTitle() + "' ilanınız reddedildi.";
        try {
            notificationService.createNotification(auction.getListing().getSeller(), auction, message);
        } catch (Exception e) {
            log.error("Bildirim oluşturulamadı, red işlemi devam ediyor", e);
        }
    }

    // ------------------------------------------------------------------
    // BR-U-008 — Rol atama / geri alma
    //
    // "Yalnızca ADMIN rol atayabilir" kısmı burada DEĞİL, SecurityConfig'de
    // çözülüyor: /api/admin/** yolu hasRole("ADMIN") ile korunuyor, dolayısıyla
    // bu metotlara admin olmayan biri hiç ulaşamıyor. Burada kalan iş, kuralın
    // ikinci yarısı: bir admin kendi admin rolünü kaldıramaz.
    // ------------------------------------------------------------------

    @Transactional
    public UserDto addRole(UUID targetUserId, Role role, String actingAdminEmail) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        target.addRole(role);
        log.info("ROL ATANDI - {} kullanıcısına {} rolü verildi (işlemi yapan: {})",
                target.getEmail(), role, actingAdminEmail);

        // Kullanıcı managed durumda; dirty checking commit sırasında INSERT'i atar.
        return UserDto.fromEntity(target);
    }

    @Transactional
    public UserDto removeRole(UUID targetUserId, Role role, String actingAdminEmail) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        // BR-U-008: kendi admin rolünü kaldıramaz.
        //
        // Kural ilk bakışta sadece bir "ayağına sıkma" koruması gibi duruyor ama
        // aslında sistemin adminsiz kalmasını da engelliyor: son admin kendi
        // rolünü kaldıramadığı için her zaman en az bir admin kalır.
        if (role == Role.ADMIN) {
            User actor = userRepository.findByEmail(actingAdminEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı!"));
            if (actor.getId().equals(target.getId())) {
                throw new ForbiddenActionException(
                        "Kendi admin rolünüzü kaldıramazsınız. Sistemde admin kalmaması riskine karşı bu işlem engellendi.");
            }
        }

        if (!target.hasRole(role)) {
            throw new ResourceNotFoundException("Kullanıcının böyle bir rolü yok: " + role);
        }

        target.getRoles().remove(role);
        log.info("ROL KALDIRILDI - {} kullanıcısından {} rolü alındı (işlemi yapan: {})",
                target.getEmail(), role, actingAdminEmail);

        return UserDto.fromEntity(target);
    }
}
