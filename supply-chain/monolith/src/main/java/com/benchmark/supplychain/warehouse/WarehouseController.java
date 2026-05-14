package com.benchmark.supplychain.warehouse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/tasks")
    public ResponseEntity<WarehouseTask> createTask(@RequestParam Long orderId,
                                                     @RequestParam(required = false) String binLocation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createPickTask(orderId, binLocation));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<WarehouseTask> getById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getById(id));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<WarehouseTask>> getAll() {
        return ResponseEntity.ok(warehouseService.getAll());
    }

    @GetMapping("/tasks/order/{orderId}")
    public ResponseEntity<WarehouseTask> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(warehouseService.getByOrderId(orderId));
    }

    @PatchMapping("/tasks/{id}/pick")
    public ResponseEntity<WarehouseTask> startPicking(@PathVariable Long id,
                                                       @RequestParam String operator) {
        return ResponseEntity.ok(warehouseService.startPicking(id, operator));
    }

    @PatchMapping("/tasks/{id}/pack")
    public ResponseEntity<WarehouseTask> completePacking(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.completePacking(id));
    }

    @PatchMapping("/tasks/{id}/dispatch")
    public ResponseEntity<WarehouseTask> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.dispatch(id));
    }
}
