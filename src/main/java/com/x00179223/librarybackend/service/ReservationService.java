package com.x00179223.librarybackend.service;
import com.x00179223.librarybackend.model.Reservation;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReservationService {
    Reservation reserveBook(Long bookId, Long userId);

    Reservation checkOutBook(Long reservationId);

    Reservation checkInBook(Long reservationId);

    Reservation cancelReservation(Long reservationId);

    Page<Reservation> findAllReservations(int page, int size, String sortField, String sortDirection);

    Reservation findReservationById(Long id);

    List<Reservation> findReservationsByUserId(Long userId);

    Reservation extendDueDate(Long reservationId);

    List<Reservation> findOverdueCheckins();

    void purgeNonPickedUpReservations();

    void addFine(Long reservationId, Long userId);

    List<Reservation> findOverduePickups();

}
