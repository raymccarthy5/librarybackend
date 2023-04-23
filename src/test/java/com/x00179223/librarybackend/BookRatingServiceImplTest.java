package com.x00179223.librarybackend;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.model.UserBookRating;
import com.x00179223.librarybackend.repository.BookRatingRepository;
import com.x00179223.librarybackend.repository.BookRepository;
import com.x00179223.librarybackend.service.BookRatingServiceImpl;
import com.x00179223.librarybackend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRatingServiceImplTest {

    @Mock
    private BookRatingRepository bookRatingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookRatingServiceImpl bookRatingService;

    private User user;
    private Book book;
    private UserBookRating userBookRating;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        book = new Book();
        book.setId(1L);
        book.setRating(0);
        book.setRatingCount(0);
        book.setRatingTotal(0);

        userBookRating = new UserBookRating();
        userBookRating.setId(1L);
        userBookRating.setUser(user);
        userBookRating.setBook(book);
        userBookRating.setRating(4);
    }

    @Test
    void rateBook_whenUserAndBookExist_shouldReturnUpdatedBook() {
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookRatingRepository.findByUserAndBook(user, book)).thenReturn(null);
        when(bookRatingRepository.save(any(UserBookRating.class))).thenReturn(userBookRating);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book updatedBook = bookRatingService.rateBook(book.getId(), user.getId(), 4);

        assertNotNull(updatedBook);
        assertEquals(4, updatedBook.getRating());
        assertEquals(1, updatedBook.getRatingCount());
        assertEquals(4, updatedBook.getRatingTotal());

        verify(bookRatingRepository, times(1)).findByUserAndBook(user, book);
        verify(bookRatingRepository, times(1)).save(any(UserBookRating.class));
    }

    @Test
    void rateBook_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(userService.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookRatingService.rateBook(book.getId(), user.getId(), 4));

        verify(userService, times(1)).findById(user.getId());
        verify(bookRepository, never()).findById(book.getId());
    }

    @Test
    void rateBook_whenBookNotFound_shouldThrowEntityNotFoundException() {
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookRatingService.rateBook(book.getId(), user.getId(), 4));

        verify(userService, times(1)).findById(user.getId());
        verify(bookRepository, times(1)).findById(book.getId());
    }

    @Test
    void rateBook_whenRatingOutOfBounds_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookRatingService.rateBook(book.getId(), user.getId(), -1));
        assertThrows(IllegalArgumentException.class, () -> bookRatingService.rateBook(book.getId(), user.getId(), 6));

        verify(userService, never()).findById(user.getId());
        verify(bookRepository, never()).findById(book.getId());
    }

    @Test
    void rateBook_whenUserHasAlreadyRatedBook_shouldUpdateExistingRating() {
        //demo
        book.setRatingCount(1);
        book.setRatingTotal(userBookRating.getRating());

        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookRatingRepository.findByUserAndBook(user, book)).thenReturn(userBookRating);
        when(bookRatingRepository.save(any(UserBookRating.class))).thenReturn(userBookRating);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book updatedBook = bookRatingService.rateBook(book.getId(), user.getId(), 5);

        assertNotNull(updatedBook);
        assertEquals(5, updatedBook.getRating());
        assertEquals(1, updatedBook.getRatingCount());
        assertEquals(5, updatedBook.getRatingTotal());

        verify(bookRatingRepository, times(1)).findByUserAndBook(user, book);
        verify(bookRatingRepository, times(1)).save(any(UserBookRating.class));
    }


}