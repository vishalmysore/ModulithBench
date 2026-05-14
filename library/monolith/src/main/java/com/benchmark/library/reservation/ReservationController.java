package com.benchmark.library.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> create(@RequestParam Long bookId,
                                               @RequestParam Long memberId,
                                               @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(bookId, memberId, notes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Reservation>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(reservationService.getReservationsByMember(memberId));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Reservation>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getReservationsByBook(bookId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Reservation>> getPending() {
        return ResponseEntity.ok(reservationService.getPendingReservations());
    }

    @PatchMapping("/{id}/fulfill")
    public ResponseEntity<Reservation> fulfill(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.fulfillReservation(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
