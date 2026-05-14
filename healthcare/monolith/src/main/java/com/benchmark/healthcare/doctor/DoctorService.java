package com.benchmark.healthcare.doctor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findByAvailable(true);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor details) {
        Doctor doctor = getDoctorById(id);
        doctor.setFirstName(details.getFirstName());
        doctor.setLastName(details.getLastName());
        doctor.setEmail(details.getEmail());
        doctor.setPhone(details.getPhone());
        doctor.setSpecialization(details.getSpecialization());
        doctor.setDepartmentId(details.getDepartmentId());
        doctor.setAvailable(details.isAvailable());
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        doctorRepository.delete(getDoctorById(id));
    }

    // Called by AppointmentService — checks doctor availability before scheduling
    @Transactional(readOnly = true)
    public void validateDoctorAvailability(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        if (!doctor.isAvailable()) {
            throw new IllegalStateException("Doctor " + doctorId + " is not available for appointments");
        }
    }
}
