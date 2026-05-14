package com.benchmark.insurance.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PremiumService {
    private final PremiumRepository premiumRepository;

    @Transactional
    public Premium createPremium(Premium premium) {
        return premiumRepository.save(premium);
    }

    @Transactional(readOnly = true)
    public Premium getPremiumById(Long id) {
        return premiumRepository.findById(id)
                .orElseThrow(() -> new PremiumNotFoundException("Premium not found with id: " + id));
    }

    @Transactional
    public Premium updatePremium(Long id, Premium premiumDetails) {
        Premium premium = getPremiumById(id);
        return premiumRepository.save(premium);
    }

    @Transactional
    public void deletePremium(Long id) {
        Premium premium = getPremiumById(id);
        premiumRepository.delete(premium);
    }
}