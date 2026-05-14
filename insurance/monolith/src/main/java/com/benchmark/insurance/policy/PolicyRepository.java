package com.benchmark.insurance.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByCustomerId(Long customerId);
    List<Policy> findByAgentId(Long agentId);
    List<Policy> findByStatus(PolicyStatus status);
    List<Policy> findByCustomerIdAndStatus(Long customerId, PolicyStatus status);
}
