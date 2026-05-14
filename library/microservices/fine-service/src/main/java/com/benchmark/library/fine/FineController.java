package com.benchmark.library.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;

    @PostMapping
    public ResponseEntity<Fine> create(@RequestBody Fine fine) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fineService.createFine(fine));
    }

    @GetMapping("/{fineId}")
    public ResponseEntity<Fine> getById(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.getFineById(fineId));
    }

    @PutMapping("/{fineId}")
    public ResponseEntity<Fine> update(@PathVariable Long fineId, @RequestBody Fine fine) {
        return ResponseEntity.ok(fineService.updateFine(fineId, fine));
    }

    @DeleteMapping("/{fineId}")
    public ResponseEntity<Void> delete(@PathVariable Long fineId) {
        fineService.deleteFine(fineId);
        return ResponseEntity.noContent().build();
    }
}