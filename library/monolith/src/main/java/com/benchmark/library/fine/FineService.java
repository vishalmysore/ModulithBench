package com.benchmark.library.fine;

import com.benchmark.library.loan.Loan;
import com.benchmark.library.loan.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: FineService reads loan data directly from LoanService
 * in the same transaction — no HTTP call, no deserialization, no network latency.
 */
@Service
@RequiredArgsConstructor
public class FineService {

    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    private final FineRepository fineRepository;

    // Direct Spring injection — accesses loan data without HTTP
    private final LoanService loanService;

    @Transactional
    public Fine issueFineForLoan(Long loanId) {
        // Read loan data directly via method call — no HTTP, no JSON parsing
        Loan loan = loanService.getLoanById(loanId);

        if (!loan.isOverdue()) {
            throw new IllegalStateException("Loan " + loanId + " is not overdue, cannot issue fine");
        }

        long daysOverdue = loan.getDaysOverdue();
        BigDecimal amount = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));

        Fine fine = Fine.builder()
                .loanId(loanId)
                .memberId(loan.getMemberId())
                .amount(amount)
                .reason("Overdue by " + daysOverdue + " days at $" + FINE_PER_DAY + "/day")
                .issuedDate(LocalDate.now())
                .build();

        return fineRepository.save(fine);
    }

    @Transactional
    public Fine createFine(Fine fine) {
        return fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public Fine getFineById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new FineNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Fine> getAllFines() {
        return fineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fine> getFinesByMember(Long memberId) {
        return fineRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Fine> getUnpaidFines() {
        return fineRepository.findByPaid(false);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidFinesByMember(Long memberId) {
        return fineRepository.sumUnpaidFinesByMember(memberId);
    }

    @Transactional
    public Fine payFine(Long fineId) {
        Fine fine = getFineById(fineId);
        fine.setPaid(true);
        fine.setPaidDate(LocalDate.now());
        return fineRepository.save(fine);
    }

    @Transactional
    public void deleteFine(Long id) {
        Fine fine = getFineById(id);
        fineRepository.delete(fine);
    }
}
