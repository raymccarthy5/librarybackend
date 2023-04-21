package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final UserService userService;

    private final EmailService emailService;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, BookService bookService, UserService userService, EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public Reservation reserveBook(Long bookId, Long userId) {
        Book book = bookService.findById(bookId).orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = userService.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (book.getQuantityAvailable() <= 0) {
            throw new IllegalArgumentException("Book is not available for reservation");
        }
        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .reservedAt(LocalDateTime.now())
                .pickUpBy(LocalDateTime.now().plusDays(7))
                .build();
        book.setQuantityAvailable(book.getQuantityAvailable() - 1);
        reservationRepository.save(reservation);
        bookService.save(book);
        return reservation;
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.getBook().setQuantityAvailable(reservation.getBook().getQuantityAvailable() + 1);
        reservationRepository.delete(reservation);
        bookService.save(reservation.getBook());
        return reservation;
    }

    @Override
    public Reservation checkOutBook(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setDueDate(LocalDateTime.now().plusDays(14));
        reservation.setReturned(false);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation checkInBook(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setReturned(true);
        return reservationRepository.save(reservation);
    }

    @Override
    public Page<Reservation> findAllReservations(int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.by(sortField);
        if ("desc".equals(sortDirection)) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.findAll(pageable);
    }

    @Override
    public Reservation findReservationById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    }

    @Override
    public List<Reservation> findReservationsByUserId(Long userId) {
        userService.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return reservationRepository.findReservationsByUserId(userId);
    }

    @Override
    public Reservation extendDueDate(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setDueDate(reservation.getDueDate().plusDays(7));
        return reservationRepository.save(        reservation);
    }

    @Scheduled(fixedRate = 4 * 60 * 60 * 1000L) // run every 4 hours (1000=1second)
    @Override
    public List<Reservation> findOverdueCheckins() {
        try {
            System.out.println("Checking for overdue reservations");
            LocalDateTime now = LocalDateTime.now();
            List<Reservation> overdueReservations = reservationRepository.findAllByCheckedOutAtIsNotNullAndDueDateBeforeAndReturnedIsFalse(now);
            for (Reservation reservation : overdueReservations) {
                User user = reservation.getUser();
                if (user != null) {
                    addFine(reservation.getId(), user.getId());
                }
            }
            return overdueReservations;
        } catch (Exception e) {
            System.err.println("Error occurred while finding overdue check-ins: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void purgeNonPickedUpReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> overdueReservations = reservationRepository.findAllByPickUpByBeforeAndCheckedOutAtIsNull(now);
        for (Reservation reservation : overdueReservations) {
            reservationRepository.delete(reservation);
        }
    }
    @Override
    public List<Reservation> findOverduePickups() {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findAllByPickUpByBeforeAndCheckedOutAtIsNull(now);
    }

    @Override
    public void cancelReservationsForUser(Long userId) {
        User user = userService.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Reservation> reservations = reservationRepository.findReservationsByUserId(userId);
        if(user.getFine() > 0.0){
            throw new RuntimeException("Can't delete user with unpaid fine");
        }
        for(Reservation reservation : reservations){
            cancelReservation(reservation.getId());
        }
    }

    @Override
    public void addFine(Long reservationId, Long userId) {
        User user = userService.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        LocalDate today = LocalDate.now();
        if (reservation.getLastFineAddedAt() != null && reservation.getLastFineAddedAt().equals(today)) {
            System.out.println("User has already received a fine for this reservation today, do not add another one");
            return;
        }

        double fine = user.getFine() + 0.50;
        if (fine > 50.0) {
            fine = 50.0;
        }

        emailService.sendOverdueEmail(user.getEmail(), "Overdue Book Return", "You have been issued a 50c charge for overdue book return.");
        user.setFine(fine);
        userService.addFine(user);

        reservation.setLastFineAddedAt(today);
        reservationRepository.save(reservation);
    }
}

