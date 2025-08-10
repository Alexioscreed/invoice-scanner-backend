package com.invoice_scanner_backend.controller;

import com.invoice_scanner_backend.dto.auth.MessageResponse;
import com.invoice_scanner_backend.dto.user.ChangePasswordRequest;
import com.invoice_scanner_backend.dto.user.UpdateProfileRequest;
import com.invoice_scanner_backend.entity.User;
import com.invoice_scanner_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.getUserByEmail(userEmail);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        User updatedUser = userService.updateProfile(userEmail, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        userService.changePassword(userEmail, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
