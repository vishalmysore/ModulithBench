package com.benchmark.insurance.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    public ResponseEntity<Policy> create(@RequestBody Policy policy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(policy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> getAll() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Policy>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(policyService.getPoliciesByCustomer(customerId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Policy>> getActive() {
        return ResponseEntity.ok(policyService.getActivePolicies());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Policy> activate(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.activatePolicy(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Policy> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.cancelPolicy(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Policy> update(@PathVariable Long id, @RequestBody Policy policy) {
        return ResponseEntity.ok(policyService.updatePolicy(id, policy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
