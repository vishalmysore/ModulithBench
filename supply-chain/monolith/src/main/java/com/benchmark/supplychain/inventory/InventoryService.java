package com.benchmark.supplychain.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Inventory getById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Inventory getBySku(String sku, String warehouseCode) {
        return inventoryRepository.findBySkuAndWarehouseCode(sku, warehouseCode)
                .orElseThrow(() -> new InventoryNotFoundException(sku, warehouseCode));
    }

    @Transactional(readOnly = true)
    public List<Inventory> getByWarehouse(String warehouseCode) {
        return inventoryRepository.findByWarehouseCode(warehouseCode);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getBelowReorderPoint() {
        return inventoryRepository.findBelowReorderPoint();
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Called by OrderService when confirming order — reserves stock atomically
    @Transactional
    public void reserveStock(String sku, String warehouseCode, int quantity) {
        int updated = inventoryRepository.reserveStock(sku, warehouseCode, quantity);
        if (updated == 0) {
            Inventory inv = getBySku(sku, warehouseCode);
            throw new IllegalStateException(
                "Insufficient stock for SKU " + sku + " at " + warehouseCode +
                ": available=" + inv.getQuantityAvailable() + ", requested=" + quantity);
        }
    }

    // Called by OrderService.cancelOrder() — releases reserved stock atomically
    @Transactional
    public void releaseReservedStock(String sku, String warehouseCode, int quantity) {
        inventoryRepository.releaseReservedStock(sku, warehouseCode, quantity);
    }

    // Called by WarehouseService when goods physically leave warehouse
    @Transactional
    public void decrementOnDispatch(String sku, String warehouseCode, int quantity) {
        int updated = inventoryRepository.decrementOnDispatch(sku, warehouseCode, quantity);
        if (updated == 0) {
            throw new IllegalStateException(
                "Cannot decrement: no reserved stock for SKU " + sku + " at " + warehouseCode);
        }
    }

    @Transactional
    public Inventory updateInventory(Long id, Inventory details) {
        Inventory inv = getById(id);
        inv.setQuantityOnHand(details.getQuantityOnHand());
        inv.setReorderPoint(details.getReorderPoint());
        inv.setUnitWeight(details.getUnitWeight());
        inv.setUnitValue(details.getUnitValue());
        return inventoryRepository.save(inv);
    }
}
