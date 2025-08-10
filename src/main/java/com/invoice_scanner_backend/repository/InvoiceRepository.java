package com.invoice_scanner_backend.repository;

import com.invoice_scanner_backend.entity.Invoice;
import com.invoice_scanner_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Page<Invoice> findByUser(User user, Pageable pageable);
    
    Page<Invoice> findByUserAndVendorNameContainingIgnoreCase(User user, String vendorName, Pageable pageable);
    
    Page<Invoice> findByUserAndInvoiceNumberContainingIgnoreCase(User user, String invoiceNumber, Pageable pageable);
    
    Page<Invoice> findByUserAndCategory(User user, String category, Pageable pageable);
    
    Page<Invoice> findByUserAndInvoiceDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    Page<Invoice> findByUserAndTotalAmountBetween(User user, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND " +
           "(LOWER(i.vendorName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.ocrRawText) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Invoice> searchInvoices(@Param("user") User user, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT DISTINCT i.category FROM Invoice i WHERE i.user = :user AND i.category IS NOT NULL")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT i.vendorName FROM Invoice i WHERE i.user = :user")
    List<String> findDistinctVendorNamesByUser(@Param("user") User user);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user AND i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i.vendorName, SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user AND i.invoiceDate BETWEEN :startDate AND :endDate GROUP BY i.vendorName ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> getVendorSpendingByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i.category, SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user AND i.category IS NOT NULL AND i.invoiceDate BETWEEN :startDate AND :endDate GROUP BY i.category ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> getCategorySpendingByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT YEAR(i.invoiceDate), MONTH(i.invoiceDate), SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user GROUP BY YEAR(i.invoiceDate), MONTH(i.invoiceDate) ORDER BY YEAR(i.invoiceDate), MONTH(i.invoiceDate)")
    List<Object[]> getMonthlySpendingByUser(@Param("user") User user);
    
    long countByUser(User user);
}
