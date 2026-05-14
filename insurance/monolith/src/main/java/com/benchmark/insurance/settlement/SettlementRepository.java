package com.benchmark.insurance.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByClaimId(Long claimId);
    List<Settlement> findByStatus(SettlementStatus status);
    Optional<Settlement> findByPaymentReference(String reference);
}
