package com.benchmark.supplychain.inventory;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(Long id) { super("Inventory not found with id: " + id); }
    public InventoryNotFoundException(String sku, String warehouse) {
        super("Inventory not found for SKU: " + sku + " at warehouse: " + warehouse);
    }
}
