package com.invoice_scanner_backend.service;

import com.invoice_scanner_backend.entity.Invoice;
import com.invoice_scanner_backend.entity.InvoiceLineItem;
import com.invoice_scanner_backend.entity.User;
import com.invoice_scanner_backend.repository.InvoiceRepository;
import com.invoice_scanner_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private NotificationService notificationService;

    public List<Invoice> getAllInvoices(String userEmail) {
        User user = getUserByEmail(userEmail);
        return invoiceRepository.findByUser(user, Pageable.unpaged()).getContent();
    }

    public Page<Invoice> getInvoicesPage(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return invoiceRepository.findByUser(user, pageable);
    }

    public Optional<Invoice> getInvoiceById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        return invoiceRepository.findById(id)
            .filter(invoice -> invoice.getUser().getId().equals(user.getId()));
    }

    public Invoice createInvoice(Invoice invoice, String userEmail) {
        User user = getUserByEmail(userEmail);
        invoice.setUser(user);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        // Calculate total amount from line items
        if (invoice.getLineItems() != null && !invoice.getLineItems().isEmpty()) {
            BigDecimal totalAmount = invoice.getLineItems().stream()
                    .map(item -> item.getQuantity().multiply(item.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setTotalAmount(totalAmount);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Send notification
        notificationService.createNotification(
            userEmail,
            "Invoice Created",
            "Invoice " + savedInvoice.getInvoiceNumber() + " has been created successfully",
            "INVOICE_CREATED"
        );

        return savedInvoice;
    }

    public Invoice updateInvoice(Long id, Invoice invoiceDetails, String userEmail) {
        User user = getUserByEmail(userEmail);
        Invoice invoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Update invoice fields
        invoice.setInvoiceNumber(invoiceDetails.getInvoiceNumber());
        invoice.setVendorName(invoiceDetails.getVendorName());
        invoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setTotalAmount(invoiceDetails.getTotalAmount());
        invoice.setProcessingStatus(invoiceDetails.getProcessingStatus());
        invoice.setCategory(invoiceDetails.getCategory());
        invoice.setNotes(invoiceDetails.getNotes());

        // Update line items if provided
        if (invoiceDetails.getLineItems() != null) {
            invoice.getLineItems().clear();
            for (InvoiceLineItem item : invoiceDetails.getLineItems()) {
                item.setInvoice(invoice);
                invoice.getLineItems().add(item);
            }
            
            // Recalculate total amount
            BigDecimal totalAmount = invoice.getLineItems().stream()
                    .map(item -> item.getQuantity().multiply(item.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setTotalAmount(totalAmount);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Send notification
        notificationService.createNotification(
            userEmail,
            "Invoice Updated",
            "Invoice " + savedInvoice.getInvoiceNumber() + " has been updated",
            "INVOICE_UPDATED"
        );

        return savedInvoice;
    }

    public void deleteInvoice(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Invoice invoice = invoiceRepository.findById(id)
                .filter(inv -> inv.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Delete associated file if exists
        if (invoice.getFilePath() != null) {
            fileStorageService.deleteFile(invoice.getFilePath());
        }

        invoiceRepository.delete(invoice);
        
        // Send notification
        notificationService.createNotification(
            userEmail,
            "Invoice Deleted",
            "Invoice " + invoice.getInvoiceNumber() + " has been deleted",
            "INVOICE_DELETED"
        );
    }

    public Invoice uploadInvoiceFile(MultipartFile file, String userEmail) {
        try {
            // Store the file
            String filePath = fileStorageService.storeFile(file);
            
            // Create invoice with basic info
            Invoice invoice = new Invoice();
            User user = getUserByEmail(userEmail);
            invoice.setUser(user);
            invoice.setFilePath(filePath);
            invoice.setProcessingStatus(Invoice.ProcessingStatus.PROCESSING);
            invoice.setOriginalFilename(file.getOriginalFilename());
            invoice.setFileSize(file.getSize());
            invoice.setMimeType(file.getContentType());
            
            // Generate temporary invoice number
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            
            // Process OCR asynchronously
            processOcrData(savedInvoice.getId(), filePath);
            
            return savedInvoice;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload invoice file: " + e.getMessage());
        }
    }

    @Transactional
    public void processOcrData(Long invoiceId, String filePath) {
        try {
            // Extract data using OCR
            Map<String, Object> ocrData = ocrService.extractInvoiceData(filePath);
            
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
            
            // Update invoice with OCR data
            if (ocrData.containsKey("invoiceNumber")) {
                invoice.setInvoiceNumber((String) ocrData.get("invoiceNumber"));
            }
            if (ocrData.containsKey("vendor")) {
                invoice.setVendorName((String) ocrData.get("vendor"));
            }
            if (ocrData.containsKey("invoiceDate")) {
                invoice.setInvoiceDate((LocalDate) ocrData.get("invoiceDate"));
            }
            if (ocrData.containsKey("dueDate")) {
                invoice.setDueDate((LocalDate) ocrData.get("dueDate"));
            }
            if (ocrData.containsKey("totalAmount")) {
                invoice.setTotalAmount((BigDecimal) ocrData.get("totalAmount"));
            }
            
            // Add line items if extracted
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lineItemsData = (List<Map<String, Object>>) ocrData.get("lineItems");
            if (lineItemsData != null) {
                for (Map<String, Object> itemData : lineItemsData) {
                    InvoiceLineItem lineItem = new InvoiceLineItem();
                    lineItem.setInvoice(invoice);
                    lineItem.setDescription((String) itemData.get("description"));
                    lineItem.setQuantity((BigDecimal) itemData.get("quantity"));
                    lineItem.setUnitPrice((BigDecimal) itemData.get("unitPrice"));
                    invoice.getLineItems().add(lineItem);
                }
            }
            
            invoice.setProcessingStatus(Invoice.ProcessingStatus.COMPLETED);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            // Send notification
            notificationService.createNotification(
                invoice.getUser().getEmail(),
                "Invoice Processing Complete",
                "Invoice " + invoice.getInvoiceNumber() + " has been processed successfully",
                "INVOICE_PROCESSED"
            );
            
        } catch (Exception e) {
            // Update status to failed
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice != null) {
                invoice.setProcessingStatus(Invoice.ProcessingStatus.FAILED);
                invoiceRepository.save(invoice);
                
                notificationService.createNotification(
                    invoice.getUser().getEmail(),
                    "Invoice Processing Failed",
                    "Failed to process invoice: " + e.getMessage(),
                    "INVOICE_PROCESSING_FAILED"
                );
            }
        }
    }

    public List<Invoice> searchInvoices(String userEmail, String searchTerm, String vendorName, 
                                       String category, LocalDate startDate, LocalDate endDate) {
        User user = getUserByEmail(userEmail);
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return invoiceRepository.searchInvoices(user, searchTerm, Pageable.unpaged()).getContent();
        }
        // If no search term, return all invoices for the user
        return invoiceRepository.findByUser(user, Pageable.unpaged()).getContent();
    }

    public List<String> getInvoiceCategories(String userEmail) {
        User user = getUserByEmail(userEmail);
        return invoiceRepository.findDistinctCategoriesByUser(user);
    }

    public List<String> getInvoiceVendors(String userEmail) {
        User user = getUserByEmail(userEmail);
        return invoiceRepository.findDistinctVendorNamesByUser(user);
    }

    public byte[] exportInvoices(String userEmail, String format, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices;
        User user = getUserByEmail(userEmail);
        
        if (startDate != null && endDate != null) {
            invoices = invoiceRepository.findByUserAndInvoiceDateBetween(user, startDate, endDate, Pageable.unpaged()).getContent();
        } else {
            invoices = invoiceRepository.findByUser(user, Pageable.unpaged()).getContent();
        }

        if ("csv".equalsIgnoreCase(format)) {
            return exportToCsv(invoices);
        } else if ("excel".equalsIgnoreCase(format)) {
            // Excel export not implemented - return CSV format instead
            return exportToCsv(invoices);
        } else {
            throw new RuntimeException("Unsupported export format: " + format);
        }
    }

    public Map<String, Object> getInvoiceAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = getUserByEmail(userEmail);
        Map<String, Object> analytics = new HashMap<>();
        
        // Get total count
        analytics.put("totalInvoices", invoiceRepository.countByUser(user));
        
        // Get total amount for date range
        if (startDate != null && endDate != null) {
            BigDecimal totalAmount = invoiceRepository.sumTotalAmountByUserAndDateRange(user, startDate, endDate);
            analytics.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
            
            // Get vendor spending
            List<Object[]> vendorSpending = invoiceRepository.getVendorSpendingByUserAndDateRange(user, startDate, endDate);
            analytics.put("vendorSpending", vendorSpending);
            
            // Get category spending
            List<Object[]> categorySpending = invoiceRepository.getCategorySpendingByUserAndDateRange(user, startDate, endDate);
            analytics.put("categorySpending", categorySpending);
        }
        
        // Get monthly spending
        List<Object[]> monthlySpending = invoiceRepository.getMonthlySpendingByUser(user);
        analytics.put("monthlySpending", monthlySpending);
        
        return analytics;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private byte[] exportToCsv(List<Invoice> invoices) {
        StringBuilder csv = new StringBuilder();
        csv.append("Invoice Number,Vendor,Invoice Date,Due Date,Total Amount,Status,Category,Notes\n");
        
        for (Invoice invoice : invoices) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                invoice.getInvoiceNumber(),
                invoice.getVendorName(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getProcessingStatus(),
                invoice.getCategory(),
                invoice.getNotes() != null ? invoice.getNotes().replace(",", ";") : ""
            ));
        }
        
        return csv.toString().getBytes();
    }
}
