package com.benchmark.supplychain.tracking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/initialize")
    public ResponseEntity<Tracking> initialize(@RequestParam Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackingService.initializeTracking(orderId));
    }

    @PostMapping("/update")
    public ResponseEntity<Tracking> update(@RequestParam Long orderId,
                                            @RequestParam String location,
                                            @RequestParam String status,
                                            @RequestParam(required = false) BigDecimal latitude,
                                            @RequestParam(required = false) BigDecimal longitude) {
        return ResponseEntity.ok(trackingService.updateLocation(orderId, location, status, latitude, longitude));
    }

    @GetMapping("/order/{orderId}/latest")
    public ResponseEntity<Tracking> getLatest(@PathVariable Long orderId) {
        return ResponseEntity.ok(trackingService.getLatestByOrder(orderId));
    }

    @GetMapping("/order/{orderId}/history")
    public ResponseEntity<List<Tracking>> getHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(trackingService.getHistoryByOrder(orderId));
    }
}
