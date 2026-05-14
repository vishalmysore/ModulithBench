package com.benchmark.library.loan;

import com.benchmark.library.book.BookService;
import com.benchmark.library.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: All cross-module operations (book availability check,
 * member validation, copy decrement) execute in a single atomic transaction.
 * No HTTP calls, no eventual consistency, no distributed transaction complexity.
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    // Direct Spring injection — no HTTP client, no service discovery needed
    private final BookService bookService;
    private final MemberService memberService;

    @Transactional
    public Loan createLoan(Long bookId, Long memberId, String notes) {
        // Step 1: Validate member is active (direct method call, same JVM)
        memberService.validateActiveMember(memberId);

        // Step 2: Check member hasn't exceeded borrow limit (direct method call)
        int maxAllowed = memberService.getMaxBooksAllowed(memberId);
        long activeLoans = loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
        if (activeLoans >= maxAllowed) {
            throw new IllegalStateException(
                "Member " + memberId + " has reached the maximum limit of " + maxAllowed + " active loans");
        }

        // Step 3: Check and decrement book availability atomically (direct method call)
        // This throws BookNotAvailableException if no copies — all within same transaction
        bookService.decrementAvailableCopies(bookId);

        // Step 4: Create the loan record — all four steps are in one DB transaction
        Loan loan = Loan.builder()
                .bookId(bookId)
                .memberId(memberId)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .notes(notes)
                .build();

        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByMember(Long memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Loan> getActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDate.now());
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan " + loanId + " is already returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        // Increment book copies atomically in the same transaction
        bookService.incrementAvailableCopies(loan.getBookId());

        return loanRepository.save(loan);
    }

    @Transactional
    public List<Loan> markOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        overdueLoans.forEach(loan -> loan.setStatus(LoanStatus.OVERDUE));
        return loanRepository.saveAll(overdueLoans);
    }

    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        loanRepository.delete(loan);
    }
}
