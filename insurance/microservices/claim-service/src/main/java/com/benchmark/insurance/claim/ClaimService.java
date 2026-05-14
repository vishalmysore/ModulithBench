package com.benchmark.insurance.claim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClaimService {
    private final ClaimRepository claimRepository;

    @Transactional
    public Claim createClaim(Claim claim) {
        return claimRepository.save(claim);
    }

    @Transactional(readOnly = true)
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with id: " + id));
    }

    @Transactional
    public Claim updateClaim(Long id, Claim claimDetails) {
        Claim claim = getClaimById(id);
        return claimRepository.save(claim);
    }

    @Transactional
    public void deleteClaim(Long id) {
        Claim claim = getClaimById(id);
        claimRepository.delete(claim);
    }
}