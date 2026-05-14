package com.benchmark.healthcare;

import com.benchmark.healthcare.appointment.Appointment;
import com.benchmark.healthcare.appointment.AppointmentService;
import com.benchmark.healthcare.billing.BillingService;
import com.benchmark.healthcare.doctor.Doctor;
import com.benchmark.healthcare.doctor.DoctorService;
import com.benchmark.healthcare.medicalrecord.MedicalRecord;
import com.benchmark.healthcare.medicalrecord.MedicalRecordService;
import com.benchmark.healthcare.patient.Patient;
import com.benchmark.healthcare.patient.PatientService;
import com.benchmark.healthcare.prescription.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * BENCHMARK VALIDATION TESTS — Healthcare Monolith
 *
 * Run: mvn test -Dtest=CrossModuleIntegrationTest
 *
 * Tests verify that cross-module service calls work atomically in the monolith.
 * Compare with the microservices version to see the architectural difference.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:healthcare_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CrossModuleIntegrationTest {

    @Autowired PatientService patientService;
    @Autowired DoctorService doctorService;
    @Autowired AppointmentService appointmentService;
    @Autowired MedicalRecordService medicalRecordService;
    @Autowired PrescriptionService prescriptionService;
    @Autowired BillingService billingService;

    private Patient createPatient(String email) {
        return patientService.createPatient(Patient.builder()
                .firstName("Test").lastName("Patient").email(email).build());
    }

    private Doctor createDoctor(String email) {
        return doctorService.createDoctor(Doctor.builder()
                .firstName("Test").lastName("Doctor").email(email)
                .specialization("General").available(true).build());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 1: Appointment validates patient + doctor atomically
    // ───────────────────────────────────────────────────────────

    @Test
    void scheduleAppointment_shouldValidatePatientAndDoctorInOneTransaction() {
        Patient patient = createPatient("patient1@test.com");
        Doctor doctor = createDoctor("doctor1@test.com");

        // Cross-module: AppointmentService calls PatientService + DoctorService
        Appointment appt = appointmentService.scheduleAppointment(
                patient.getId(), doctor.getId(), null,
                LocalDateTime.now().plusDays(1), "Routine checkup");

        assertThat(appt.getPatientId()).isEqualTo(patient.getId());
        assertThat(appt.getDoctorId()).isEqualTo(doctor.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 2: Unavailable doctor cannot be scheduled
    // ───────────────────────────────────────────────────────────

    @Test
    void scheduleAppointment_shouldRejectUnavailableDoctor() {
        Patient patient = createPatient("patient2@test.com");
        Doctor doctor = createDoctor("doctor2@test.com");
        doctor.setAvailable(false);
        doctorService.updateDoctor(doctor.getId(), doctor);

        // Cross-module: AppointmentService calls DoctorService.validateDoctorAvailability()
        assertThatThrownBy(() -> appointmentService.scheduleAppointment(
                patient.getId(), doctor.getId(), null,
                LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 3: Medical record reads appointment data directly
    // ───────────────────────────────────────────────────────────

    @Test
    void createMedicalRecord_fromAppointment_shouldCopyPatientAndDoctorIds() {
        Patient patient = createPatient("patient3@test.com");
        Doctor doctor = createDoctor("doctor3@test.com");

        Appointment appt = appointmentService.scheduleAppointment(
                patient.getId(), doctor.getId(), null,
                LocalDateTime.now().plusDays(1), null);

        // Cross-module: MedicalRecordService reads Appointment via AppointmentService
        // then reads patient via PatientService — all in one @Transactional
        MedicalRecord record = medicalRecordService.createRecord(
                appt.getId(), "Flu", "Rest and fluids", "No complications");

        // patientId and doctorId should be copied from the appointment
        assertThat(record.getPatientId()).isEqualTo(patient.getId());
        assertThat(record.getDoctorId()).isEqualTo(doctor.getId());
        assertThat(record.getAppointmentId()).isEqualTo(appt.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 4: Prescription reads medical record for context
    // ───────────────────────────────────────────────────────────

    @Test
    void createPrescription_fromMedicalRecord_shouldCopyPatientAndDoctorIds() {
        Patient patient = createPatient("patient4@test.com");
        Doctor doctor = createDoctor("doctor4@test.com");

        Appointment appt = appointmentService.scheduleAppointment(
                patient.getId(), doctor.getId(), null,
                LocalDateTime.now().plusDays(1), null);
        MedicalRecord record = medicalRecordService.createRecord(
                appt.getId(), "Infection", "Antibiotics", null);

        // Cross-module: PrescriptionService reads MedicalRecord via MedicalRecordService
        var prescription = prescriptionService.createFromMedicalRecord(
                record.getId(), "Amoxicillin 500mg 3x daily", "Take with food");

        assertThat(prescription.getPatientId()).isEqualTo(patient.getId());
        assertThat(prescription.getDoctorId()).isEqualTo(doctor.getId());
        assertThat(prescription.getMedicalRecordId()).isEqualTo(record.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 5: Billing created from appointment reads patient ID
    // ───────────────────────────────────────────────────────────

    @Test
    void createBilling_fromAppointment_shouldUsePatientFromAppointment() {
        Patient patient = createPatient("patient5@test.com");
        Doctor doctor = createDoctor("doctor5@test.com");

        Appointment appt = appointmentService.scheduleAppointment(
                patient.getId(), doctor.getId(), null,
                LocalDateTime.now().plusDays(1), null);

        // Cross-module: BillingService reads Appointment via AppointmentService
        var billing = billingService.createBillingForAppointment(
                appt.getId(), new BigDecimal("150.00"), "Consultation fee");

        assertThat(billing.getPatientId()).isEqualTo(patient.getId());
        assertThat(billing.getAppointmentId()).isEqualTo(appt.getId());
        assertThat(billing.getTotalAmount()).isEqualByComparingTo("150.00");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 6: Scheduling conflict detection
    // ───────────────────────────────────────────────────────────

    @Test
    void scheduleAppointment_shouldPreventDoubleBooking() {
        Patient p1 = createPatient("patient6a@test.com");
        Patient p2 = createPatient("patient6b@test.com");
        Doctor doctor = createDoctor("doctor6@test.com");
        LocalDateTime time = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);

        appointmentService.scheduleAppointment(p1.getId(), doctor.getId(), null, time, null);

        // Same doctor, overlapping time — should conflict
        assertThatThrownBy(() -> appointmentService.scheduleAppointment(
                p2.getId(), doctor.getId(), null, time.plusMinutes(15), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has an appointment");
    }
}
