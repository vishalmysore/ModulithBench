package com.benchmark.healthcare.medicalrecord;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medicalrecords")
@RequiredArgsConstructor
public class MedicalRecordController {
    private final MedicalRecordService medicalrecordService;

    @PostMapping
    public ResponseEntity<MedicalRecord> create(@RequestBody MedicalRecord medicalrecord) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalrecordService.createMedicalRecord(medicalrecord));
    }

    @GetMapping("/{medicalrecordId}")
    public ResponseEntity<MedicalRecord> getById(@PathVariable Long medicalrecordId) {
        return ResponseEntity.ok(medicalrecordService.getMedicalRecordById(medicalrecordId));
    }

    @PutMapping("/{medicalrecordId}")
    public ResponseEntity<MedicalRecord> update(@PathVariable Long medicalrecordId, @RequestBody MedicalRecord medicalrecord) {
        return ResponseEntity.ok(medicalrecordService.updateMedicalRecord(medicalrecordId, medicalrecord));
    }

    @DeleteMapping("/{medicalrecordId}")
    public ResponseEntity<Void> delete(@PathVariable Long medicalrecordId) {
        medicalrecordService.deleteMedicalRecord(medicalrecordId);
        return ResponseEntity.noContent().build();
    }
}