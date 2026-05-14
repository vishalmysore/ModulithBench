package com.benchmark.insurance.policy;

import com.benchmark.insurance.agent.AgentService;
import com.benchmark.insurance.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * MONOLITH ADVANTAGE: Policy creation validates customer and agent atomically.
 * No saga pattern needed — if either validation fails, the whole transaction rolls back.
 */
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    // Direct cross-module injection
    private final CustomerService customerService;
    private final AgentService agentService;

    @Transactional
    public Policy createPolicy(Policy policy) {
        // Validate customer is active — direct call, same transaction
        customerService.validateActiveCustomer(policy.getCustomerId());

        // Validate agent if provided — direct call, same transaction
        if (policy.getAgentId() != null) {
            agentService.validateActiveAgent(policy.getAgentId());
        }

        // Auto-generate policy number if not provided
        if (policy.getPolicyNumber() == null || policy.getPolicyNumber().isBlank()) {
            policy.setPolicyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        return policyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Policy getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with number: " + policyNumber));
    }

    @Transactional(readOnly = true)
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Policy> getPoliciesByCustomer(Long customerId) {
        return policyRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Policy> getActivePolicies() {
        return policyRepository.findByStatus(PolicyStatus.ACTIVE);
    }

    @Transactional
    public Policy activatePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policy.setStatus(PolicyStatus.ACTIVE);
        return policyRepository.save(policy);
    }

    @Transactional
    public Policy cancelPolicy(Long id) {
        Policy policy = getPolicyById(id);
        policy.setStatus(PolicyStatus.CANCELLED);
        return policyRepository.save(policy);
    }

    @Transactional
    public Policy updatePolicy(Long id, Policy details) {
        Policy policy = getPolicyById(id);
        policy.setType(details.getType());
        policy.setStartDate(details.getStartDate());
        policy.setEndDate(details.getEndDate());
        policy.setPremiumAmount(details.getPremiumAmount());
        policy.setCoverageAmount(details.getCoverageAmount());
        policy.setDescription(details.getDescription());
        return policyRepository.save(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        policyRepository.delete(getPolicyById(id));
    }

    // Called by ClaimService and PremiumService — validates policy is active
    @Transactional(readOnly = true)
    public void validateActivePolicy(Long policyId) {
        Policy policy = getPolicyById(policyId);
        if (!policy.isActive()) {
            throw new IllegalStateException("Policy " + policyId + " is not active (status: " + policy.getStatus() + ")");
        }
    }
}
