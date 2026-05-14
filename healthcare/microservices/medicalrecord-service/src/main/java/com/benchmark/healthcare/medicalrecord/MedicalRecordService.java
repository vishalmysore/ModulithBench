package com.benchmark.healthcare.medicalrecord;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalrecordRepository;

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord medicalrecord) {
        return medicalrecordRepository.save(medicalrecord);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalrecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException("MedicalRecord not found with id: " + id));
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord medicalrecordDetails) {
        MedicalRecord medicalrecord = getMedicalRecordById(id);
        return medicalrecordRepository.save(medicalrecord);
    }

    @Transactional
    public void deleteMedicalRecord(Long id) {
        MedicalRecord medicalrecord = getMedicalRecordById(id);
        medicalrecordRepository.delete(medicalrecord);
    }
}