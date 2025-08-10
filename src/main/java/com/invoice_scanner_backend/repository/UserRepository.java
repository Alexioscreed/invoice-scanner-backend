package com.invoice_scanner_backend.repository;

import com.invoice_scanner_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    Optional<User> findByResetToken(String resetToken);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    java.util.List<User> findUnverifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();
}
