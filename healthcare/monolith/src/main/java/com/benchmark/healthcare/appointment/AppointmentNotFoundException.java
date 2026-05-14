package com.benchmark.healthcare.appointment;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(Long id) { super("Appointment not found with id: " + id); }
}
