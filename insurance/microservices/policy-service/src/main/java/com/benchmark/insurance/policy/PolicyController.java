package com.benchmark.insurance.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policys")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @PostMapping
    public ResponseEntity<Policy> create(@RequestBody Policy policy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(policy));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<Policy> getById(@PathVariable Long policyId) {
        return ResponseEntity.ok(policyService.getPolicyById(policyId));
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<Policy> update(@PathVariable Long policyId, @RequestBody Policy policy) {
        return ResponseEntity.ok(policyService.updatePolicy(policyId, policy));
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> delete(@PathVariable Long policyId) {
        policyService.deletePolicy(policyId);
        return ResponseEntity.noContent().build();
    }
}