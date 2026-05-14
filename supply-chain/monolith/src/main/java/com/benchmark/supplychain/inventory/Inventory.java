package com.benchmark.supplychain.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"sku", "warehouse_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String sku;

    @NotBlank
    @Column(name = "product_name", nullable = false)
    private String productName;

    @NotBlank
    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Min(0)
    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private int quantityOnHand = 0;

    @Min(0)
    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private int quantityReserved = 0;

    @Column(name = "reorder_point")
    @Builder.Default
    private int reorderPoint = 10;

    @Column(name = "unit_weight", precision = 8, scale = 3)
    private BigDecimal unitWeight;

    @Column(name = "unit_value", precision = 10, scale = 2)
    private BigDecimal unitValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public int getQuantityAvailable() {
        return quantityOnHand - quantityReserved;
    }

    public boolean isBelowReorderPoint() {
        return quantityOnHand <= reorderPoint;
    }
}
