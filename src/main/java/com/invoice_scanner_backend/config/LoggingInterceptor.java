package com.invoice_scanner_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                           @NonNull HttpServletResponse response, 
                           @NonNull Object handler) {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        MDC.put(REQUEST_ID, requestId);
        MDC.put(START_TIME, String.valueOf(startTime));
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String clientIp = getClientIpAddress(request);
        
        logger.info("""
                
                ╔════════════════════════════════════════════════════════════
                ║ 🚀 INCOMING REQUEST
                ║ ────────────────────────────────────────────────────────────
                ║ 📅 Time     : {}
                ║ 🆔 Request  : {}
                ║ 🌐 Method   : {}
                ║ 📍 URL      : {}
                ║ 📡 IP       : {}
                ║ 🖥️  Agent    : {}
                ╚════════════════════════════════════════════════════════════
                """, 
                timestamp,
                requestId,
                request.getMethod(),
                request.getRequestURL(),
                clientIp,
                request.getHeader("User-Agent") != null ? request.getHeader("User-Agent").substring(0, Math.min(50, request.getHeader("User-Agent").length())) + "..." : "Unknown"
        );
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, 
                              @NonNull HttpServletResponse response, 
                              @NonNull Object handler, 
                              Exception ex) {
        
        String requestId = MDC.get(REQUEST_ID);
        long startTime = Long.parseLong(MDC.get(START_TIME));
        long duration = System.currentTimeMillis() - startTime;
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String statusEmoji = getStatusEmoji(response.getStatus());
        String durationColor = getDurationColor(duration);
        
        logger.info("""
                
                ╔════════════════════════════════════════════════════════════
                ║ ✅ RESPONSE COMPLETE
                ║ ────────────────────────────────────────────────────────────
                ║ 📅 Time     : {}
                ║ 🆔 Request  : {}
                ║ {} Status   : {} {}
                ║ ⏱️  Duration : {}{}ms
                {}╚════════════════════════════════════════════════════════════
                """, 
                timestamp,
                requestId,
                statusEmoji,
                response.getStatus(),
                getStatusText(response.getStatus()),
                durationColor,
                duration,
                ex != null ? "║ ❌ Exception: " + ex.getMessage() + "\n" : ""
        );
        
        MDC.clear();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "🔄";
        if (status >= 400 && status < 500) return "❌";
        if (status >= 500) return "💥";
        return "❓";
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "CREATED";
            case 400 -> "BAD REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT FOUND";
            case 500 -> "INTERNAL SERVER ERROR";
            default -> "HTTP " + status;
        };
    }

    private String getDurationColor(long duration) {
        if (duration < 100) return "🟢 ";
        if (duration < 500) return "🟡 ";
        if (duration < 1000) return "🟠 ";
        return "🔴 ";
    }
}
