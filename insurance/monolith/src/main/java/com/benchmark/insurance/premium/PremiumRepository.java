package com.benchmark.insurance.premium;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PremiumRepository extends JpaRepository<Premium, Long> {
    List<Premium> findByPolicyId(Long policyId);
    List<Premium> findByStatus(PremiumStatus status);
    List<Premium> findByPolicyIdAndStatus(Long policyId, PremiumStatus status);

    @Query("SELECT p FROM Premium p WHERE p.status = 'PENDING' AND p.dueDate < :today")
    List<Premium> findOverduePremiums(LocalDate today);
}
