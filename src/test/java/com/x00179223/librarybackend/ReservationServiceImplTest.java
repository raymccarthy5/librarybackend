package com.x00179223.librarybackend;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.ReservationRepository;
import com.x00179223.librarybackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationServiceImplTest {
    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;


    @BeforeEach
    public void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        bookService = mock(BookService.class);
        userService = mock(UserService.class);
        reservationService = new ReservationServiceImpl(reservationRepository, bookService, userService, emailService);
    }

    @Test
    public void testFindAllReservations() {
        List<Reservation> expectedReservations = new ArrayList<>();
        expectedReservations.add(
                Reservation.builder()
                        .id(1L)
                        .book(Book.builder().build())
                        .user(null)
                        .reservedAt(null).pickUpBy(null)
                        .checkedOutAt(null).dueDate(null)
                        .returned(true).build()
        );
        expectedReservations.add(
                Reservation.builder()
                        .id(2L)
                        .book(Book.builder().build())
                        .user(null)
                        .reservedAt(null).pickUpBy(null)
                        .checkedOutAt(null).dueDate(null)
                        .returned(true).build()
        );

        int page = 0;
        int size = 2;
        String sortField = "id";
        String sortDirection = "asc";

        Sort sort = Sort.by(sortField);
        if ("desc".equals(sortDirection)) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Reservation> expectedPage = new PageImpl<>(expectedReservations, pageable, expectedReservations.size());

        when(reservationRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Reservation> actualPage = reservationService.findAllReservations(page, size, sortField, sortDirection);

        assertEquals(expectedPage.getContent(), actualPage.getContent());
    }

    @Test
    public void testFindReservationById() {
        Long reservationId = 1L;
        Reservation expectedReservation = (
                Reservation.builder()
                        .id(1L)
                        .book(Book.builder().build())
                        .user(null)
                        .reservedAt(null).pickUpBy(null)
                        .checkedOutAt(null).dueDate(null)
                        .returned(true).build()
        );;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(expectedReservation));

        Reservation actualReservation = reservationService.findReservationById(reservationId);

        assertEquals(expectedReservation, actualReservation);
    }

    @Test
    public void testFindOverdueCheckins() {
        // Create a book
        Book book = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .genre("Test Genre")
                .quantityAvailable(2)
                .publicationYear(2021)
                .build();
        bookService.save(book);

        // Create a user
        User user = User.builder()
                .firstname("testuser")
                .password("password")
                .email("testuser@example.com")
                .build();
        userService.save(user);

        // Create a reservation
        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .reservedAt(LocalDateTime.now().minusDays(5))
                .pickUpBy(LocalDateTime.now().minusDays(2))
                .checkedOutAt(null)
                .dueDate(LocalDateTime.now().minusDays(1))
                .returned(false)
                .build();
        reservationRepository.save(reservation);

        // Call the method to test
        List<Reservation> overdueReservations = reservationService.findOverdueCheckins();

        // Check that the returned list contains the overdue reservation
        assertThat(overdueReservations.contains(reservation));
    }

    @Test
    public void testPurgeNonPickedUpReservations() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> overdueReservations = new ArrayList<>();
        overdueReservations.add(new Reservation());
        overdueReservations.add(new Reservation());

        // Mock repository method calls
        when(reservationRepository.findAllByPickUpByBeforeAndCheckedOutAtIsNull(any(LocalDateTime.class)))
                .thenReturn(overdueReservations);

        // Execute the method being tested
        reservationService.purgeNonPickedUpReservations();

        // Verify the repository method calls
        verify(reservationRepository, times(2)).delete(any(Reservation.class));
    }
}
