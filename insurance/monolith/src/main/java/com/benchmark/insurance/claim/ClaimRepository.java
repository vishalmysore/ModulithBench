package com.benchmark.insurance.claim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByCustomerId(Long customerId);
    List<Claim> findByStatus(ClaimStatus status);
    java.util.Optional<Claim> findByClaimNumber(String claimNumber);
}
