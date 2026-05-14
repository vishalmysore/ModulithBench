package com.benchmark.healthcare.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    List<Billing> findByPatientId(Long patientId);
    List<Billing> findByStatus(BillingStatus status);
    List<Billing> findByPatientIdAndStatus(Long patientId, BillingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount - b.paidAmount), 0) FROM Billing b WHERE b.patientId = :patientId AND b.status != 'PAID' AND b.status != 'CANCELLED'")
    BigDecimal sumOutstandingByPatient(Long patientId);
}
