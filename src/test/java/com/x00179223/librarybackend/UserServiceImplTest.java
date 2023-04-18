package com.x00179223.librarybackend;

import com.x00179223.librarybackend.dto.BookIdUserIdRequest;
import com.x00179223.librarybackend.dto.UserUpdateRequest;
import com.x00179223.librarybackend.exception.ResourceNotFoundException;
import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.repository.UserRepository;
import com.x00179223.librarybackend.service.BookService;
import com.x00179223.librarybackend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findByEmail_ShouldReturnUserWhenEmailExists() {
        String testEmail = "test@example.com";
        User testUser = User.builder()
                .id(1L)
                .email(testEmail)
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail(testEmail);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    public void findByEmail_ShouldReturnEmptyOptionalWhenEmailDoesNotExist() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail(nonExistentEmail);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    public void save_ShouldSaveNewUser() {
        User newUser = User.builder()
                .email("newuser@example.com")
                .firstname("Jane")
                .lastname("Doe")
                .password("password")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email(newUser.getEmail())
                .firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .password(newUser.getPassword())
                .build();

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User result = userService.save(newUser);

        assertEquals(savedUser, result);
        verify(userRepository, times(1)).findByEmail(newUser.getEmail());
        verify(userRepository, times(1)).save(newUser);
    }


    @Test
    public void save_ShouldThrowExceptionWhenEmailAlreadyExists() {
        User existingUser = User.builder()
                .id(1L)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .build();

        User newUserWithExistingEmail = User.builder()
                .email("existing@example.com")
                .firstname("Jane")
                .lastname("Doe")
                .password("password")
                .build();

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(RuntimeException.class, () -> userService.save(newUserWithExistingEmail));
        verify(userRepository, times(1)).findByEmail(existingUser.getEmail());
        verify(userRepository, times(0)).save(newUserWithExistingEmail);
    }

    @Test
    public void findById_ShouldReturnUserWhenIdExists() {
        Long existingUserId = 1L;
        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .build();

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));

        Optional<User> result = userService.findById(existingUserId);

        assertTrue(result.isPresent());
        assertEquals(existingUser, result.get());
        assertTrue(existingUser.isAccountNonExpired());
        assertTrue(existingUser.isCredentialsNonExpired());
        assertTrue(existingUser.isAccountNonLocked());
        assertTrue(existingUser.isEnabled());
        verify(userRepository, times(1)).findById(existingUserId);
    }

    @Test
    public void findById_ShouldReturnEmptyOptionalWhenIdDoesNotExist() {
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(nonExistentUserId);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(nonExistentUserId);
    }

    @Test
    public void findAll_ShouldReturnPageOfUsers() {
        User user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .firstname("Jane")
                .lastname("Doe")
                .password("password")
                .build();

        List<User> users = Arrays.asList(user1, user2);
        int page = 0;
        int size = 2;
        String sortField = "email";
        String sortDirection = "asc";
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortField));
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.findAll(page, size, sortField, sortDirection);

        assertNotNull(result);
        assertEquals(userPage, result);
        assertEquals(2, result.getContent().size());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    public void deleteById_ShouldDeleteUserById() {
        Long userIdToDelete = 1L;
        doNothing().when(userRepository).deleteById(userIdToDelete);

        userService.deleteById(userIdToDelete);

        verify(userRepository, times(1)).deleteById(userIdToDelete);
    }

    @Test
    public void updateUser_ShouldUpdateUserSuccessfully() {
        Long existingUserId = 1L;
        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .build();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder().email("updated@example.com")
                .firstname("Jane").lastname("Smith").build();

        User updatedUser = User.builder()
                .id(existingUserId)
                .email(updateRequest.getEmail())
                .firstname(updateRequest.getFirstname())
                .lastname(updateRequest.getLastname())
                .password(existingUser.getPassword())
                .build();

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(existingUserId, updateRequest);

        assertEquals(updatedUser, result);
        verify(userRepository, times(1)).findById(existingUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateUser_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstname("Jane");
        updateRequest.setLastname("Smith");

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(nonExistentUserId, updateRequest));
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void addFine_ShouldAddFineToUser() {
        Long existingUserId = 1L;
        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .fine(0.0)
                .build();

        double newFine = 5.0;
        User userWithFine = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .fine(newFine)
                .build();

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(userWithFine);

        User result = userService.addFine(userWithFine);

        assertEquals(userWithFine.getFine(), result.getFine(), 0.01);
        verify(userRepository, times(1)).findById(existingUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void addFine_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        User userWithNonExistentId = User.builder()
                .id(nonExistentUserId)
                .email("nonexistent@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .fine(5.0)
                .build();

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addFine(userWithNonExistentId));
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void updatePassword_ShouldUpdatePasswordSuccessfully() {
        Long existingUserId = 1L;
        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("oldPassword")
                .build();

        User updatedPasswordUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("newPassword")
                .build();

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedPasswordUser);

        User result = userService.updatePassword(updatedPasswordUser);

        assertEquals(updatedPasswordUser.getPassword(), result.getPassword());
        verify(userRepository, times(1)).findById(existingUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updatePassword_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        User userWithNonExistentId = User.builder()
                .id(nonExistentUserId)
                .email("nonexistent@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("newPassword")
                .build();

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword(userWithNonExistentId));
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void addToFavourites_ShouldAddBookToFavouritesSuccessfully() {
        Long existingUserId = 1L;
        Long existingBookId = 1L;
        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .favourites(new HashSet<>())
                .build();
        Book existingBook = Book.builder()
                .id(existingBookId)
                .title("Test Book")
                .author("Test Author")
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(existingBookId);
        request.setUserId(existingUserId);

        Set<Book> updatedFavourites = new HashSet<>();
        updatedFavourites.add(existingBook);

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(bookService.findById(existingBookId)).thenReturn(Optional.of(existingBook));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Set<Book> result = userService.addToFavourites(request);

        assertEquals(updatedFavourites, result);
        verify(userRepository, times(1)).findById(existingUserId);
        verify(bookService, times(1)).findById(existingBookId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void addToFavourites_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        Long existingBookId = 1L;

        Book existingBook = Book.builder()
                .id(existingBookId)
                .title("Test Book")
                .author("Test Author")
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(existingBookId);
        request.setUserId(nonExistentUserId);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        when(bookService.findById(existingBookId)).thenReturn(Optional.of(existingBook));

        assertThrows(ResourceNotFoundException.class, () -> userService.addToFavourites(request));
        verify(userRepository, times(1)).findById(nonExistentUserId);
    }

    @Test
    public void addToFavourites_ShouldThrowResourceNotFoundExceptionWhenBookNotFound() {
        Long existingUserId = 1L;
        Long nonExistentBookId = 999L;

        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .favourites(new HashSet<>())
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(nonExistentBookId);
        request.setUserId(existingUserId);

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(bookService.findById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addToFavourites(request));
        verify(userRepository, times(1)).findById(existingUserId);
        verify(bookService, times(1)).findById(nonExistentBookId);
        verify(userRepository, times(0)).save(any(User.class));
    }


    @Test
    public void getFavourites_ShouldReturnFavouritesForUser() {
        Long existingUserId = 1L;

        Book book1 = Book.builder()
                .id(1L)
                .title("Test Book 1")
                .author("Test Author 1")
                .build();

        Book book2 = Book.builder()
                .id(2L)
                .title("Test Book 2")
                .author("Test Author 2")
                .build();

        Set<Book> favourites = new HashSet<>();
        favourites.add(book1);
        favourites.add(book2);

        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .favourites(favourites)
                .build();

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));

        Set<Book> result = userService.getFavourites(existingUserId);

        assertEquals(favourites, result);
        verify(userRepository, times(1)).findById(existingUserId);
    }

    @Test
    public void getFavourites_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getFavourites(nonExistentUserId));
        verify(userRepository, times(1)).findById(nonExistentUserId);
    }

    @Test
    public void removeFavourite_ShouldRemoveBookFromFavouritesSuccessfully() {
        Long existingUserId = 1L;
        Long existingBookId = 1L;

        Book book1 = Book.builder()
                .id(existingBookId)
                .title("Test Book 1")
                .author("Test Author 1")
                .ISBN("12345")
                .build();

        Book book2 = Book.builder()
                .id(2L)
                .title("Test Book 2")
                .author("Test Author 2")
                .build();

        Set<Book> favourites = new HashSet<>();
        favourites.add(book1);
        favourites.add(book2);

        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .favourites(favourites)
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(existingBookId);
        request.setUserId(existingUserId);

        Set<Book> updatedFavourites = new HashSet<>();
        updatedFavourites.add(book2);

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(bookService.findById(existingBookId)).thenReturn(Optional.of(book1));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.removeFavourite(request);

        assertEquals(updatedFavourites, existingUser.getFavourites());
        verify(userRepository, times(1)).findById(existingUserId);
        verify(bookService, times(1)).findById(existingBookId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void removeFavourite_ShouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        Long existingBookId = 1L;

        Book existingBook = Book.builder()
                .id(existingBookId)
                .title("Test Book")
                .author("Test Author")
                .ISBN("1234567890")
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(existingBookId);
        request.setUserId(nonExistentUserId);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        when(bookService.findById(existingBookId)).thenReturn(Optional.of(existingBook));

        assertThrows(ResourceNotFoundException.class, () -> userService.removeFavourite(request));
        verify(userRepository, times(1)).findById(nonExistentUserId);
    }

    @Test
    public void removeFavourite_ShouldThrowResourceNotFoundExceptionWhenBookNotFound() {
        Long existingUserId = 1L;
        Long nonExistentBookId = 999L;

        User existingUser = User.builder()
                .id(existingUserId)
                .email("existing@example.com")
                .firstname("John")
                .lastname("Doe")
                .password("password")
                .favourites(new HashSet<>())
                .build();

        BookIdUserIdRequest request = new BookIdUserIdRequest();
        request.setBookId(nonExistentBookId);
        request.setUserId(existingUserId);

        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));
        when(bookService.findById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.removeFavourite(request));
        verify(userRepository, times(1)).findById(existingUserId);
        verify(bookService, times(1)).findById(nonExistentBookId);
        verify(userRepository, times(0)).save(any(User.class));
    }
}
