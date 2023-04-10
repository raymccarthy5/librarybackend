package com.x00179223.librarybackend.controller;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.repository.BookRepository;
import com.x00179223.librarybackend.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/stats")
@CrossOrigin(origins = {"https://library-management-frontend.herokuapp.com", "http://localhost:3000"})
public class StatisticsController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/genre-count")
    public ResponseEntity<List<Map<String, Object>>> getBooksByGenre() {
        List<Map<String, Object>> booksCountByGenre = bookRepository.countBooksByGenre();
        return ResponseEntity.ok(booksCountByGenre);
    }

    @GetMapping("reservations-over-time")
    public ResponseEntity<List<Map<String, Object>>> getReservationsOverTime() {
        List<Map<String, Object>> reservationsOverTime = reservationRepository.findReservationsCountByWeek();
        return ResponseEntity.ok(reservationsOverTime);
    }

    @GetMapping("/reservations-by-book")
    public ResponseEntity<List<Map<String, Object>>> getReservationsByBook() {
        List<Map<String, Object>> reservationsByBook = reservationRepository.findReservationsCountByBook();
        return ResponseEntity.ok(reservationsByBook);
    }

    @GetMapping("/books-inventory")
    public List<Book> getBooksInventory() {
        return bookRepository.findAll();
    }
}
