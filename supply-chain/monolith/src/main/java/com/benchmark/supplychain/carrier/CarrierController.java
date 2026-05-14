package com.benchmark.supplychain.carrier;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService carrierService;

    @PostMapping("/book")
    public ResponseEntity<Carrier> book(@RequestParam Long orderId,
                                         @RequestParam String carrierName,
                                         @RequestParam(defaultValue = "GROUND") String serviceType) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierService.bookCarrier(orderId, carrierName, serviceType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrier> getById(@PathVariable Long id) {
        return ResponseEntity.ok(carrierService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Carrier>> getAll() {
        return ResponseEntity.ok(carrierService.getAll());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Carrier> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(carrierService.getByOrderId(orderId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Carrier> updateStatus(@PathVariable Long id, @RequestParam CarrierStatus status) {
        return ResponseEntity.ok(carrierService.updateStatus(id, status));
    }
}
