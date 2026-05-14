package com.benchmark.healthcare.appointment;

import com.benchmark.healthcare.doctor.DoctorService;
import com.benchmark.healthcare.patient.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Scheduling validates patient and doctor availability atomically.
 * In microservices, this requires 2 HTTP calls with potential partial failure.
 * Here, both validations run in the same transaction — either all succeed or none do.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    // Direct cross-module injection — no HTTP, no service registry
    private final PatientService patientService;
    private final DoctorService doctorService;

    @Transactional
    public Appointment scheduleAppointment(Long patientId, Long doctorId, Long departmentId,
                                            LocalDateTime scheduledAt, String notes) {
        // Validate patient exists — direct method call
        patientService.validatePatientExists(patientId);

        // Validate doctor is available — direct method call, same transaction
        doctorService.validateDoctorAvailability(doctorId);

        // Check no scheduling conflict for the doctor
        List<Appointment> conflicts = appointmentRepository.findByDoctorIdAndScheduledAtBetween(
                doctorId,
                scheduledAt.minusMinutes(29),
                scheduledAt.plusMinutes(29)
        );
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                "Doctor " + doctorId + " already has an appointment at " + scheduledAt);
        }

        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .departmentId(departmentId)
                .scheduledAt(scheduledAt)
                .notes(notes)
                .build();

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public Appointment completeAppointment(Long id) {
        Appointment appt = getAppointmentById(id);
        appt.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appt);
    }

    @Transactional
    public Appointment cancelAppointment(Long id) {
        Appointment appt = getAppointmentById(id);
        appt.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appt);
    }

    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.delete(getAppointmentById(id));
    }
}
