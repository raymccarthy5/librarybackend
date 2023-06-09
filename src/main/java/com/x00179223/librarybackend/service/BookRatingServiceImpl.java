package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.model.UserBookRating;
import com.x00179223.librarybackend.repository.BookRatingRepository;
import com.x00179223.librarybackend.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BookRatingServiceImpl implements BookRatingService  {

    private final BookRatingRepository bookRatingRepository;
    private final BookRepository bookRepository;
    private final UserService userService;

    @Autowired
    public BookRatingServiceImpl(BookRatingRepository bookRatingRepository, BookRepository bookRepository, UserService userService) {
        this.bookRatingRepository = bookRatingRepository;
        this.bookRepository = bookRepository;
        this.userService = userService;
    }

    @Override
    public Book rateBook(Long bookId, Long userId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating value out of bounds: 1 - 5");
        }
        User user = userService.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("Book not found"));

        UserBookRating userBookRating = bookRatingRepository.findByUserAndBook(user, book);

        int newRatingCount = book.getRatingCount();

        if (userBookRating == null) {
            userBookRating = new UserBookRating();
            userBookRating.setUser(user);
            userBookRating.setBook(book);
            newRatingCount += 1;
        } else {
            book.setRatingTotal(book.getRatingTotal() - userBookRating.getRating());
        }

        double doubleRating = rating;

        userBookRating.setRating(doubleRating);
        bookRatingRepository.save(userBookRating);
        double newRatingTotal = book.getRatingTotal() + doubleRating;

        double newRating = BigDecimal.valueOf(newRatingTotal / newRatingCount)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();

        book.setRating(newRating);
        book.setRatingTotal(newRatingTotal);
        book.setRatingCount(newRatingCount);
        return bookRepository.save(book);
    }
}
