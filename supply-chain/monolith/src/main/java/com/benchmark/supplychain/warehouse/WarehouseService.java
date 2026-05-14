package com.benchmark.supplychain.warehouse;

import com.benchmark.supplychain.inventory.InventoryService;
import com.benchmark.supplychain.order.Order;
import com.benchmark.supplychain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseTaskRepository warehouseTaskRepository;
    private final InventoryService inventoryService;

    // @Lazy breaks circular dependency: OrderService -> WarehouseService -> OrderService
    @Lazy
    private final OrderService orderService;

    // Called by OrderService when confirming order — creates pick task atomically
    @Transactional
    public WarehouseTask createPickTask(Long orderId, String binLocation) {
        // Read order details directly — no HTTP
        Order order = orderService.getOrderById(orderId);

        WarehouseTask task = WarehouseTask.builder()
                .orderId(orderId)
                .warehouseCode(order.getOriginWarehouse())
                .binLocation(binLocation)
                .status(WarehouseTaskStatus.PENDING)
                .build();
        return warehouseTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public WarehouseTask getById(Long id) {
        return warehouseTaskRepository.findById(id)
                .orElseThrow(() -> new WarehouseTaskNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public WarehouseTask getByOrderId(Long orderId) {
        return warehouseTaskRepository.findByOrderId(orderId)
                .orElseThrow(() -> new WarehouseTaskNotFoundException("No warehouse task for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<WarehouseTask> getAll() {
        return warehouseTaskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WarehouseTask> getByWarehouse(String warehouseCode) {
        return warehouseTaskRepository.findByWarehouseCode(warehouseCode);
    }

    @Transactional
    public WarehouseTask startPicking(Long taskId, String operator) {
        WarehouseTask task = getById(taskId);
        task.setStatus(WarehouseTaskStatus.PICKING);
        task.setAssignedOperator(operator);
        task.setPickStartedAt(LocalDateTime.now());
        return warehouseTaskRepository.save(task);
    }

    @Transactional
    public WarehouseTask completePacking(Long taskId) {
        WarehouseTask task = getById(taskId);
        task.setStatus(WarehouseTaskStatus.PACKED);
        task.setPackCompletedAt(LocalDateTime.now());
        return warehouseTaskRepository.save(task);
    }

    @Transactional
    public WarehouseTask dispatch(Long taskId) {
        WarehouseTask task = getById(taskId);
        if (task.getStatus() != WarehouseTaskStatus.PACKED &&
            task.getStatus() != WarehouseTaskStatus.READY_FOR_DISPATCH) {
            throw new IllegalStateException("Task " + taskId + " must be PACKED before dispatch");
        }

        // Reads order to get items, decrements inventory — cross-module, same transaction
        Order order = orderService.getOrderById(task.getOrderId());
        // Parse items JSON and decrement each SKU — simplified here
        inventoryService.decrementOnDispatch(
            "SKU-FROM-ORDER", order.getOriginWarehouse(), 1);

        task.setStatus(WarehouseTaskStatus.DISPATCHED);
        task.setDispatchedAt(LocalDateTime.now());
        return warehouseTaskRepository.save(task);
    }

    // Called by OrderService.cancelOrder() — cancels pick task in same transaction
    @Transactional
    public void cancelPickTask(Long orderId) {
        warehouseTaskRepository.findByOrderId(orderId).ifPresent(task -> {
            if (task.getStatus() == WarehouseTaskStatus.DISPATCHED) {
                throw new IllegalStateException(
                    "Cannot cancel: warehouse task for order " + orderId + " is already DISPATCHED");
            }
            task.setStatus(WarehouseTaskStatus.CANCELLED);
            warehouseTaskRepository.save(task);
        });
    }
}
