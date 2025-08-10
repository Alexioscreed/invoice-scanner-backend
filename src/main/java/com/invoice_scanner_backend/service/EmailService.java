package com.invoice_scanner_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_FROM:noreply@invoicescanner.com}")
    private String fromEmail;

    @Value("${APP_URL:http://localhost:3000}")
    private String appUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Invoice Scanner - Email Verification");
            
            String verificationUrl = appUrl + "/verify-email?token=" + verificationToken;
            String content = "Dear User,\n\n" +
                    "Thank you for registering with Invoice Scanner!\n\n" +
                    "Please click the following link to verify your email address:\n" +
                    verificationUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not create this account, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Invoice Scanner Team";
            
            message.setText(content);
            mailSender.send(message);
            
            System.out.println("Verification email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Invoice Scanner - Password Reset");
            
            String resetUrl = appUrl + "/reset-password?token=" + resetToken;
            String content = "Dear User,\n\n" +
                    "You have requested to reset your password for your Invoice Scanner account.\n\n" +
                    "Please click the following link to reset your password:\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not request this password reset, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Invoice Scanner Team";
            
            message.setText(content);
            mailSender.send(message);
            
            System.out.println("Password reset email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void sendInvoiceProcessingNotification(String toEmail, String invoiceNumber, boolean success) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            
            if (success) {
                message.setSubject("Invoice Processing Complete - " + invoiceNumber);
                String content = "Dear User,\n\n" +
                        "Your invoice " + invoiceNumber + " has been successfully processed!\n\n" +
                        "You can now view the extracted information in your Invoice Scanner dashboard.\n\n" +
                        "Best regards,\n" +
                        "Invoice Scanner Team";
                message.setText(content);
            } else {
                message.setSubject("Invoice Processing Failed - " + invoiceNumber);
                String content = "Dear User,\n\n" +
                        "Unfortunately, we were unable to process your invoice " + invoiceNumber + ".\n\n" +
                        "Please try uploading the invoice again or contact support if the issue persists.\n\n" +
                        "Best regards,\n" +
                        "Invoice Scanner Team";
                message.setText(content);
            }
            
            mailSender.send(message);
            
            System.out.println("Invoice processing notification sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send invoice processing notification: " + e.getMessage());
        }
    }

    public void sendExportReadyNotification(String toEmail, String exportType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Invoice Export Ready - " + exportType.toUpperCase());
            
            String content = "Dear User,\n\n" +
                    "Your " + exportType.toUpperCase() + " export is ready for download!\n\n" +
                    "Please log into your Invoice Scanner dashboard to download your export file.\n\n" +
                    "Best regards,\n" +
                    "Invoice Scanner Team";
            
            message.setText(content);
            mailSender.send(message);
            
            System.out.println("Export ready notification sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send export ready notification: " + e.getMessage());
        }
    }
}
