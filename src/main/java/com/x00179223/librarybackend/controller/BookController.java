package com.x00179223.librarybackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.service.BookService;
import com.x00179223.librarybackend.service.BookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping(value = "api/v1/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController {

    @Autowired
    private final BookService bookService;

    public BookController(BookServiceImpl bookServiceImpl) {
        this.bookService = bookServiceImpl;
    }

    @GetMapping
    public Page<Book> getAllBooks(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "title") String sortField,
                                  @RequestParam(defaultValue = "asc") String sortDirection) {
        return bookService.findAll(page, size, sortField, sortDirection);
    }

    @GetMapping("/{id}")
    public Optional<Book> getBookById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.save(book);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.update(id, book);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }

    @GetMapping("/search/{query}")
    public List<Book> searchBooks(@PathVariable String query) throws JsonProcessingException {
        return bookService.searchByTitleOrAuthorOrGenre(query.toLowerCase());
    }
    @GetMapping("/genres")
    public List<String> getGenres(){
        return bookService.findDistinctGenres();
    }

    @GetMapping("/genre/{genre}")
    public Page<Book> getBooksByGenre(@PathVariable("genre") String genre,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(defaultValue = "title") String sortField,
                                        @RequestParam(defaultValue = "asc") String sortDirection){
        return bookService.findByGenre(genre, page, size, sortField, sortDirection);
    }


}
