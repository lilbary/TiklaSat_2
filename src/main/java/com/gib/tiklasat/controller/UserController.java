package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.*;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        String currentUserEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserProfile(currentUserEmail));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(Authentication authentication, @RequestBody UserProfileUpdateDto dto) {
        String currentUserEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(currentUserEmail, dto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @RequestBody ChangePasswordDto dto) {
        String currentUserEmail = (String) authentication.getPrincipal();
        userService.changePassword(currentUserEmail, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicProfileDto> getPublicProfile(@PathVariable UUID id){
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

}
