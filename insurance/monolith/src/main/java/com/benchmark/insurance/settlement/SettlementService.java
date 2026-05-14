package com.benchmark.insurance.settlement;

import com.benchmark.insurance.claim.Claim;
import com.benchmark.insurance.claim.ClaimService;
import com.benchmark.insurance.claim.ClaimStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * MONOLITH ADVANTAGE: Settlement reads approved amount directly from ClaimService
 * and marks the claim as settled — all in one transaction.
 * No saga choreography, no compensating transactions needed.
 */
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    // Direct cross-module injection — reads claim state without HTTP
    private final ClaimService claimService;

    @Transactional
    public Settlement settleApprovedClaim(Long claimId, String paymentMethod, String notes) {
        // Read claim directly — gets approved amount without HTTP call
        Claim claim = claimService.getClaimById(claimId);

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new IllegalStateException(
                "Claim " + claimId + " must be APPROVED before settlement (current: " + claim.getStatus() + ")");
        }

        if (claim.getApprovedAmount() == null) {
            throw new IllegalStateException("Claim " + claimId + " has no approved amount");
        }

        Settlement settlement = Settlement.builder()
                .claimId(claimId)
                .amount(claim.getApprovedAmount())
                .paymentMethod(paymentMethod)
                .paymentReference("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .notes(notes)
                .status(SettlementStatus.PROCESSING)
                .build();

        Settlement saved = settlementRepository.save(settlement);

        // Mark claim as settled — cross-module state update in same transaction
        // This ensures settlement and claim status are always consistent
        claimService.approveClaim(claimId, claim.getApprovedAmount());

        return saved;
    }

    @Transactional
    public Settlement createSettlement(Settlement settlement) {
        return settlementRepository.save(settlement);
    }

    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new SettlementNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Settlement> getSettlementsByClaim(Long claimId) {
        return settlementRepository.findByClaimId(claimId);
    }

    @Transactional
    public Settlement markPaid(Long id) {
        Settlement settlement = getSettlementById(id);
        settlement.setStatus(SettlementStatus.PAID);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public Settlement updateSettlement(Long id, BigDecimal amount, String notes) {
        Settlement settlement = getSettlementById(id);
        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new IllegalStateException("Cannot update a paid settlement");
        }
        settlement.setAmount(amount);
        settlement.setNotes(notes);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public void deleteSettlement(Long id) {
        settlementRepository.delete(getSettlementById(id));
    }
}
