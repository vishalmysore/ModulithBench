package com.benchmark.supplychain.route;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "origin_warehouse", nullable = false)
    private String originWarehouse;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm;

    @Column(name = "estimated_duration_hours", precision = 6, scale = 2)
    private BigDecimal estimatedDurationHours;

    @Column(name = "fuel_cost_estimate", precision = 10, scale = 2)
    private BigDecimal fuelCostEstimate;

    // JSON waypoints array
    @Column(name = "route_details", columnDefinition = "TEXT")
    private String routeDetails;

    @Column(name = "optimized_at")
    private LocalDateTime optimizedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
