package com.invoice_scanner_backend.repository;

import com.invoice_scanner_backend.entity.InvoiceLineItem;
import com.invoice_scanner_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {
    
    List<InvoiceLineItem> findByInvoiceOrderByLineNumber(Invoice invoice);
    
    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);
    
    @Query("SELECT SUM(li.totalPrice) FROM InvoiceLineItem li WHERE li.invoice.id = :invoiceId")
    java.math.BigDecimal sumTotalPriceByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    void deleteByInvoiceId(Long invoiceId);
    
    long countByInvoice(Invoice invoice);
}
