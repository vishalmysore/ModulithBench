package com.benchmark.insurance.coverage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coverages")
@RequiredArgsConstructor
public class CoverageController {

    private final CoverageService coverageService;

    @PostMapping
    public ResponseEntity<Coverage> create(@RequestBody Coverage coverage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coverageService.createCoverage(coverage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coverage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coverageService.getCoverageById(id));
    }

    @GetMapping
    public ResponseEntity<List<Coverage>> getAll() {
        return ResponseEntity.ok(coverageService.getAllCoverages());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<Coverage>> getByPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(coverageService.getCoveragesByPolicy(policyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coverage> update(@PathVariable Long id, @RequestBody Coverage coverage) {
        return ResponseEntity.ok(coverageService.updateCoverage(id, coverage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coverageService.deleteCoverage(id);
        return ResponseEntity.noContent().build();
    }
}
