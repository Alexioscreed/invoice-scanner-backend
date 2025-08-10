package com.invoice_scanner_backend.controller;

import com.invoice_scanner_backend.dto.auth.MessageResponse;
import com.invoice_scanner_backend.entity.Notification;
import com.invoice_scanner_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('USER')")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationService.getUserNotifications(userEmail, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.markAsRead(id, userEmail);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.deleteNotification(id, userEmail);
        return ResponseEntity.ok(new MessageResponse("Notification deleted successfully"));
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<MessageResponse> clearAllNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.clearAllNotifications(userEmail);
        return ResponseEntity.ok(new MessageResponse("All notifications cleared"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = notificationService.getUnreadCount(userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        String title = (String) request.get("title");
        String message = (String) request.get("message");
        String type = (String) request.getOrDefault("type", "SYSTEM_ALERT");
        
        Notification notification = notificationService.createNotification(userEmail, title, message, type);
        return ResponseEntity.ok(notification);
    }
}
