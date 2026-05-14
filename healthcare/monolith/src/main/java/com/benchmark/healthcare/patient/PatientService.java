package com.benchmark.healthcare.patient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Patient> searchByName(String name) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    @Transactional
    public Patient updatePatient(Long id, Patient details) {
        Patient patient = getPatientById(id);
        patient.setFirstName(details.getFirstName());
        patient.setLastName(details.getLastName());
        patient.setEmail(details.getEmail());
        patient.setPhone(details.getPhone());
        patient.setAddress(details.getAddress());
        patient.setDateOfBirth(details.getDateOfBirth());
        patient.setGender(details.getGender());
        patient.setBloodType(details.getBloodType());
        return patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        patientRepository.delete(getPatientById(id));
    }

    // Called by AppointmentService, BillingService — validate patient exists
    @Transactional(readOnly = true)
    public void validatePatientExists(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException(patientId);
        }
    }
}
