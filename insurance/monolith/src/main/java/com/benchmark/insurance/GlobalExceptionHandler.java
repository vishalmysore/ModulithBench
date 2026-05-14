package com.benchmark.insurance;

import com.benchmark.insurance.agent.AgentNotFoundException;
import com.benchmark.insurance.claim.ClaimNotFoundException;
import com.benchmark.insurance.coverage.CoverageNotFoundException;
import com.benchmark.insurance.customer.CustomerNotFoundException;
import com.benchmark.insurance.policy.PolicyNotFoundException;
import com.benchmark.insurance.premium.PremiumNotFoundException;
import com.benchmark.insurance.settlement.SettlementNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class, AgentNotFoundException.class,
            PolicyNotFoundException.class, CoverageNotFoundException.class,
            PremiumNotFoundException.class, ClaimNotFoundException.class,
            SettlementNotFoundException.class})
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
