package com.benchmark.supplychain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Order> confirm(@PathVariable Long id,
                                          @RequestParam String sku,
                                          @RequestParam int quantity) {
        return ResponseEntity.ok(orderService.confirmOrder(id, sku, quantity));
    }

    /**
     * THE GHOST SHIPMENT ENDPOINT
     * Cancels order + releases inventory + cancels warehouse task + cancels carrier — all atomic.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable Long id,
                                         @RequestParam(required = false, defaultValue = "") String sku,
                                         @RequestParam(required = false, defaultValue = "0") int quantity) {
        return ResponseEntity.ok(orderService.cancelOrder(id, sku, quantity));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
