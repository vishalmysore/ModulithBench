package com.benchmark.library;

import com.benchmark.library.book.BookNotFoundException;
import com.benchmark.library.book.BookNotAvailableException;
import com.benchmark.library.fine.FineNotFoundException;
import com.benchmark.library.loan.LoanNotFoundException;
import com.benchmark.library.member.MemberNotFoundException;
import com.benchmark.library.reservation.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class, MemberNotFoundException.class,
            LoanNotFoundException.class, FineNotFoundException.class, ReservationNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage(), 404));
    }

    @ExceptionHandler(BookNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNotAvailable(BookNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage(), 409));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex.getMessage(), 400));
    }

    private Map<String, Object> errorBody(String message, int status) {
        return Map.of("error", message, "status", status, "timestamp", LocalDateTime.now().toString());
    }
}
