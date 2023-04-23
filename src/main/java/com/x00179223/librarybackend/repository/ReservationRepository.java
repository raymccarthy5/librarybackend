package com.x00179223.librarybackend.repository;


import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.Reservation;
import com.x00179223.librarybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByBook(Book book);

    @Query("SELECT r FROM Reservation r WHERE r.pickUpBy < :now AND r.checkedOutAt IS NULL")
    List<Reservation> findAllByPickUpByBeforeAndCheckedOutAtIsNull(LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId")
    List<Reservation> findReservationsByUserId(Long userId);

    @Query("SELECT r FROM Reservation r WHERE r.checkedOutAt IS NOT NULL AND r.dueDate < :currentDateTime AND r.returned = false")
    List<Reservation> findAllByCheckedOutAtIsNotNullAndDueDateBeforeAndReturnedIsFalse(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Query(value = "SELECT DATE(reserved_at - INTERVAL (DAYOFWEEK(reserved_at) - 1) DAY) AS startDateOfWeek, COUNT(*) AS count FROM reservations WHERE reserved_at >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) GROUP BY startDateOfWeek ORDER BY startDateOfWeek", nativeQuery = true)
    List<Map<String, Object>> findReservationsCountByWeek();

    @Query(value = "SELECT b.title AS title, COUNT(r.id) AS count FROM reservations r JOIN books b ON r.book_id = b.id GROUP BY b.title", nativeQuery = true)
    List<Map<String, Object>> findReservationsCountByBook();
}