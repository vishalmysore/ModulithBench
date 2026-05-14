package com.benchmark.supplychain.route;

import com.benchmark.supplychain.order.Order;
import com.benchmark.supplychain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final OrderService orderService;

    // Called by OrderService.confirmOrder() — reads order data directly, no HTTP
    @Transactional
    public Route optimizeRoute(Long orderId) {
        Order order = orderService.getOrderById(orderId);

        // Simplified route calculation — real implementation would call a routing engine
        BigDecimal distanceKm = estimateDistance(order.getOriginWarehouse(), order.getDestinationAddress());
        BigDecimal durationHours = distanceKm.divide(BigDecimal.valueOf(80), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal fuelCost = distanceKm.multiply(new BigDecimal("0.15"));

        Route route = Route.builder()
                .orderId(orderId)
                .originWarehouse(order.getOriginWarehouse())
                .destinationAddress(order.getDestinationAddress())
                .totalDistanceKm(distanceKm)
                .estimatedDurationHours(durationHours)
                .fuelCostEstimate(fuelCost)
                .routeDetails("{\"waypoints\": []}")
                .optimizedAt(LocalDateTime.now())
                .build();

        return routeRepository.save(route);
    }

    @Transactional(readOnly = true)
    public Route getById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Route getByOrderId(Long orderId) {
        return routeRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RouteNotFoundException("No route for order: " + orderId));
    }

    private BigDecimal estimateDistance(String warehouse, String destination) {
        // Placeholder — real impl uses geocoding + routing API
        return new BigDecimal("450.00");
    }
}
