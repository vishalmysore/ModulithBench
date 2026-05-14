package com.benchmark.insurance.claim;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {
    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<Claim> create(@RequestBody Claim claim) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.createClaim(claim));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<Claim> getById(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimById(claimId));
    }

    @PutMapping("/{claimId}")
    public ResponseEntity<Claim> update(@PathVariable Long claimId, @RequestBody Claim claim) {
        return ResponseEntity.ok(claimService.updateClaim(claimId, claim));
    }

    @DeleteMapping("/{claimId}")
    public ResponseEntity<Void> delete(@PathVariable Long claimId) {
        claimService.deleteClaim(claimId);
        return ResponseEntity.noContent().build();
    }
}