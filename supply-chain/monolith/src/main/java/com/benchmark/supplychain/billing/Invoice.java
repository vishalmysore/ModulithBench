package com.benchmark.supplychain.billing;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    @Column(name = "goods_value", precision = 12, scale = 2)
    private BigDecimal goodsValue;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "duties_and_taxes", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal dutiesAndTaxes = BigDecimal.ZERO;

    @Column(name = "fuel_surcharge", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fuelSurcharge = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceNumber == null) invoiceNumber = "INV-" + System.nanoTime();
        recalculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalculateTotal();
    }

    private void recalculateTotal() {
        totalAmount = (goodsValue != null ? goodsValue : BigDecimal.ZERO)
                .add(shippingCost != null ? shippingCost : BigDecimal.ZERO)
                .add(dutiesAndTaxes != null ? dutiesAndTaxes : BigDecimal.ZERO)
                .add(fuelSurcharge != null ? fuelSurcharge : BigDecimal.ZERO);
    }
}
