package com.benchmark.healthcare.prescription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByDoctorId(Long doctorId);
    List<Prescription> findByMedicalRecordId(Long medicalRecordId);
    List<Prescription> findByPatientIdAndActive(Long patientId, boolean active);
}
