package com.benchmark.healthcare.prescription;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(Long id) { super("Prescription not found with id: " + id); }
}
