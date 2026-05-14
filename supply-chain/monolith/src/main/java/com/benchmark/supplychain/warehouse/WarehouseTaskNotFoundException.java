package com.benchmark.supplychain.warehouse;

public class WarehouseTaskNotFoundException extends RuntimeException {
    public WarehouseTaskNotFoundException(Long id) { super("Warehouse task not found with id: " + id); }
    public WarehouseTaskNotFoundException(String msg) { super(msg); }
}
