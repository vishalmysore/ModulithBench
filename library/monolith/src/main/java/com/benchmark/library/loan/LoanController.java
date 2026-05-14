package com.benchmark.library.loan;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<Loan> create(@RequestParam Long bookId,
                                        @RequestParam Long memberId,
                                        @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(bookId, memberId, notes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAll() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Loan>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(loanService.getLoansByMember(memberId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActive() {
        return ResponseEntity.ok(loanService.getActiveLoans());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdue() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    @PostMapping("/mark-overdue")
    public ResponseEntity<List<Loan>> markOverdue() {
        return ResponseEntity.ok(loanService.markOverdueLoans());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
}
