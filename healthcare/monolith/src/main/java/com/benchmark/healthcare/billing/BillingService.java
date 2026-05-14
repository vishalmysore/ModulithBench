package com.benchmark.healthcare.billing;

import com.benchmark.healthcare.appointment.Appointment;
import com.benchmark.healthcare.appointment.AppointmentService;
import com.benchmark.healthcare.patient.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Billing reads appointment data and patient validation
 * happen in a single transaction — ensuring consistency between billing
 * and appointment records without distributed coordination.
 */
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @Transactional
    public Billing createBillingForAppointment(Long appointmentId, BigDecimal amount, String description) {
        // Load appointment data directly — no HTTP call, no serialization overhead
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        patientService.validatePatientExists(appointment.getPatientId());

        Billing billing = Billing.builder()
                .patientId(appointment.getPatientId())
                .appointmentId(appointmentId)
                .totalAmount(amount)
                .description(description)
                .build();

        return billingRepository.save(billing);
    }

    @Transactional
    public Billing createBilling(Billing billing) {
        patientService.validatePatientExists(billing.getPatientId());
        return billingRepository.save(billing);
    }

    @Transactional(readOnly = true)
    public Billing getBillingById(Long id) {
        return billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Billing> getAllBillings() {
        return billingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Billing> getBillingsByPatient(Long patientId) {
        return billingRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(Long patientId) {
        return billingRepository.sumOutstandingByPatient(patientId);
    }

    @Transactional
    public Billing recordPayment(Long billingId, BigDecimal amount) {
        Billing billing = getBillingById(billingId);
        BigDecimal newPaid = billing.getPaidAmount().add(amount);
        billing.setPaidAmount(newPaid);

        if (newPaid.compareTo(billing.getTotalAmount()) >= 0) {
            billing.setStatus(BillingStatus.PAID);
        } else {
            billing.setStatus(BillingStatus.PARTIAL);
        }

        return billingRepository.save(billing);
    }

    @Transactional
    public Billing cancelBilling(Long id) {
        Billing billing = getBillingById(id);
        billing.setStatus(BillingStatus.CANCELLED);
        return billingRepository.save(billing);
    }

    @Transactional
    public void deleteBilling(Long id) {
        billingRepository.delete(getBillingById(id));
    }
}
