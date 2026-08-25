package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PutMapping;
import com.gib.tiklasat.dto.UserProfileUpdateDto;
import com.gib.tiklasat.dto.ChangePasswordDto;

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
}
