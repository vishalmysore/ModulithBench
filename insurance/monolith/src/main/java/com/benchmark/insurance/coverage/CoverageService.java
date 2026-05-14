package com.benchmark.insurance.coverage;

import com.benchmark.insurance.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoverageService {

    private final CoverageRepository coverageRepository;
    private final PolicyService policyService;

    @Transactional
    public Coverage createCoverage(Coverage coverage) {
        // Validate policy exists before adding coverage — direct call
        policyService.getPolicyById(coverage.getPolicyId());
        return coverageRepository.save(coverage);
    }

    @Transactional(readOnly = true)
    public Coverage getCoverageById(Long id) {
        return coverageRepository.findById(id)
                .orElseThrow(() -> new CoverageNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Coverage> getAllCoverages() {
        return coverageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Coverage> getCoveragesByPolicy(Long policyId) {
        return coverageRepository.findByPolicyId(policyId);
    }

    @Transactional(readOnly = true)
    public List<Coverage> getActiveCoveragesByPolicy(Long policyId) {
        return coverageRepository.findByPolicyIdAndActive(policyId, true);
    }

    @Transactional
    public Coverage updateCoverage(Long id, Coverage details) {
        Coverage coverage = getCoverageById(id);
        coverage.setType(details.getType());
        coverage.setDescription(details.getDescription());
        coverage.setMaximumAmount(details.getMaximumAmount());
        coverage.setDeductibleAmount(details.getDeductibleAmount());
        coverage.setActive(details.isActive());
        return coverageRepository.save(coverage);
    }

    @Transactional
    public void deleteCoverage(Long id) {
        coverageRepository.delete(getCoverageById(id));
    }
}
