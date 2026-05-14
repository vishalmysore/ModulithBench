package com.benchmark.healthcare;

import com.benchmark.healthcare.appointment.AppointmentNotFoundException;
import com.benchmark.healthcare.billing.BillingNotFoundException;
import com.benchmark.healthcare.department.DepartmentNotFoundException;
import com.benchmark.healthcare.doctor.DoctorNotFoundException;
import com.benchmark.healthcare.medicalrecord.MedicalRecordNotFoundException;
import com.benchmark.healthcare.patient.PatientNotFoundException;
import com.benchmark.healthcare.prescription.PrescriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PatientNotFoundException.class, DoctorNotFoundException.class,
            DepartmentNotFoundException.class, AppointmentNotFoundException.class,
            MedicalRecordNotFoundException.class, PrescriptionNotFoundException.class,
            BillingNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage(), 404));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex.getMessage(), 400));
    }

    private Map<String, Object> errorBody(String message, int status) {
        return Map.of("error", message, "status", status, "timestamp", LocalDateTime.now().toString());
    }
}
