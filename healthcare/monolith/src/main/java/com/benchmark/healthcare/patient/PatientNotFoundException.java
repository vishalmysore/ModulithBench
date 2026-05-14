package com.benchmark.healthcare.patient;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) { super("Patient not found with id: " + id); }
    public PatientNotFoundException(String msg) { super(msg); }
}
