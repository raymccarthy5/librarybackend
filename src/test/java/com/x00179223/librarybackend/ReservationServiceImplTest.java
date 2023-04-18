package com.x00179223.librarybackend;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.ReservationRepository;
import com.x00179223.librarybackend.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void reserveBook_ShouldReserveBookSuccessfully() {
        Long bookId = 1L;
        Long userId = 2L;
        Book book = Book.builder().id(bookId).title("Test Book").quantityAvailable(5).build();
        User user = User.builder().id(userId).email("test@example.com").build();

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation reservation = reservationService.reserveBook(bookId, userId);

        Assertions.assertNotNull(reservation);
        Assertions.assertEquals(book, reservation.getBook());
        Assertions.assertEquals(user, reservation.getUser());
        Assertions.assertEquals(4, book.getQuantityAvailable());
        verify(bookService, times(1)).save(book);
    }

    @Test
    public void reserveBook_ShouldThrowEntityNotFoundExceptionWhenBookNotFound() {
        Long bookId = 1L;
        Long userId = 2L;

        when(bookService.findById(bookId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.reserveBook(bookId, userId));
        verify(bookService, times(0)).save(any(Book.class));
    }

    @Test
    public void reserveBook_ShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        Long bookId = 1L;
        Long userId = 2L;
        Book book = Book.builder().id(bookId).title("Test Book").quantityAvailable(3).build();

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.reserveBook(bookId, userId));
        verify(bookService, times(0)).save(any(Book.class));
    }


    @Test
    public void reserveBook_ShouldThrowIllegalArgumentExceptionWhenBookNotAvailable() {
        Long bookId = 1L;
        Long userId = 2L;
        Book book = Book.builder().id(bookId).title("Test Book").quantityAvailable(0).build();
        User user = User.builder().id(userId).email("test@example.com").build();

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        Assertions.assertThrows(IllegalArgumentException.class, () -> reservationService.reserveBook(bookId, userId));
        verify(bookService, times(0)).save(any(Book.class));
    }


    @Test
    public void cancelReservation_ShouldCancelReservationSuccessfully() {
        Long reservationId = 1L;
        Book book = Book.builder().id(1L).title("Test Book").quantityAvailable(2).build();
        User user = User.builder().id(2L).email("test@example.com").build();
        Reservation reservation = Reservation.builder().id(reservationId).book(book).user(user).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(bookService.save(book)).thenReturn(book);

        Reservation cancelledReservation = reservationService.cancelReservation(reservationId);

        assertEquals(reservation, cancelledReservation);
        assertEquals(3, book.getQuantityAvailable());
        verify(reservationRepository).delete(reservation);
        verify(bookService).save(book);
    }

    @Test
    public void cancelReservation_ShouldThrowEntityNotFoundExceptionWhenReservationNotFound() {
        Long reservationId = 1L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.cancelReservation(reservationId));

        verify(reservationRepository).findById(reservationId);
        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(bookService);
    }

    @Test
    public void checkOutBook_ShouldCheckOutBookSuccessfully() {
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
    public void checkOutBook_ShouldThrowEntityNotFoundExceptionWhenReservationNotFound() {
        Long reservationId = 1L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.checkOutBook(reservationId));
        verify(reservationRepository).findById(reservationId);
        verifyNoMoreInteractions(reservationRepository);
    }


    @Test
    public void checkInBook_ShouldCheckInBookSuccessfully() {
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .book(new Book())
                .user(new User())
                .checkedOutAt(LocalDateTime.now().minusDays(1))
                .dueDate(LocalDateTime.now().plusDays(13))
                .returned(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation checkedInReservation = reservationService.checkInBook(reservationId);

        assertNotNull(checkedInReservation);
        assertTrue(checkedInReservation.isReturned());

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(reservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    public void checkInBook_ShouldThrowEntityNotFoundExceptionWhenReservationNotFound() {
        Long reservationId = 100L;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.checkInBook(reservationId);
        });

        verify(reservationRepository, times(1)).findById(reservationId);
        verifyNoMoreInteractions(reservationRepository);
    }


    @Test
    void findAllReservations_ShouldReturnPageOfReservations() {
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDirection = "asc";
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        BookService bookService = mock(BookService.class);
        UserService userService = mock(UserService.class);
        EmailService emailService = mock(EmailService.class);
        ReservationServiceImpl reservationService = new ReservationServiceImpl(reservationRepository, bookService, userService, emailService);
        List<Reservation> reservations = Collections.singletonList(new Reservation());
        Page<Reservation> expectedPage = new PageImpl<>(reservations);

        when(reservationRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Reservation> result = reservationService.findAllReservations(page, size, sortField, sortDirection);

        verify(reservationRepository).findAll(any(Pageable.class));
        assertEquals(expectedPage, result);
    }


    @Test
    void findReservationById_ShouldReturnReservationWhenIdExists() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.findReservationById(1L);

        assertEquals(reservation, result);
    }



    @Test
    public void findReservationById_ShouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.findReservationById(1L);
        });
    }

    @Test
    void findReservationsByUserId_ShouldReturnReservationsForUser() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("password")
                .firstname("John")
                .lastname("Doe")
                .build();
        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .book(Book.builder()
                        .id(1L)
                        .ISBN("1234567890")
                        .title("Book 1")
                        .author("Author 1")
                        .quantityAvailable(1)
                        .build())
                .user(user)
                .reservedAt(LocalDateTime.now())
                .pickUpBy(LocalDateTime.now().plusDays(7))
                .build();
        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .book(Book.builder()
                        .id(2L)
                        .ISBN("0987654321")
                        .title("Book 2")
                        .author("Author 2")
                        .quantityAvailable(1)
                        .build())
                .user(user)
                .reservedAt(LocalDateTime.now())
                .pickUpBy(LocalDateTime.now().plusDays(7))
                .build();

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findReservationsByUserId(userId)).thenReturn(Arrays.asList(reservation1, reservation2));

        List<Reservation> reservations = reservationService.findReservationsByUserId(userId);

        assertEquals(2, reservations.size());
        assertEquals(reservation1, reservations.get(0));
        assertEquals(reservation2, reservations.get(1));
    }


    @Test
    public void findReservationsByUserId_ShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        long userId = 1L;
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.findReservationsByUserId(userId);
        });
    }

    @Test
    public void extendDueDate_ShouldExtendDueDateSuccessfully() {
        Long reservationId = 1L;
        LocalDateTime dueDate = LocalDateTime.now().plusDays(14);
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .dueDate(dueDate)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation result = reservationService.extendDueDate(reservationId);

        assertEquals(result.getId(), reservationId);
        assertEquals(result.getDueDate(), dueDate.plusDays(7));
    }

    @Test
    public void extendDueDate_ShouldThrowEntityNotFoundExceptionWhenReservationNotFound() {
        long reservationId = 1L;
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.extendDueDate(reservationId));
        verify(reservationRepository).findById(reservationId);
    }
    @Test
    void purgeNonPickedUpReservations_ShouldPurgeReservations() {
        Book book = Book.builder()
                .title("The Test Book")
                .author("Test Author")
                .ISBN("1234567890123")
                .quantityAvailable(1)
                .build();
        bookService.save(book);
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@test.com")
                .password("password")
                .build();
        userService.save(user);
        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .reservedAt(LocalDateTime.now().minusDays(3))
                .pickUpBy(LocalDateTime.now().minusDays(1))
                .build();
        reservationRepository.save(reservation);

        reservationService.purgeNonPickedUpReservations();

        assertThrows(EntityNotFoundException.class, () -> reservationService.findReservationById(reservation.getId()));
    }

    @Test
    public void findOverduePickups_ShouldReturnOverduePickups() {

    }


    @Test
    void addFine_ShouldAddFineToUser() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(User.builder().id(1L).fine(0.0).build())
                .lastFineAddedAt(null)
                .build();
        User user = User.builder().id(1L).fine(0.0).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        reservationService.addFine(1L, 1L);

        verify(userService, times(1)).addFine(user);
        assertEquals(0.5, user.getFine());
        assertEquals(LocalDate.now(), reservation.getLastFineAddedAt());
        verify(emailService, times(1)).sendOverdueEmail(user.getEmail(), "Overdue Book Return", "You have been issued a 50c charge for overdue book return.");
    }

    @Test
    void addFine_ShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        Long reservationId = 2L;
        when(userService.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> reservationService.addFine(reservationId, userId));
    }

    @Test
    void addFine_ShouldThrowEntityNotFoundExceptionWhenReservationNotFound() {
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.addFine(1L, 1L);
        });
    }
}