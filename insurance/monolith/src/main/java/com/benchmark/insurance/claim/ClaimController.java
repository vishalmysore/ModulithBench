package com.benchmark.insurance.claim;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<Claim> file(
            @RequestParam Long policyId,
            @RequestParam Long customerId,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam BigDecimal claimedAmount,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.fileClaim(policyId, customerId, type, description, claimedAmount, notes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAll() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Claim>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(claimService.getClaimsByCustomer(customerId));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<Claim>> getByPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(claimService.getClaimsByPolicy(policyId));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<Claim> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.startReview(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Claim> approve(@PathVariable Long id, @RequestParam BigDecimal approvedAmount) {
        return ResponseEntity.ok(claimService.approveClaim(id, approvedAmount));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Claim> reject(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(claimService.rejectClaim(id, reason));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claimService.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }
}
