package com.x00179223.librarybackend.controller;

import com.x00179223.librarybackend.dto.RatingRequest;
import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.service.BookRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/rate")
@CrossOrigin(origins = {"https://library-management-frontend.herokuapp.com", "http://localhost:3000"})
public class BookRatingController {

    @Autowired
    private final BookRatingService bookRatingService;

    public BookRatingController(BookRatingService bookRatingService) {
        this.bookRatingService = bookRatingService;
    }

    @PostMapping("/book/{bookId}")
    public Book rateBook(@PathVariable("bookId") Long bookId, @RequestBody RatingRequest ratingRequest) {
        return bookRatingService.rateBook(bookId, ratingRequest.getUserId(), ratingRequest.getRating());
    }
}
