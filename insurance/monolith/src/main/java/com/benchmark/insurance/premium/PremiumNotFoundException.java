package com.benchmark.insurance.premium;

public class PremiumNotFoundException extends RuntimeException {
    public PremiumNotFoundException(Long id) { super("Premium not found with id: " + id); }
}
