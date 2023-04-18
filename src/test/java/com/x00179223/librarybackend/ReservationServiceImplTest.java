package com.x00179223.librarybackend;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.ReservationRepository;
import com.x00179223.librarybackend.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReservationServiceImplTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    BookService bookService;

    @Mock
    UserService userService;

    @Mock
    EmailService emailService;

    @Mock
    ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationServiceImpl(reservationRepository, bookService, userService, emailService);
    }

    @Test
    void reserveBook_validInput_success() {
        Long bookId = 1L;
        Long userId = 2L;

        Book book = new Book();
        book.setQuantityAvailable(1);

        User user = new User();

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setPickUpBy(LocalDateTime.now().plusDays(7));

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(bookService.save(any())).thenReturn(book);

        Reservation result = reservationService.reserveBook(bookId, userId);

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(user, result.getUser());
        assertEquals(reservation.getReservedAt(), result.getReservedAt());
        assertEquals(reservation.getPickUpBy(), result.getPickUpBy());
    }

    @Test
    void reserveBook_bookNotAvailable_exceptionThrown() {
        Long bookId = 1L;
        Long userId = 2L;

        Book book = new Book();
        book.setQuantityAvailable(0);

        User user = new User();

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> reservationService.reserveBook(bookId, userId));
    }

    @Test
    void cancelReservation_validInput_success() {
        Long reservationId = 1L;

        Book book = new Book();
        book.setQuantityAvailable(1);

        User user = new User();

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setBook(book);
        reservation.setUser(user);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.cancelReservation(reservationId);

        assertNotNull(result);
        assertEquals(reservation, result);
        verify(reservationRepository, times(1)).delete(reservation);
    }

    @Test
    void checkOutBook_validInput_success() {
        Long reservationId = 1L;

        Book book = new Book();
        book.setQuantityAvailable(1);

        User user = new User();

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setCheckedOutAt(null);
        reservation.setDueDate(null);
        reservation.setReturned(false);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation result = reservationService.checkOutBook(reservationId);

        assertNotNull(result);
        assertEquals(reservation, result);
        assertNotNull(result.getCheckedOutAt());
        assertNotNull(result.getDueDate());
        assertFalse(result.isReturned());
    }

    @Test
    void checkInBook_validInput_success() {
        Long reservationId = 1L;

        Book book = new Book();
        book.setQuantityAvailable(1);

        User user = new User();

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setDueDate(LocalDateTime.now().plusDays(14));
        reservation.setReturned(false);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);


        Reservation result = reservationService.checkInBook(reservationId);

        assertNotNull(result);
        assertEquals(reservation, result);
        assertTrue(result.isReturned());
    }

    @Test
    void findAllReservations_validInput_success() {
        int page = 0;
        int size = 10;
        String sortField = "reservedAt";
        String sortDirection = "asc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField).ascending());

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Reservation reservation = new Reservation();
            reservation.setId((long) i);
            reservation.setBook(new Book());
            reservation.setUser(new User());
            reservation.setReservedAt(LocalDateTime.now());
            reservation.setPickUpBy(LocalDateTime.now().plusDays(7));
            reservations.add(reservation);
        }

        Page<Reservation> reservationsPage = new PageImpl<>(reservations, pageable, reservations.size());

        when(reservationRepository.findAll(pageable)).thenReturn(reservationsPage);

        Page<Reservation> result = reservationService.findAllReservations(page, size, sortField, sortDirection);

        assertNotNull(result);
        assertEquals(reservations.size(), result.getContent().size());
        assertEquals(reservations.get(0), result.getContent().get(0));
    }



    @Test
    void findReservationById_validInput_success() {
        Long reservationId = 1L;

        Book book = new Book();
        book.setQuantityAvailable(1);

        User user = new User();

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setBook(book);
        reservation.setUser(user);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.findReservationById(reservationId);

        assertNotNull(result);
        assertEquals(reservation, result);
    }

    @Test
    void findReservationById_invalidInput_exceptionThrown() {
        Long reservationId = 1L;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.findReservationById(reservationId));
    }

    @Test
    void findReservationsByUserId_validInput_success() {
        Long userId = 1L;
        User user = new User();

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Reservation reservation = new Reservation();
            reservation.setId((long) i);
            reservation.setBook(new Book());
            reservation.setUser(new User());
            reservation.setReservedAt(LocalDateTime.now());
            reservation.setPickUpBy(LocalDateTime.now().plusDays(7));
            reservations.add(reservation);
        }
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(reservationService.findReservationsByUserId(userId)).thenReturn(reservations);

        List<Reservation> result = reservationService.findReservationsByUserId(userId);

        assertNotNull(result);
        assertEquals(reservations.size(), result.size());
        assertEquals(reservations.get(0), result.get(0));
    }


//    @Test
//    void extendDueDate_validInput_success(){
//        Long reservationId = 1L;
//        Book book = new Book();
//        book.setQuantityAvailable(1);
//
//        User user = new User();
//
//        Reservation reservation = new Reservation();
//        reservation.setId(reservationId);
//        reservation.setBook(book);
//        reservation.setUser(user);
//        reservation.setDueDate(LocalDateTime.now().plusDays(14));
//
//        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
//        when(reservationRepository.save(any())).thenReturn(reservation);
//
//        Reservation result = reservationService.extendDueDate(reservationId);
//
//        assertNotNull(result);
//        assertEquals(reservation.getDueDate().plusDays(7), result.getDueDate());
//    }

    @Test
    void purgeNonPickedUpReservations_validInput_success() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> overdueReservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Reservation reservation = new Reservation();
            reservation.setId((long) i);
            reservation.setBook(new Book());
            reservation.setUser(new User());
            reservation.setReservedAt(now.minusDays(10));
            reservation.setPickUpBy(now.minusDays(3));
            overdueReservations.add(reservation);
        }

        when(reservationRepository.findAllByPickUpByBeforeAndCheckedOutAtIsNull(now)).thenReturn(overdueReservations);

        reservationService.purgeNonPickedUpReservations();

        verify(reservationRepository, times(3)).delete(any());
    }

    @Test
    void findOverduePickups_validInput_success() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> overdueReservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Reservation reservation = new Reservation();
            reservation.setId((long) i);
            reservation.setBook(new Book());
            reservation.setUser(new User());
            reservation.setReservedAt(now.minusDays(3));
            reservation.setPickUpBy(now.minusDays(1));
            overdueReservations.add(reservation);
        }

        when(reservationRepository.findAllByPickUpByBeforeAndCheckedOutAtIsNull(now)).thenReturn(overdueReservations);

        List<Reservation> result = reservationService.findOverduePickups();

        assertNotNull(result);
        assertEquals(overdueReservations.size(), result.size());
        assertEquals(overdueReservations.get(0), result.get(0));
    }

    @Test
    void addFine_validInput_success() {
        Long reservationId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setFine(0);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setBook(new Book());
        reservation.setUser(user);
        reservation.setDueDate(LocalDate.now().minusDays(3).atStartOfDay());
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);

        reservationService.addFine(reservationId, userId);

        assertEquals(0.5, user.getFine(), 0.001);
        assertNotNull(reservation.getLastFineAddedAt());
    }


}
