package com.benchmark.supplychain.tracking;

public class TrackingNotFoundException extends RuntimeException {
    public TrackingNotFoundException(Long id) { super("Tracking not found with id: " + id); }
    public TrackingNotFoundException(String msg) { super(msg); }
}
