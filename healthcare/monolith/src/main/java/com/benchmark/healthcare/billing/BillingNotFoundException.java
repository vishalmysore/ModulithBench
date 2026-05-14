package com.benchmark.healthcare.billing;

public class BillingNotFoundException extends RuntimeException {
    public BillingNotFoundException(Long id) { super("Billing not found with id: " + id); }
}
