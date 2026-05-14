package com.benchmark.insurance.customer;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) { super("Customer not found with id: " + id); }
    public CustomerNotFoundException(String msg) { super(msg); }
}
