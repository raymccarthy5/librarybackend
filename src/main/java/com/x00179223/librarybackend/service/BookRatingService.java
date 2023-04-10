package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.model.Book;

public interface BookRatingService {
    Book rateBook(Long bookId, Long userId, int rating);
}
