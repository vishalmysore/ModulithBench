package com.benchmark.healthcare.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillingRepository billingRepository;

    @Transactional
    public Billing createBilling(Billing billing) {
        return billingRepository.save(billing);
    }

    @Transactional(readOnly = true)
    public Billing getBillingById(Long id) {
        return billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException("Billing not found with id: " + id));
    }

    @Transactional
    public Billing updateBilling(Long id, Billing billingDetails) {
        Billing billing = getBillingById(id);
        return billingRepository.save(billing);
    }

    @Transactional
    public void deleteBilling(Long id) {
        Billing billing = getBillingById(id);
        billingRepository.delete(billing);
    }
}