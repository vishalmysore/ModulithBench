package com.benchmark.insurance.coverage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoverageService {
    private final CoverageRepository coverageRepository;

    @Transactional
    public Coverage createCoverage(Coverage coverage) {
        return coverageRepository.save(coverage);
    }

    @Transactional(readOnly = true)
    public Coverage getCoverageById(Long id) {
        return coverageRepository.findById(id)
                .orElseThrow(() -> new CoverageNotFoundException("Coverage not found with id: " + id));
    }

    @Transactional
    public Coverage updateCoverage(Long id, Coverage coverageDetails) {
        Coverage coverage = getCoverageById(id);
        return coverageRepository.save(coverage);
    }

    @Transactional
    public void deleteCoverage(Long id) {
        Coverage coverage = getCoverageById(id);
        coverageRepository.delete(coverage);
    }
}