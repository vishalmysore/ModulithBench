package com.benchmark.healthcare.medicalrecord;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(Long id) { super("Medical record not found with id: " + id); }
}
