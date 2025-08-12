package com.invoice_scanner_backend.util;

import org.slf4j.Logger;

/**
 * Utility class for improved logging messages.
 * Provides standardized formatting for common log scenarios.
 */
public class LogUtils {

    /**
     * Log a successful API request
     * @param logger The SLF4J logger
     * @param method HTTP method
     * @param endpoint API endpoint
     * @param duration Duration in milliseconds
     */
    public static void logApiSuccess(Logger logger, String method, String endpoint, long duration) {
        logger.info("✓ {} {} completed in {}ms", method, endpoint, duration);
    }
    
    /**
     * Log a failed API request
     * @param logger The SLF4J logger
     * @param method HTTP method
     * @param endpoint API endpoint
     * @param status HTTP status code
     * @param error Error message
     */
    public static void logApiError(Logger logger, String method, String endpoint, int status, String error) {
        logger.error("✗ {} {} failed with status {}: {}", method, endpoint, status, error);
    }
    
    /**
     * Log successful data processing
     * @param logger The SLF4J logger
     * @param operation The operation name
     * @param entityType The type of entity
     * @param identifier The entity identifier
     */
    public static void logProcessSuccess(Logger logger, String operation, String entityType, String identifier) {
        logger.info("✓ {} {} {}", operation, entityType, identifier);
    }
    
    /**
     * Log authentication related events
     * @param logger The SLF4J logger
     * @param event Event description
     * @param username Username
     * @param success Whether authentication was successful
     */
    public static void logAuthEvent(Logger logger, String event, String username, boolean success) {
        String status = success ? "succeeded" : "failed";
        String symbol = success ? "✓" : "✗";
        
        logger.info("{} Auth {} for user '{}' {}", symbol, event, username, status);
    }
    
    /**
     * Format file size in human readable form
     * @param bytes Size in bytes
     * @return Formatted size string
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
