package com.benchmark.library.reservation;

import com.benchmark.library.book.Book;
import com.benchmark.library.book.BookService;
import com.benchmark.library.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * MONOLITH ADVANTAGE: Reservation creation validates both book and member
 * in a single transaction — impossible with separate microservices without
 * distributed transactions or saga patterns.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    // Direct cross-module injection — both in same Spring context
    private final BookService bookService;
    private final MemberService memberService;

    @Transactional
    public Reservation createReservation(Long bookId, Long memberId, String notes) {
        // Validate member is active — direct call, no HTTP
        memberService.validateActiveMember(memberId);

        // Verify book exists — direct call, no HTTP
        Book book = bookService.getBookById(bookId);

        // Prevent duplicate pending reservations
        if (reservationRepository.existsByMemberIdAndBookIdAndStatus(memberId, bookId, ReservationStatus.PENDING)) {
            throw new IllegalStateException(
                "Member " + memberId + " already has a pending reservation for book " + bookId);
        }

        // If book is available, suggest borrowing instead — cross-module state check in one call
        ReservationStatus initialStatus = book.isAvailable()
                ? ReservationStatus.PENDING
                : ReservationStatus.PENDING;

        Reservation reservation = Reservation.builder()
                .bookId(bookId)
                .memberId(memberId)
                .status(initialStatus)
                .notes(notes)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByMember(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByBook(Long bookId) {
        return reservationRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByStatus(ReservationStatus.PENDING);
    }

    @Transactional
    public Reservation fulfillReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Reservation " + reservationId + " is not in PENDING status");
        }
        reservation.setStatus(ReservationStatus.FULFILLED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.delete(reservation);
    }
}
