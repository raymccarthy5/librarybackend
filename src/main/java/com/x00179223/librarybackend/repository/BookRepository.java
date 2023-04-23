package com.x00179223.librarybackend.repository;

import com.x00179223.librarybackend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b WHERE lower(concat(b.title, ' ', b.author, ' ', b.genre)) LIKE lower(concat('%', :query, '%'))")
    List<Book> searchByTitleOrAuthorOrGenre(@Param("query") String query);

    @Query("SELECT b.genre as genre, COUNT(b) as count FROM Book b GROUP BY b.genre")
    List<Map<String, Object>> countBooksByGenre();

    @Query("SELECT DISTINCT b.genre FROM Book b")
    List<String> findDistinctGenres();

    Page<Book> findByGenre(String genre, Pageable pageable);
}
