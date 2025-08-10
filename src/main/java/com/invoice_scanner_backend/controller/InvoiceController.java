package com.invoice_scanner_backend.controller;

import com.invoice_scanner_backend.dto.auth.MessageResponse;
import com.invoice_scanner_backend.entity.Invoice;
import com.invoice_scanner_backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasRole('USER')")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<Invoice>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        
        List<Invoice> invoices = invoiceService.searchInvoices(
            userEmail, search, vendorName, category, startDate, endDate
        );
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<Invoice> invoice = invoiceService.getInvoiceById(id, userEmail);
        return invoice.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody Invoice invoice, Authentication authentication) {
        String userEmail = authentication.getName();
        Invoice createdInvoice = invoiceService.createInvoice(invoice, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody Invoice invoice,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Invoice updatedInvoice = invoiceService.updateInvoice(id, invoice, userEmail);
        return ResponseEntity.ok(updatedInvoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteInvoice(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        invoiceService.deleteInvoice(id, userEmail);
        return ResponseEntity.ok(new MessageResponse("Invoice deleted successfully"));
    }

    @PostMapping("/upload")
    public ResponseEntity<Invoice> uploadInvoice(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Invoice invoice = invoiceService.uploadInvoiceFile(file, userEmail);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(Authentication authentication) {
        String userEmail = authentication.getName();
        List<String> categories = invoiceService.getInvoiceCategories(userEmail);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/processing-status/{id}")
    public ResponseEntity<Map<String, Object>> getProcessingStatus(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<Invoice> invoice = invoiceService.getInvoiceById(id, userEmail);
        
        if (invoice.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("id", id);
        status.put("status", invoice.get().getProcessingStatus().toString());
        status.put("confidenceScore", invoice.get().getConfidenceScore());
        status.put("completed", true);
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/vendors")
    public ResponseEntity<List<String>> getVendors(Authentication authentication) {
        String userEmail = authentication.getName();
        List<String> vendors = invoiceService.getInvoiceVendors(userEmail);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportInvoices(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        
        byte[] exportData = invoiceService.exportInvoices(
            userEmail, format, startDate, endDate
        );
        
        String filename = "invoices_export." + format;
        String contentType = format.equals("pdf") ? "application/pdf" : "text/csv";
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(exportData);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Map<String, Object> analytics = invoiceService.getInvoiceAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
}
