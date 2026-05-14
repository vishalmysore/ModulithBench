package com.benchmark.insurance.premium;

import com.benchmark.insurance.policy.Policy;
import com.benchmark.insurance.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private final PremiumRepository premiumRepository;
    private final PolicyService policyService;

    @Transactional
    public Premium schedulePremium(Long policyId, PremiumFrequency frequency, LocalDate dueDate) {
        // Read policy data to get premium amount — direct call, no HTTP
        Policy policy = policyService.getPolicyById(policyId);
        policyService.validateActivePolicy(policyId);

        Premium premium = Premium.builder()
                .policyId(policyId)
                .amount(policy.getPremiumAmount())
                .frequency(frequency)
                .dueDate(dueDate)
                .build();

        return premiumRepository.save(premium);
    }

    @Transactional
    public Premium createPremium(Premium premium) {
        policyService.validateActivePolicy(premium.getPolicyId());
        return premiumRepository.save(premium);
    }

    @Transactional(readOnly = true)
    public Premium getPremiumById(Long id) {
        return premiumRepository.findById(id)
                .orElseThrow(() -> new PremiumNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Premium> getPremiumsByPolicy(Long policyId) {
        return premiumRepository.findByPolicyId(policyId);
    }

    @Transactional(readOnly = true)
    public List<Premium> getAllPremiums() {
        return premiumRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Premium> getOverduePremiums() {
        return premiumRepository.findOverduePremiums(LocalDate.now());
    }

    @Transactional
    public Premium recordPayment(Long id) {
        Premium premium = getPremiumById(id);
        premium.setPaidDate(LocalDate.now());
        premium.setStatus(PremiumStatus.PAID);
        return premiumRepository.save(premium);
    }

    @Transactional
    public List<Premium> markOverdue() {
        List<Premium> overdue = premiumRepository.findOverduePremiums(LocalDate.now());
        overdue.forEach(p -> p.setStatus(PremiumStatus.OVERDUE));
        return premiumRepository.saveAll(overdue);
    }

    @Transactional
    public void deletePremium(Long id) {
        premiumRepository.delete(getPremiumById(id));
    }
}
