package com.benchmark.insurance.policy;

public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(Long id) { super("Policy not found with id: " + id); }
    public PolicyNotFoundException(String msg) { super(msg); }
}
