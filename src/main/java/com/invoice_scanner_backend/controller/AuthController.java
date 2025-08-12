package com.invoice_scanner_backend.controller;

import com.invoice_scanner_backend.dto.auth.*;
import com.invoice_scanner_backend.service.AuthService;
import com.invoice_scanner_backend.util.LogUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        String username = registerRequest.getEmail();
        try {
            long startTime = System.currentTimeMillis();
            AuthResponse response = authService.registerUser(registerRequest);
            long duration = System.currentTimeMillis() - startTime;
            
            LogUtils.logAuthEvent(logger, "registration", username, true);
            LogUtils.logApiSuccess(logger, "POST", "/auth/register", duration);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LogUtils.logAuthEvent(logger, "registration", username, false);
            logger.error("Registration failed: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        try {
            long startTime = System.currentTimeMillis();
            AuthResponse response = authService.authenticateUser(loginRequest);
            long duration = System.currentTimeMillis() - startTime;
            
            LogUtils.logAuthEvent(logger, "login", username, true);
            LogUtils.logApiSuccess(logger, "POST", "/auth/login", duration);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LogUtils.logAuthEvent(logger, "login", username, false);
            logger.error("Login failed: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        logger.info("👋 User logout request");
        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.info("📧 Email verification attempt with token: {}...", token.substring(0, Math.min(8, token.length())));
        try {
            authService.verifyEmail(token);
            logger.info("✅ Email verification successful");
            return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
        } catch (RuntimeException e) {
            logger.error("❌ Email verification failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("📨 Resend verification email request for: {}", emailRequest.getEmail());
        try {
            authService.resendVerificationEmail(emailRequest.getEmail());
            logger.info("✅ Verification email resent to: {}", emailRequest.getEmail());
            return ResponseEntity.ok(new MessageResponse("Verification email sent!"));
        } catch (RuntimeException e) {
            logger.error("❌ Failed to resend verification email to: {} - Error: {}", emailRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody EmailRequest emailRequest) {
        logger.info("🔑 Password reset request for email: {}", emailRequest.getEmail());
        try {
            authService.requestPasswordReset(emailRequest.getEmail());
            logger.info("✅ Password reset email sent to: {}", emailRequest.getEmail());
            return ResponseEntity.ok(new MessageResponse("Password reset email sent!"));
        } catch (RuntimeException e) {
            logger.error("❌ Password reset failed for email: {} - Error: {}", emailRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetRequest resetRequest) {
        logger.info("🔐 Password reset confirmation attempt with token: {}...", resetRequest.getToken().substring(0, Math.min(8, resetRequest.getToken().length())));
        try {
            authService.resetPassword(resetRequest.getToken(), resetRequest.getNewPassword());
            logger.info("✅ Password reset completed successfully");
            return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
        } catch (RuntimeException e) {
            logger.error("❌ Password reset confirmation failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
