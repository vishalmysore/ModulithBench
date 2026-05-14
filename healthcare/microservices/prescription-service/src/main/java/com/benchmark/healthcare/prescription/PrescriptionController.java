package com.benchmark.healthcare.prescription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<Prescription> create(@RequestBody Prescription prescription) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.createPrescription(prescription));
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<Prescription> getById(@PathVariable Long prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(prescriptionId));
    }

    @PutMapping("/{prescriptionId}")
    public ResponseEntity<Prescription> update(@PathVariable Long prescriptionId, @RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(prescriptionId, prescription));
    }

    @DeleteMapping("/{prescriptionId}")
    public ResponseEntity<Void> delete(@PathVariable Long prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId);
        return ResponseEntity.noContent().build();
    }
}