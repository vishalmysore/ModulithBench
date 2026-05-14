package com.benchmark.healthcare.prescription;

import com.benchmark.healthcare.medicalrecord.MedicalRecord;
import com.benchmark.healthcare.medicalrecord.MedicalRecordService;
import com.benchmark.healthcare.patient.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Issuing a prescription reads the medical record directly
 * to copy patientId/doctorId — no HTTP call, no data duplication risk.
 */
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;

    @Transactional
    public Prescription createFromMedicalRecord(Long medicalRecordId, String medications, String instructions) {
        // Read medical record data directly — no HTTP call
        MedicalRecord record = medicalRecordService.getRecordById(medicalRecordId);

        Prescription prescription = Prescription.builder()
                .patientId(record.getPatientId())
                .doctorId(record.getDoctorId())
                .medicalRecordId(medicalRecordId)
                .medications(medications)
                .instructions(instructions)
                .build();

        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription createPrescription(Prescription prescription) {
        patientService.validatePatientExists(prescription.getPatientId());
        return prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Prescription> getActivePrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdAndActive(patientId, true);
    }

    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Transactional
    public Prescription updatePrescription(Long id, Prescription details) {
        Prescription prescription = getPrescriptionById(id);
        prescription.setMedications(details.getMedications());
        prescription.setInstructions(details.getInstructions());
        prescription.setExpiryDate(details.getExpiryDate());
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public void deletePrescription(Long id) {
        prescriptionRepository.delete(getPrescriptionById(id));
    }
}
