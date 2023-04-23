package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.dto.BookIdUserIdRequest;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    Optional<User> findByEmail(String email);
    User save(User user);
    Optional<User> findById(Long id);
    Page<User> findAll(int page, int size, String sortField, String sortDirection);
    void deleteById(Long id);
    User updateUser(Long id, UserUpdateRequest request);
    User addFine(User user);
    User updatePassword(User user);
    Set<Book> addToFavourites(BookIdUserIdRequest request);
    Set<Book> getFavourites(Long userId);
    void removeFavourite(BookIdUserIdRequest request);
}
