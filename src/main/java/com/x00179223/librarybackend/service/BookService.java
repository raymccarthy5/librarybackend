package com.x00179223.librarybackend.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x00179223.librarybackend.model.Book;
import org.springframework.data.domain.Page;

public interface BookService {
    Optional<Book> findById(Long id);
    Page<Book> findAll(int page, int size, String sortField, String sortDirection);
    Book save(Book book);
    void delete(Long id);
    Book update(Long id, Book book);
    List<Book> searchByTitleOrAuthorOrGenre(String query) throws JsonProcessingException;
    List<String> findDistinctGenres();
    Page<Book> findByGenre(String genre, int page, int size, String sortField, String sortDirection);
}
