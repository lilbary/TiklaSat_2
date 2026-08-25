package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Favorite;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.FavoriteRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addFavorite(UUID auctionId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new ResourceNotFoundException("Açık artırma bulunamadı!"));

        if (favoriteRepository.findByUserIdAndAuctionId(user.getId(), auctionId).isPresent()) {
            throw new ConflictException("Bu açık artırma zaten favorilerinizde!");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setAuction(auction);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(UUID auctionId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        Favorite favorite = favoriteRepository.findByUserIdAndAuctionId(user.getId(), auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bu açık artırma favorilerinizde değil!"));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<AuctionDto> getMyFavorites(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(f -> AuctionDto.fromEntity(f.getAuction(), f.getAuction().getCurrentPrice()))
                .collect(Collectors.toList());
    }
}
