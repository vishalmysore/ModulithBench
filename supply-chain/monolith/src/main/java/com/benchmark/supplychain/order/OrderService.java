package com.benchmark.supplychain.order;

import com.benchmark.supplychain.carrier.CarrierService;
import com.benchmark.supplychain.inventory.InventoryService;
import com.benchmark.supplychain.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * THE GHOST SHIPMENT SCENARIO
 *
 * cancelOrder() must atomically:
 *   1. Update order status to CANCELLED
 *   2. Release reserved inventory (InventoryService)
 *   3. Cancel the warehouse pick task (WarehouseService)
 *   4. Cancel the carrier booking (CarrierService)
 *
 * MONOLITH: All 4 steps run in ONE @Transactional.
 * If anything fails, ALL steps roll back. Inventory is never wrong.
 * The "Ghost Shipment" is structurally impossible.
 *
 * MICROSERVICES: Each step is an HTTP call to a separate service.
 * If carrier-service is down when steps 1-3 have succeeded,
 * you now have: cancelled order, released inventory, cancelled pick task,
 * but an ACTIVE carrier booking. The ghost shipment exists.
 * An AI agent implementing this in microservices must reason about:
 *   - Saga choreography
 *   - Compensating transactions
 *   - Idempotency keys
 *   - Dead letter queues for failed compensations
 *   - Manual review flags
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // Direct cross-module injection — all in same Spring context
    private final InventoryService inventoryService;

    // @Lazy prevents circular dependency with WarehouseService
    @Lazy
    private final WarehouseService warehouseService;
    private final CarrierService carrierService;

    @Transactional
    public Order createOrder(Order order) {
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmOrder(Long orderId, String sku, int quantity) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order " + orderId + " is not in PENDING status");
        }

        // Reserve inventory — direct call, same transaction
        inventoryService.reserveStock(sku, order.getOriginWarehouse(), quantity);

        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * THE GHOST SHIPMENT FIX — ATOMIC CANCELLATION
     *
     * Every compensation step runs in THIS transaction.
     * Spring's @Transactional guarantees: either all 4 succeed, or none do.
     *
     * An AI agent adding a 5th compensation (e.g., notify customer service)
     * just adds one more line here — fully in scope, same transaction.
     * In microservices, adding a 5th service to a saga requires:
     *   - Adding a new event type
     *   - Writing a new consumer
     *   - Writing a compensating consumer
     *   - Testing 2^5 partial failure combinations
     */
    @Transactional
    public Order cancelOrder(Long orderId, String sku, int quantity) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order " + orderId + " is already cancelled");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order " + orderId);
        }

        // STEP 1: Release reserved inventory — direct call, this transaction
        if (order.getStatus().ordinal() >= OrderStatus.CONFIRMED.ordinal()) {
            inventoryService.releaseReservedStock(sku, order.getOriginWarehouse(), quantity);
        }

        // STEP 2: Cancel warehouse pick task — direct call, this transaction
        // Throws if task is DISPATCHED — prevents ghost shipment at warehouse layer
        warehouseService.cancelPickTask(orderId);

        // STEP 3: Cancel carrier booking — direct call, this transaction
        // Throws if carrier has already PICKED_UP the package
        carrierService.cancelBooking(orderId);

        // STEP 4: Mark order cancelled — only reaches here if steps 1-3 succeeded
        // If any step above threw, this line never executes AND steps 1-3 roll back
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.delete(getOrderById(id));
    }
}
