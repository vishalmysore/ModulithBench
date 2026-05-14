package com.benchmark.supplychain.warehouse;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(name = "bin_location")
    private String binLocation;

    @Column(name = "assigned_operator")
    private String assignedOperator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WarehouseTaskStatus status = WarehouseTaskStatus.PENDING;

    @Column(name = "pick_started_at")
    private LocalDateTime pickStartedAt;

    @Column(name = "pack_completed_at")
    private LocalDateTime packCompletedAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
