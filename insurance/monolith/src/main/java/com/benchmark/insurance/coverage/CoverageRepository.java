package com.benchmark.insurance.coverage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoverageRepository extends JpaRepository<Coverage, Long> {
    List<Coverage> findByPolicyId(Long policyId);
    List<Coverage> findByPolicyIdAndActive(Long policyId, boolean active);
}
