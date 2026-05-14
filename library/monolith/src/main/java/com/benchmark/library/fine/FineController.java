package com.benchmark.library.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @PostMapping("/issue")
    public ResponseEntity<Fine> issueForLoan(@RequestParam Long loanId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fineService.issueFineForLoan(loanId));
    }

    @PostMapping
    public ResponseEntity<Fine> create(@RequestBody Fine fine) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fineService.createFine(fine));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fine> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.getFineById(id));
    }

    @GetMapping
    public ResponseEntity<List<Fine>> getAll() {
        return ResponseEntity.ok(fineService.getAllFines());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Fine>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getFinesByMember(memberId));
    }

    @GetMapping("/member/{memberId}/total-unpaid")
    public ResponseEntity<BigDecimal> getTotalUnpaid(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getTotalUnpaidFinesByMember(memberId));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Fine> payFine(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.payFine(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fineService.deleteFine(id);
        return ResponseEntity.noContent().build();
    }
}
