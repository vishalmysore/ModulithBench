package com.benchmark.supplychain.customs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customs")
@RequiredArgsConstructor
public class CustomsController {

    private final CustomsService customsService;

    @PostMapping
    public ResponseEntity<Customs> create(@RequestParam Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customsService.createDeclaration(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customs> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customsService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Customs>> getAll() {
        return ResponseEntity.ok(customsService.getAll());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Customs> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(customsService.getByOrderId(orderId));
    }

    @GetMapping("/held")
    public ResponseEntity<List<Customs>> getHeld() {
        return ResponseEntity.ok(customsService.getHeldDeclarations());
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<Customs> submit(@PathVariable Long id) {
        return ResponseEntity.ok(customsService.submit(id));
    }

    @PatchMapping("/{id}/clear")
    public ResponseEntity<Customs> clear(@PathVariable Long id) {
        return ResponseEntity.ok(customsService.clear(id));
    }

    @PatchMapping("/{id}/hold")
    public ResponseEntity<Customs> hold(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(customsService.hold(id, reason));
    }
}
