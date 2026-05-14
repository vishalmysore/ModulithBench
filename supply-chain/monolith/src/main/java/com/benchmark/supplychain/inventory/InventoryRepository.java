package com.benchmark.supplychain.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuAndWarehouseCode(String sku, String warehouseCode);

    List<Inventory> findByWarehouseCode(String warehouseCode);

    List<Inventory> findBySku(String sku);

    @Query("SELECT i FROM Inventory i WHERE i.quantityOnHand <= i.reorderPoint")
    List<Inventory> findBelowReorderPoint();

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityReserved = i.quantityReserved + :qty WHERE i.sku = :sku AND i.warehouseCode = :warehouse AND (i.quantityOnHand - i.quantityReserved) >= :qty")
    int reserveStock(String sku, String warehouse, int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityReserved = i.quantityReserved - :qty WHERE i.sku = :sku AND i.warehouseCode = :warehouse AND i.quantityReserved >= :qty")
    int releaseReservedStock(String sku, String warehouse, int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityOnHand = i.quantityOnHand - :qty, i.quantityReserved = i.quantityReserved - :qty WHERE i.sku = :sku AND i.warehouseCode = :warehouse AND i.quantityReserved >= :qty")
    int decrementOnDispatch(String sku, String warehouse, int qty);
}
