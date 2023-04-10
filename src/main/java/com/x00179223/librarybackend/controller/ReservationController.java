package com.x00179223.librarybackend.controller;

import com.x00179223.librarybackend.exception.ResourceNotFoundException;
import com.x00179223.librarybackend.dto.BookIdUserIdRequest;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    @Autowired
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> reserveBook(@RequestBody BookIdUserIdRequest request) {
        Reservation reservation = reservationService.reserveBook(request.getBookId(), request.getUserId());
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/checkout/{id}")
    public ResponseEntity<Reservation> checkOutBook(@PathVariable Long id) {
        Reservation reservation = reservationService.checkOutBook(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/checkin/{id}")
    public ResponseEntity<Reservation> checkInBook(@PathVariable Long id) {
        Reservation reservation = reservationService.checkInBook(id);
        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<Reservation>> findAllReservations(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String sortField,
                                                                 @RequestParam(defaultValue = "asc") String sortDirection) {
        Page<Reservation> reservations = reservationService.findAllReservations(page, size, sortField, sortDirection);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.findReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<Reservation> extendDueDate(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.extendDueDate(id);
            return ResponseEntity.ok(reservation);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/userId/{id}")
    public ResponseEntity<List<Reservation>> getReservationsByUserId(@PathVariable Long id){
        List<Reservation> reservations = reservationService.findReservationsByUserId(id);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/overdue-checkins")
    public ResponseEntity<List<Reservation>> getOverdueCheckins() {
        List<Reservation> overdueReservations = reservationService.findOverdueCheckins();
        return new ResponseEntity<>(overdueReservations, HttpStatus.OK);
    }

    @GetMapping("/purge-non-picked-up")
    public ResponseEntity<Void> purgeNonPickedUpReservations() {
        reservationService.purgeNonPickedUpReservations();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/overdue-pickups")
    public ResponseEntity<List<Reservation>> getOverduePickups() {
        List<Reservation> overduePickups = reservationService.findOverduePickups();
        return ResponseEntity.ok(overduePickups);
    }



}
