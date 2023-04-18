package com.x00179223.librarybackend;

import com.x00179223.librarybackend.model.Book;
import com.x00179223.librarybackend.repository.BookRepository;
import com.x00179223.librarybackend.service.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BookServiceImplTest {

    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bookService = new BookServiceImpl(bookRepository);

        book = Book.builder().id(1L).author("Test Author").title("Test Title")
                .genre("Test Genre").rating(0).ratingCount(0).ratingTotal(0)
                .build();
    }

    @Test
    public void testSave() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book savedBook = bookService.save(book);

        assertEquals("Test Title", savedBook.getTitle());
        assertEquals("Test Author", savedBook.getAuthor());
        assertEquals("Test Genre", savedBook.getGenre());
        assertEquals(0, savedBook.getRating());
        assertEquals(0, savedBook.getRatingCount());
        assertEquals(0, savedBook.getRatingTotal());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    public void testDelete() {
        Long id = 1L;

        bookService.delete(id);

        verify(bookRepository, times(1)).deleteById(id);
    }

    @Test
    public void testUpdate() {
        Long id = 1L;
        Book newBook = Book.builder().id(id).title("New Title").author("New Author")
                .genre("New Genre").quantityAvailable(10).publicationYear(2022).rating(4.0).build();
        Book existingBook = Book.builder().id(id).title("Existing Title").author("Existing Author")
                .genre("Existing Genre").quantityAvailable(5).publicationYear(2021).rating(3.5).build();

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);

        Book updatedBook = bookService.update(id, newBook);

        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals("New Genre", updatedBook.getGenre());
        assertEquals(10, updatedBook.getQuantityAvailable());
        assertEquals(2022, updatedBook.getPublicationYear());
        assertEquals(4.0, updatedBook.getRating());
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    public void testUpdateBookNotFound() {
        Long id = 1L;
        Book newBook = Book.builder().id(id).title("New Title").author("New Author")
                .genre("New Genre").quantityAvailable(10).publicationYear(2022).rating(4.0).build();

        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.update(id, newBook));
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testSearchByTitleOrAuthorOrGenre() {
        String query = "Test";
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().title("Test Book 1").author("Test Author 1").genre("Test Genre 1")
                .quantityAvailable(5).publicationYear(2021).rating(4.0).build());
        books.add(Book.builder().title("Test Book 2").author("Test Author 2").genre("Test Genre 2")
                .quantityAvailable(3).publicationYear(2022).rating(4.5).build());
        when(bookRepository.searchByTitleOrAuthorOrGenre(anyString())).thenReturn(books);

        List<Book> searchResults = bookService.searchByTitleOrAuthorOrGenre(query);

        assertEquals(2, searchResults.size());
        assertEquals("Test Book 1", searchResults.get(0).getTitle());
        assertEquals("Test Author 1", searchResults.get(0).getAuthor());
        assertEquals("Test Genre 1", searchResults.get(0).getGenre());
        assertEquals(5, searchResults.get(0).getQuantityAvailable());
        assertEquals(2021, searchResults.get(0).getPublicationYear());
        assertEquals(4.0, searchResults.get(0).getRating());
        assertEquals("Test Book 2", searchResults.get(1).getTitle());
        assertEquals("Test Author 2", searchResults.get(1).getAuthor());
        assertEquals("Test Genre 2", searchResults.get(1).getGenre());
        assertEquals(3, searchResults.get(1).getQuantityAvailable());
        assertEquals(2022, searchResults.get(1).getPublicationYear());
        assertEquals(4.5, searchResults.get(1).getRating());
        verify(bookRepository, times(1)).searchByTitleOrAuthorOrGenre(query);
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        book.setId(id);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.findById(id);

        assertEquals("Test Title", foundBook.get().getTitle());
        assertEquals("Test Author", foundBook.get().getAuthor());
        assertEquals("Test Genre", foundBook.get().getGenre());
        assertEquals(0, foundBook.get().getRating());
        assertEquals(0, foundBook.get().getRatingCount());
        assertEquals(0, foundBook.get().getRatingTotal());
        verify(bookRepository, times(1)).findById(id);
    }

    @Test
    public void testFindDistinctGenres() {
        List<String> genres = new ArrayList<>();
        genres.add("Test Genre 1");
        genres.add("Test Genre 2");
        when(bookRepository.findDistinctGenres()).thenReturn(genres);

        List<String> distinctGenres = bookService.findDistinctGenres();

        assertEquals(2, distinctGenres.size());
        assertEquals("Test Genre 1", distinctGenres.get(0));
        assertEquals("Test Genre 2", distinctGenres.get(1));
        verify(bookRepository, times(1)).findDistinctGenres();
    }

    @Test
    public void testFindAll() {
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().title("Test Book 1").author("Test Author 1").genre("Test Genre 1")
                .quantityAvailable(5).publicationYear(2021).rating(4.0).build());
        books.add(Book.builder().title("Test Book 2").author("Test Author 2").genre("Test Genre 2")
                .quantityAvailable(3).publicationYear(2022).rating(4.5).build());
        Page<Book> page = new PageImpl<>(books);
        int pageNumber = 0;
        int pageSize = 2;
        String sortField = "title";
        String sortDirection = "asc";

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Book> allBooks = bookService.findAll(pageNumber, pageSize, sortField, sortDirection);

        assertEquals(2, allBooks.getContent().size());
        assertEquals("Test Book 1", allBooks.getContent().get(0).getTitle());
        assertEquals("Test Author 1", allBooks.getContent().get(0).getAuthor());
        assertEquals("Test Genre 1", allBooks.getContent().get(0).getGenre());
        assertEquals(5, allBooks.getContent().get(0).getQuantityAvailable());
        assertEquals(2021, allBooks.getContent().get(0).getPublicationYear());
        assertEquals(4.0, allBooks.getContent().get(0).getRating());
        assertEquals("Test Book 2", allBooks.getContent().get(1).getTitle());
        assertEquals("Test Author 2", allBooks.getContent().get(1).getAuthor());
        assertEquals("Test Genre 2", allBooks.getContent().get(1).getGenre());
        assertEquals(3, allBooks.getContent().get(1).getQuantityAvailable());
        assertEquals(2022, allBooks.getContent().get(1).getPublicationYear());
        assertEquals(4.5, allBooks.getContent().get(1).getRating());
        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    public void testFindByGenre() {
        List<Book> books = new ArrayList<>();
        books.add(Book.builder().title("Test Book 1").author("Test Author 1").genre("Test Genre")
                .quantityAvailable(5).publicationYear(2021).rating(4.0).build());
        books.add(Book.builder().title("Test Book 2").author("Test Author 2").genre("Test Genre")
                .quantityAvailable(3).publicationYear(2022).rating(4.5).build());
        Page<Book> page = new PageImpl<>(books);
        String genre = "Test Genre";
        int pageNumber = 0;
        int pageSize = 2;
        String sortField = "title";
        String sortDirection = "asc";

        when(bookRepository.findByGenre(anyString(), any(PageRequest.class))).thenReturn(page);

        Page<Book> booksByGenre = bookService.findByGenre(genre, pageNumber, pageSize, sortField, sortDirection);

        assertEquals(2, booksByGenre.getContent().size());
        assertEquals("Test Book 1", booksByGenre.getContent().get(0).getTitle());
        assertEquals("Test Author 1", booksByGenre.getContent().get(0).getAuthor());
        assertEquals("Test Genre", booksByGenre.getContent().get(0).getGenre());
        assertEquals(5, booksByGenre.getContent().get(0).getQuantityAvailable());
        assertEquals(2021, booksByGenre.getContent().get(0).getPublicationYear());
        assertEquals(4.0, booksByGenre.getContent().get(0).getRating());
        assertEquals("Test Book 2", booksByGenre.getContent().get(1).getTitle());
        assertEquals("Test Author 2", booksByGenre.getContent().get(1).getAuthor());
        assertEquals("Test Genre", booksByGenre.getContent().get(1).getGenre());
        assertEquals(3, booksByGenre.getContent().get(1).getQuantityAvailable());
        assertEquals(2022, booksByGenre.getContent().get(1).getPublicationYear());
        assertEquals(4.5, booksByGenre.getContent().get(1).getRating());
        verify(bookRepository, times(1)).findByGenre(genre, PageRequest.of(pageNumber, pageSize, Sort.by(sortField)));
    }

}
