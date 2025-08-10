package com.invoice_scanner_backend.repository;

import com.invoice_scanner_backend.entity.Notification;
import com.invoice_scanner_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    long countByUserAndIsReadFalse(User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false")
    List<Notification> findUnreadNotificationsByUser(@Param("user") User user);
    
    void deleteByUserAndIsReadTrue(User user);
    
    void deleteByUser(User user);
}
