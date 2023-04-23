package com.x00179223.librarybackend.controller;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.dto.BookIdUserIdRequest;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.dto.UserUpdateRequest;
import com.x00179223.librarybackend.service.ReservationService;
import com.x00179223.librarybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/v1/users")
@CrossOrigin(origins = {"https://library-management-frontend.herokuapp.com", "http://localhost:3000"})
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public Page<User> findAll(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortField,
                              @RequestParam(defaultValue = "asc") String sortDirection) {

        return userService.findAll(page, size, sortField, sortDirection);
    }

    @PutMapping("/update/{id}")
    public User updateUser(@PathVariable long id, @RequestBody UserUpdateRequest request){
        return userService.updateUser(id, request);
    }

    @GetMapping("/email/{email}")
    public Optional<User> findByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @GetMapping("/id/{id}")
    public Optional<User> findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        reservationService.cancelReservationsForUser(id);
        userService.deleteById(id);
    }

    @PutMapping("/add-to-favourites")
    public Set<Book> addToFavourites(@RequestBody BookIdUserIdRequest request){
        return userService.addToFavourites(request);
    }

    @PutMapping("/remove-favourite")
    public void removeFavourite(@RequestBody BookIdUserIdRequest request){ userService.removeFavourite(request); }

    @GetMapping("/favourites/{id}")
    public Set<Book> getFavourites(@PathVariable Long id){
        return userService.getFavourites(id);
    }
}
