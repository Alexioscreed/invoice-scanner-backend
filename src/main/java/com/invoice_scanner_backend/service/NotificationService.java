package com.invoice_scanner_backend.service;

import com.invoice_scanner_backend.entity.Notification;
import com.invoice_scanner_backend.entity.User;
import com.invoice_scanner_backend.repository.NotificationRepository;
import com.invoice_scanner_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(User user, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        
        // Convert string to enum
        try {
            notification.setType(Notification.NotificationType.valueOf(type));
        } catch (IllegalArgumentException e) {
            notification.setType(Notification.NotificationType.SYSTEM_ALERT);
        }
        
        return notificationRepository.save(notification);
    }

    public Notification createNotification(User user, String title, String message, 
                                         Notification.NotificationType type, 
                                         Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        
        return notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Page<Notification> getUserNotifications(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return getUserNotifications(user, pageable);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public long getUnreadNotificationCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return getUnreadNotificationCount(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        markAsRead(notificationId, user);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository
            .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        markAllAsRead(user);
    }

    public void deleteReadNotifications(User user) {
        notificationRepository.deleteByUserAndIsReadTrue(user);
    }

    public void deleteNotification(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notificationRepository.delete(notification);
    }

    public void clearAllNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        notificationRepository.deleteByUser(user);
    }

    public long getUnreadCount(String userEmail) {
        return getUnreadNotificationCount(userEmail);
    }

    public Notification createNotification(String userEmail, String title, String message, String type) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return createNotification(user, title, message, type);
    }

    // Helper methods for creating specific types of notifications
    public void notifyInvoiceProcessingComplete(User user, String invoiceNumber, Long invoiceId) {
        createNotification(
            user,
            "Invoice Processing Complete",
            "Invoice " + invoiceNumber + " has been successfully processed!",
            Notification.NotificationType.PROCESSING_COMPLETE,
            invoiceId,
            "INVOICE"
        );
    }

    public void notifyInvoiceProcessingFailed(User user, String invoiceNumber, Long invoiceId) {
        createNotification(
            user,
            "Invoice Processing Failed",
            "Failed to process invoice " + invoiceNumber + ". Please try again.",
            Notification.NotificationType.PROCESSING_FAILED,
            invoiceId,
            "INVOICE"
        );
    }

    public void notifyInvoiceUploaded(User user, String fileName) {
        createNotification(
            user,
            "Invoice Uploaded",
            "Invoice " + fileName + " has been uploaded and is being processed.",
            Notification.NotificationType.INVOICE_UPLOAD,
            null,
            null
        );
    }
}
