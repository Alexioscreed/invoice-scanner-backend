package com.invoice_scanner_backend.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    // Mock OCR implementation - In production, this would integrate with
    // services like Tesseract, Google Vision API, AWS Textract, etc.

    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile(
        "(?i)(?:invoice\\s*(?:number|no\\.?|#)?\\s*:?\\s*)([A-Z0-9-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?i)(?:total|amount|balance)?\\s*(?:due)?\\s*:?\\s*\\$?([0-9,]+\\.?[0-9]*)"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(?i)(?:date|dated)\\s*:?\\s*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})"
    );

    private static final Pattern VENDOR_PATTERN = Pattern.compile(
        "(?i)(?:from|vendor|company|bill\\s*to)\\s*:?\\s*([A-Z][a-zA-Z\\s&.,'-]+)",
        Pattern.CASE_INSENSITIVE
    );

    public Map<String, Object> extractInvoiceData(String filePath) {
        try {
            // Mock OCR text extraction - replace with actual OCR service
            String extractedText = performOcrExtraction(filePath);
            
            Map<String, Object> invoiceData = new HashMap<>();
            
            // Extract invoice number
            String invoiceNumber = extractInvoiceNumber(extractedText);
            if (invoiceNumber != null) {
                invoiceData.put("invoiceNumber", invoiceNumber);
            }
            
            // Extract vendor
            String vendor = extractVendor(extractedText);
            if (vendor != null) {
                invoiceData.put("vendor", vendor);
            }
            
            // Extract total amount
            BigDecimal totalAmount = extractTotalAmount(extractedText);
            if (totalAmount != null) {
                invoiceData.put("totalAmount", totalAmount);
            }
            
            // Extract dates
            LocalDate invoiceDate = extractInvoiceDate(extractedText);
            if (invoiceDate != null) {
                invoiceData.put("invoiceDate", invoiceDate);
            }
            
            LocalDate dueDate = extractDueDate(extractedText);
            if (dueDate != null) {
                invoiceData.put("dueDate", dueDate);
            }
            
            // Extract line items
            List<Map<String, Object>> lineItems = extractLineItems(extractedText);
            if (!lineItems.isEmpty()) {
                invoiceData.put("lineItems", lineItems);
            }
            
            return invoiceData;
            
        } catch (Exception e) {
            throw new RuntimeException("OCR extraction failed: " + e.getMessage(), e);
        }
    }

    private String performOcrExtraction(String filePath) {
        // Mock implementation - replace with actual OCR service
        // This would call Tesseract, Google Vision API, AWS Textract, etc.
        
        // Mock extracted text for demonstration
        return """
            ACME Corporation
            123 Business Street
            City, State 12345
            
            INVOICE
            
            Invoice Number: INV-2024-001
            Invoice Date: 01/15/2024
            Due Date: 02/15/2024
            
            Bill To:
            John Doe Company
            456 Customer Ave
            Customer City, State 67890
            
            Description                 Qty    Unit Price    Total
            Consulting Services         10     $150.00       $1,500.00
            Software License            1      $500.00       $500.00
            Support Package             12     $50.00        $600.00
            
            Subtotal:                                        $2,600.00
            Tax (8.5%):                                      $221.00
            Total Amount:                                    $2,821.00
            
            Payment Terms: Net 30
            """;
    }

    private String extractInvoiceNumber(String text) {
        Matcher matcher = INVOICE_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractVendor(String text) {
        // Look for company name at the beginning of the document
        String[] lines = text.split("\\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && 
                !line.toLowerCase().contains("invoice") &&
                !line.toLowerCase().contains("address") &&
                line.length() > 3 &&
                Character.isUpperCase(line.charAt(0))) {
                return line;
            }
        }
        
        // Fallback to pattern matching
        Matcher matcher = VENDOR_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }

    private BigDecimal extractTotalAmount(String text) {
        // Look for total amount patterns
        Pattern totalPattern = Pattern.compile(
            "(?i)(?:total|grand\\s*total|amount\\s*due|final\\s*amount)\\s*:?\\s*\\$?([0-9,]+\\.?[0-9]*)"
        );
        
        Matcher matcher = totalPattern.matcher(text);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            try {
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                // Ignore and continue
            }
        }
        
        return null;
    }

    private LocalDate extractInvoiceDate(String text) {
        Pattern invoiceDatePattern = Pattern.compile(
            "(?i)invoice\\s*date\\s*:?\\s*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})"
        );
        
        Matcher matcher = invoiceDatePattern.matcher(text);
        if (matcher.find()) {
            return parseDate(matcher.group(1));
        }
        
        return null;
    }

    private LocalDate extractDueDate(String text) {
        Pattern dueDatePattern = Pattern.compile(
            "(?i)due\\s*date\\s*:?\\s*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})"
        );
        
        Matcher matcher = dueDatePattern.matcher(text);
        if (matcher.find()) {
            return parseDate(matcher.group(1));
        }
        
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            // Try different date formats
            String[] formats = {"MM/dd/yyyy", "MM-dd-yyyy", "dd/MM/yyyy", "dd-MM-yyyy"};
            
            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    return LocalDate.parse(dateStr, formatter);
                } catch (Exception e) {
                    // Try next format
                }
            }
        } catch (Exception e) {
            // Ignore date parsing errors
        }
        
        return null;
    }

    private List<Map<String, Object>> extractLineItems(String text) {
        List<Map<String, Object>> lineItems = new ArrayList<>();
        
        // Look for table-like structure
        String[] lines = text.split("\\n");
        boolean inItemsSection = false;
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines
            if (line.isEmpty()) continue;
            
            // Look for table headers
            if (line.toLowerCase().contains("description") && 
                (line.toLowerCase().contains("qty") || line.toLowerCase().contains("quantity")) &&
                line.toLowerCase().contains("price")) {
                inItemsSection = true;
                continue;
            }
            
            // Stop at totals section
            if (line.toLowerCase().contains("subtotal") || 
                line.toLowerCase().contains("total") ||
                line.toLowerCase().contains("tax")) {
                inItemsSection = false;
                continue;
            }
            
            // Extract line items
            if (inItemsSection && isLineItem(line)) {
                Map<String, Object> lineItem = parseLineItem(line);
                if (lineItem != null) {
                    lineItems.add(lineItem);
                }
            }
        }
        
        return lineItems;
    }

    private boolean isLineItem(String line) {
        // Check if line contains typical line item patterns
        return line.matches(".*\\$[0-9,]+\\.?[0-9]*.*") && 
               line.split("\\s+").length >= 3;
    }

    private Map<String, Object> parseLineItem(String line) {
        try {
            // Basic parsing - would need more sophisticated logic for production
            String[] parts = line.split("\\s+");
            
            Map<String, Object> lineItem = new HashMap<>();
            
            // Extract description (first part)
            StringBuilder description = new StringBuilder();
            int descriptionEnd = 0;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("[0-9]+") || parts[i].startsWith("$")) {
                    descriptionEnd = i;
                    break;
                }
                if (description.length() > 0) description.append(" ");
                description.append(parts[i]);
            }
            
            if (description.length() > 0) {
                lineItem.put("description", description.toString());
            }
            
            // Extract quantity and prices from remaining parts
            for (int i = descriptionEnd; i < parts.length; i++) {
                String part = parts[i];
                
                // Try to parse as quantity
                if (part.matches("[0-9]+") && !lineItem.containsKey("quantity")) {
                    lineItem.put("quantity", new BigDecimal(part));
                }
                
                // Try to parse as price
                if (part.startsWith("$")) {
                    String priceStr = part.substring(1).replace(",", "");
                    try {
                        BigDecimal price = new BigDecimal(priceStr);
                        if (!lineItem.containsKey("unitPrice")) {
                            lineItem.put("unitPrice", price);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid price
                    }
                }
            }
            
            // Only return if we have minimum required fields
            if (lineItem.containsKey("description") && 
                lineItem.containsKey("quantity") && 
                lineItem.containsKey("unitPrice")) {
                return lineItem;
            }
            
        } catch (Exception e) {
            // Ignore parsing errors for individual line items
        }
        
        return null;
    }

    public boolean isOcrSupported(String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();
        return extension.equals(".pdf") || 
               extension.equals(".png") || 
               extension.equals(".jpg") || 
               extension.equals(".jpeg");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
