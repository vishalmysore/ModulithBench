package com.benchmark.supplychain.customs;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customs_declarations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "origin_country", nullable = false)
    private String originCountry;

    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(name = "declaration_number", unique = true)
    private String declarationNumber;

    @Column(name = "declared_value", precision = 12, scale = 2)
    private BigDecimal declaredValue;

    @Column(name = "duty_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal dutyAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    // JSON HS codes for goods classification
    @Column(name = "hs_codes", columnDefinition = "TEXT")
    private String hsCodes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CustomsStatus status = CustomsStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (declarationNumber == null) declarationNumber = "CUS-" + System.nanoTime();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public BigDecimal getTotalDutiesAndTaxes() {
        return dutyAmount.add(taxAmount);
    }
}
