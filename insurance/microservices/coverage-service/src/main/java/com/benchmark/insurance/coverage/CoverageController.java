package com.benchmark.insurance.coverage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coverages")
@RequiredArgsConstructor
public class CoverageController {
    private final CoverageService coverageService;

    @PostMapping
    public ResponseEntity<Coverage> create(@RequestBody Coverage coverage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coverageService.createCoverage(coverage));
    }

    @GetMapping("/{coverageId}")
    public ResponseEntity<Coverage> getById(@PathVariable Long coverageId) {
        return ResponseEntity.ok(coverageService.getCoverageById(coverageId));
    }

    @PutMapping("/{coverageId}")
    public ResponseEntity<Coverage> update(@PathVariable Long coverageId, @RequestBody Coverage coverage) {
        return ResponseEntity.ok(coverageService.updateCoverage(coverageId, coverage));
    }

    @DeleteMapping("/{coverageId}")
    public ResponseEntity<Void> delete(@PathVariable Long coverageId) {
        coverageService.deleteCoverage(coverageId);
        return ResponseEntity.noContent().build();
    }
}