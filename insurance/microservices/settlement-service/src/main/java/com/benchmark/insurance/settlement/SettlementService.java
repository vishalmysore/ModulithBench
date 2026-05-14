package com.benchmark.insurance.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;

    @Transactional
    public Settlement createSettlement(Settlement settlement) {
        return settlementRepository.save(settlement);
    }

    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found with id: " + id));
    }

    @Transactional
    public Settlement updateSettlement(Long id, Settlement settlementDetails) {
        Settlement settlement = getSettlementById(id);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public void deleteSettlement(Long id) {
        Settlement settlement = getSettlementById(id);
        settlementRepository.delete(settlement);
    }
}