package com.benchmark.supplychain.carrier;

public class CarrierNotFoundException extends RuntimeException {
    public CarrierNotFoundException(Long id) { super("Carrier booking not found with id: " + id); }
    public CarrierNotFoundException(String msg) { super(msg); }
}
