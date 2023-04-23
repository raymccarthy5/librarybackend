package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.dto.BookIdUserIdRequest;
import com.x00179223.librarybackend.dto.UserUpdateRequest;
import com.x00179223.librarybackend.exception.ResourceNotFoundException;
import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookService bookService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BookService bookService) {
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Page<User> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.by(sortField);
        if ("desc".equals(sortDirection)) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable);
    }

    @Override
    public void deleteById(Long id){
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        existingUser.setEmail(request.getEmail());
        existingUser.setFirstname(request.getFirstname());
        existingUser.setLastname(request.getLastname());
        existingUser.setPassword(existingUser.getPassword());
        return userRepository.save(existingUser);
    }

    @Override
    public User addFine(User user) {
        userRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.save(user);
    }

    @Override
    public User updatePassword(User user) {
        User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        existingUser.setPassword(user.getPassword());
        return userRepository.save(existingUser);
    }

    @Override
    public Set<Book> addToFavourites(BookIdUserIdRequest request) {
        User existingUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book existingBook = bookService.findById(request.getBookId()).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        Set<Book> favourites = existingUser.getFavourites();
        favourites.add(existingBook);
        existingUser.setFavourites(favourites);
        userRepository.save(existingUser);
        return favourites;
    }

    @Override
    public Set<Book> getFavourites(Long userId) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return existingUser.getFavourites();
    }

    @Override
    public void removeFavourite(BookIdUserIdRequest request) {
        User existingUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book existingBook = bookService.findById(request.getBookId()).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        Set<Book> favourites = existingUser.getFavourites();
        favourites.remove(existingBook);
        existingUser.setFavourites(favourites);
        userRepository.save(existingUser);
    }


}
