package com.benchmark.insurance.coverage;

public class CoverageNotFoundException extends RuntimeException {
    public CoverageNotFoundException(Long id) { super("Coverage not found with id: " + id); }
}
