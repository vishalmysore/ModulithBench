package com.benchmark.supplychain.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotBlank
    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    @NotBlank
    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(name = "origin_warehouse", nullable = false)
    private String originWarehouse;

    // JSON array of {sku, quantity, unitPrice}
    @Column(name = "items", columnDefinition = "TEXT", nullable = false)
    private String items;

    @Column(name = "total_weight", precision = 10, scale = 3)
    private BigDecimal totalWeight;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderPriority priority = OrderPriority.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "is_international")
    private boolean international;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderNumber == null) orderNumber = "ORD-" + System.nanoTime();
        if (destinationCountry != null) {
            international = !destinationCountry.equalsIgnoreCase("US") &&
                            !destinationCountry.equalsIgnoreCase("USA");
        }
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
