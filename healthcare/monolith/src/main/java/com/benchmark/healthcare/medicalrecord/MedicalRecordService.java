package com.benchmark.healthcare.medicalrecord;

import com.benchmark.healthcare.appointment.Appointment;
import com.benchmark.healthcare.appointment.AppointmentService;
import com.benchmark.healthcare.patient.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Creating a medical record after an appointment reads
 * appointment data directly — no HTTP roundtrip to appointment-service.
 * Patient and appointment linkage is verified atomically.
 */
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @Transactional
    public MedicalRecord createRecord(Long appointmentId, String diagnosis, String treatment, String notes) {
        // Load appointment directly — verifies it exists and gets patient/doctor IDs
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        // Validate patient still exists — cross-module validation in same transaction
        patientService.validatePatientExists(appointment.getPatientId());

        MedicalRecord record = MedicalRecord.builder()
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .appointmentId(appointmentId)
                .diagnosis(diagnosis)
                .treatment(treatment)
                .notes(notes)
                .build();

        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord createRecordManual(MedicalRecord record) {
        patientService.validatePatientExists(record.getPatientId());
        return medicalRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getAllRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }

    @Transactional
    public MedicalRecord updateRecord(Long id, MedicalRecord details) {
        MedicalRecord record = getRecordById(id);
        record.setDiagnosis(details.getDiagnosis());
        record.setTreatment(details.getTreatment());
        record.setNotes(details.getNotes());
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long id) {
        medicalRecordRepository.delete(getRecordById(id));
    }
}
