package com.invoice_scanner_backend.service;

import com.invoice_scanner_backend.dto.user.ChangePasswordRequest;
import com.invoice_scanner_backend.dto.user.UpdateProfileRequest;
import com.invoice_scanner_backend.entity.User;
import com.invoice_scanner_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = getUserByEmail(currentEmail);
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Check if email is being changed and if new email already exists
        if (!currentEmail.equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }
        
        return userRepository.save(user);
    }

    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = getUserByEmail(userEmail);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
