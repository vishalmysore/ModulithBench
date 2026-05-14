package com.benchmark.supplychain;

import com.benchmark.supplychain.billing.InvoiceNotFoundException;
import com.benchmark.supplychain.carrier.CarrierNotFoundException;
import com.benchmark.supplychain.customs.CustomsNotFoundException;
import com.benchmark.supplychain.inventory.InventoryNotFoundException;
import com.benchmark.supplychain.order.OrderNotFoundException;
import com.benchmark.supplychain.route.RouteNotFoundException;
import com.benchmark.supplychain.tracking.TrackingNotFoundException;
import com.benchmark.supplychain.warehouse.WarehouseTaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({OrderNotFoundException.class, InventoryNotFoundException.class,
            CarrierNotFoundException.class, WarehouseTaskNotFoundException.class,
            RouteNotFoundException.class, CustomsNotFoundException.class,
            TrackingNotFoundException.class, InvoiceNotFoundException.class})
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
