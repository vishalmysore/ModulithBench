package com.benchmark.insurance.claim;

public class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(Long id) { super("Claim not found with id: " + id); }
    public ClaimNotFoundException(String msg) { super(msg); }
}
