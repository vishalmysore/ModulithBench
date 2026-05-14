package com.benchmark.library.loan;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<Loan> create(@RequestBody Loan loan) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(loan));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<Loan> update(@PathVariable Long loanId, @RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.updateLoan(loanId, loan));
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> delete(@PathVariable Long loanId) {
        loanService.deleteLoan(loanId);
        return ResponseEntity.noContent().build();
    }
}