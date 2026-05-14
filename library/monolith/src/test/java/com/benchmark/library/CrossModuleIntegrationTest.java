package com.benchmark.library;

import com.benchmark.library.book.Book;
import com.benchmark.library.book.BookNotAvailableException;
import com.benchmark.library.book.BookService;
import com.benchmark.library.fine.FineService;
import com.benchmark.library.loan.Loan;
import com.benchmark.library.loan.LoanService;
import com.benchmark.library.loan.LoanStatus;
import com.benchmark.library.member.Member;
import com.benchmark.library.member.MemberService;
import com.benchmark.library.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * BENCHMARK VALIDATION TESTS
 *
 * These tests verify that cross-module operations work correctly in the monolith.
 * An AI agent should be able to run these tests and understand what each one validates.
 *
 * Run: mvn test -Dtest=CrossModuleIntegrationTest
 *
 * If an agent adds new features, they should add tests here.
 * If tests fail after an agent's changes, the agent's implementation has a bug.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CrossModuleIntegrationTest {

    @Autowired BookService bookService;
    @Autowired MemberService memberService;
    @Autowired LoanService loanService;
    @Autowired FineService fineService;
    @Autowired ReservationService reservationService;

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 1: Loan creation decrements book copies
    // ───────────────────────────────────────────────────────────

    @Test
    void createLoan_shouldDecrementBookAvailableCopies() {
        Book book = bookService.createBook(Book.builder()
                .title("Test Book").isbn("ISBN-001").author("Author A")
                .totalCopies(3).availableCopies(3).build());
        Member member = memberService.createMember(Member.builder()
                .firstName("Alice").lastName("Smith").email("alice@test.com").build());

        // Cross-module operation: LoanService calls BookService.decrementAvailableCopies()
        Loan loan = loanService.createLoan(book.getId(), member.getId(), null);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getBookId()).isEqualTo(book.getId());
        assertThat(loan.getMemberId()).isEqualTo(member.getId());

        // Verify book copies were decremented (cross-module state change)
        Book updatedBook = bookService.getBookById(book.getId());
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(2);
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 2: Book return increments copies
    // ───────────────────────────────────────────────────────────

    @Test
    void returnBook_shouldIncrementAvailableCopies() {
        Book book = bookService.createBook(Book.builder()
                .title("Return Test").isbn("ISBN-002").totalCopies(1).availableCopies(1).build());
        Member member = memberService.createMember(Member.builder()
                .firstName("Bob").lastName("Jones").email("bob@test.com").build());

        Loan loan = loanService.createLoan(book.getId(), member.getId(), null);
        assertThat(bookService.getBookById(book.getId()).getAvailableCopies()).isEqualTo(0);

        // Cross-module operation: LoanService calls BookService.incrementAvailableCopies()
        loanService.returnBook(loan.getId());

        assertThat(bookService.getBookById(book.getId()).getAvailableCopies()).isEqualTo(1);
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 3: Inactive member cannot borrow
    // ───────────────────────────────────────────────────────────

    @Test
    void createLoan_shouldRejectInactiveMember() {
        Book book = bookService.createBook(Book.builder()
                .title("Inactive Test").isbn("ISBN-003").totalCopies(2).availableCopies(2).build());
        Member member = memberService.createMember(Member.builder()
                .firstName("Charlie").lastName("Brown").email("charlie@test.com").build());
        memberService.deactivateMember(member.getId());

        // Cross-module validation: LoanService calls MemberService.validateActiveMember()
        assertThatThrownBy(() -> loanService.createLoan(book.getId(), member.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");

        // Book copies must NOT have been decremented (atomicity check)
        assertThat(bookService.getBookById(book.getId()).getAvailableCopies()).isEqualTo(2);
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 4: Unavailable book cannot be borrowed
    // ───────────────────────────────────────────────────────────

    @Test
    void createLoan_shouldRejectWhenNoAvailableCopies() {
        Book book = bookService.createBook(Book.builder()
                .title("No Copies").isbn("ISBN-004").totalCopies(1).availableCopies(1).build());
        Member member1 = memberService.createMember(Member.builder()
                .firstName("Dave").lastName("X").email("dave@test.com").build());
        Member member2 = memberService.createMember(Member.builder()
                .firstName("Eve").lastName("Y").email("eve@test.com").build());

        loanService.createLoan(book.getId(), member1.getId(), null);

        // Cross-module validation: BookService.decrementAvailableCopies() throws
        assertThatThrownBy(() -> loanService.createLoan(book.getId(), member2.getId(), null))
                .isInstanceOf(BookNotAvailableException.class);
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 5: Loan limit enforced across modules
    // ───────────────────────────────────────────────────────────

    @Test
    void createLoan_shouldRejectWhenMemberAtMaxLoans() {
        Member member = memberService.createMember(Member.builder()
                .firstName("Fred").lastName("Z").email("fred@test.com")
                .maxBooksAllowed(2).build());

        Book b1 = bookService.createBook(Book.builder().title("B1").isbn("LIM-1").totalCopies(5).availableCopies(5).build());
        Book b2 = bookService.createBook(Book.builder().title("B2").isbn("LIM-2").totalCopies(5).availableCopies(5).build());
        Book b3 = bookService.createBook(Book.builder().title("B3").isbn("LIM-3").totalCopies(5).availableCopies(5).build());

        loanService.createLoan(b1.getId(), member.getId(), null);
        loanService.createLoan(b2.getId(), member.getId(), null);

        // Cross-module: LoanService counts active loans + checks MemberService.getMaxBooksAllowed()
        assertThatThrownBy(() -> loanService.createLoan(b3.getId(), member.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum limit");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 6: Fine calculation reads loan data directly
    // ───────────────────────────────────────────────────────────

    @Test
    void issueFine_shouldFailForNonOverdueLoan() {
        Book book = bookService.createBook(Book.builder()
                .title("Fine Test").isbn("ISBN-005").totalCopies(1).availableCopies(1).build());
        Member member = memberService.createMember(Member.builder()
                .firstName("Grace").lastName("H").email("grace@test.com").build());

        Loan loan = loanService.createLoan(book.getId(), member.getId(), null);

        // Cross-module: FineService calls LoanService.getLoanById() then checks isOverdue()
        assertThatThrownBy(() -> fineService.issueFineForLoan(loan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not overdue");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 7: Reservation validates member + book exist
    // ───────────────────────────────────────────────────────────

    @Test
    void createReservation_shouldRejectInactiveMember() {
        Book book = bookService.createBook(Book.builder()
                .title("Res Test").isbn("ISBN-006").totalCopies(1).availableCopies(1).build());
        Member member = memberService.createMember(Member.builder()
                .firstName("Harry").lastName("I").email("harry@test.com").build());
        memberService.deactivateMember(member.getId());

        // Cross-module: ReservationService calls MemberService.validateActiveMember()
        assertThatThrownBy(() -> reservationService.createReservation(book.getId(), member.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }
}
