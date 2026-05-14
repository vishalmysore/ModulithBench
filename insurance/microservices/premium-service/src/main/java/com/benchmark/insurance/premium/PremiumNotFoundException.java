package com.benchmark.premium;

public class PremiumNotFoundException extends RuntimeException {
    public PremiumNotFoundException(String message) {
        super(message);
    }
}