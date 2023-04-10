package com.x00179223.librarybackend.repository;
import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.model.UserBookRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRatingRepository extends JpaRepository<UserBookRating, Long> {
    @Query("SELECT ubr FROM UserBookRating ubr WHERE ubr.user = :user AND ubr.book = :book")
    UserBookRating findByUserAndBook(@Param("user") User user, @Param("book") Book book);
}
