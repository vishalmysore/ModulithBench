package com.benchmark.supplychain.route;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/optimize")
    public ResponseEntity<Route> optimize(@RequestParam Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.optimizeRoute(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Route> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(routeService.getByOrderId(orderId));
    }
}
