package com.benchmark.insurance.claim;

import com.benchmark.insurance.customer.CustomerService;
import com.benchmark.insurance.policy.Policy;
import com.benchmark.insurance.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * MONOLITH ADVANTAGE: Filing a claim validates policy ownership, checks policy status,
 * and verifies customer — all in a single atomic transaction.
 * In microservices, this requires 3 HTTP calls with distributed rollback complexity.
 */
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    // Direct cross-module injection — zero network overhead
    private final PolicyService policyService;
    private final CustomerService customerService;

    @Transactional
    public Claim fileClaim(Long policyId, Long customerId, String type,
                            String description, BigDecimal claimedAmount, String notes) {
        // Validate policy is active — direct call, no HTTP roundtrip
        policyService.validateActivePolicy(policyId);

        // Validate customer is active — direct call
        customerService.validateActiveCustomer(customerId);

        // Verify policy belongs to the customer — cross-module data access in same transaction
        Policy policy = policyService.getPolicyById(policyId);
        if (!policy.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("Policy " + policyId + " does not belong to customer " + customerId);
        }

        Claim claim = Claim.builder()
                .policyId(policyId)
                .customerId(customerId)
                .claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .type(type)
                .description(description)
                .claimedAmount(claimedAmount)
                .notes(notes)
                .build();

        return claimRepository.save(claim);
    }

    @Transactional(readOnly = true)
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsByCustomer(Long customerId) {
        return claimRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsByPolicy(Long policyId) {
        return claimRepository.findByPolicyId(policyId);
    }

    @Transactional
    public Claim approveClaim(Long id, BigDecimal approvedAmount) {
        Claim claim = getClaimById(id);
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW && claim.getStatus() != ClaimStatus.FILED) {
            throw new IllegalStateException("Claim " + id + " cannot be approved from status: " + claim.getStatus());
        }
        claim.setApprovedAmount(approvedAmount);
        claim.setStatus(ClaimStatus.APPROVED);
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim rejectClaim(Long id, String reason) {
        Claim claim = getClaimById(id);
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setNotes(reason);
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim startReview(Long id) {
        Claim claim = getClaimById(id);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        return claimRepository.save(claim);
    }

    @Transactional
    public void deleteClaim(Long id) {
        claimRepository.delete(getClaimById(id));
    }
}
