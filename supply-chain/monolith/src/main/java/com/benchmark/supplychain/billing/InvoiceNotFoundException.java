package com.benchmark.supplychain.billing;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) { super("Invoice not found with id: " + id); }
    public InvoiceNotFoundException(String msg) { super(msg); }
}
