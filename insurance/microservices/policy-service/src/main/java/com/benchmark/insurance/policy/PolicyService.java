package com.benchmark.insurance.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;

    @Transactional
    public Policy createPolicy(Policy policy) {
        return policyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + id));
    }

    @Transactional
    public Policy updatePolicy(Long id, Policy policyDetails) {
        Policy policy = getPolicyById(id);
        return policyRepository.save(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policyRepository.delete(policy);
    }
}