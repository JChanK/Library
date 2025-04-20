package com.example.library.service;

import com.example.library.exception.*;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.util.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CacheUtil<Integer, Book> bookCacheId;

    @Mock
    private CacheUtil<Integer, Author> authorCacheId;

    @Mock
    private CacheUtil<Integer, List<Review>> reviewCacheId;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Author author;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setReviews(new ArrayList<>());
        book.setAuthors(new ArrayList<>());

        author = new Author();
        author.setId(1);
        author.setName("John");
        author.setSurname("Doe");
        author.setBooks(new ArrayList<>());

        book.getAuthors().add(author);
        author.getBooks().add(book);

        Review review = new Review();
        review.setId(1);
        review.setMessage("Great book! Highly recommend.");
        review.setBook(book);
        book.getReviews().add(review);
    }

    @Test
    void create_ValidBook_ReturnsCreatedBook() {
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.create(book);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void create_NullBook_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> bookService.create(null));
    }

    @Test
    void create_EmptyTitle_ThrowsBadRequestException() {
        book.setTitle("");
        assertThrows(BadRequestException.class, () -> bookService.create(book));
    }

    @Test
    void createBulk_ValidBooks_ReturnsCreatedBooks() {
        List<Book> books = List.of(book);
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        List<Book> result = bookService.createBulk(books);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createBulk_EmptyList_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> bookService.createBulk(Collections.emptyList()));
    }

    @Test
    void findById_ExistingId_ReturnsBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void update_ValidBook_ReturnsUpdatedBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book savedBook = invocation.getArgument(0);
            book.setTitle(savedBook.getTitle());
            return book;
        });

        Book updatedBookData = new Book();
        updatedBookData.setTitle("Updated Title");
        updatedBookData.setAuthors(List.of(author));

        Book result = bookService.update(updatedBookData, 1);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void delete_ExistingBook_ReturnsTrue() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        boolean result = bookService.delete(1);
        assertTrue(result);
    }

    @Test
    void findBooksByReviewMessageContaining_ValidKeyword_ReturnsBooks() {
        when(bookRepository.findBooksByReviewMessageContaining("good")).thenReturn(List.of(book));

        List<Book> result = bookService.findBooksByReviewMessageContaining("good");

        assertFalse(result.isEmpty());
        assertEquals("Test Book", result.get(0).getTitle());
    }

    @Test
    void findBooksByReviewMessageContaining_NoResults_ThrowsException() {
        when(bookRepository.findBooksByReviewMessageContaining("unknown")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBooksByReviewMessageContaining("unknown"));
    }

    @Test
    void findBooksByAuthorNameAndSurnameNative_ValidNames_ReturnsBooks() {
        when(bookRepository.findBooksByAuthorNameAndSurnameNative("John", "Doe"))
                .thenReturn(List.of(book));

        List<Book> result = bookService.findBooksByAuthorNameAndSurnameNative("John", "Doe");

        assertFalse(result.isEmpty());
        assertEquals("Test Book", result.get(0).getTitle());
    }

    @Test
    void findByTitle_NonExistingTitle_ThrowsException() {
        when(bookRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.findByTitle("Unknown"));
    }

    @Test
    void findBooksByAuthorNameAndSurnameNative_NoResults_ThrowsException() {
        when(bookRepository.findBooksByAuthorNameAndSurnameNative("Unknown", "Author"))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBooksByAuthorNameAndSurnameNative("Unknown", "Author"));
    }
    @Test
    void create_BookWithoutAuthors_ThrowsBadRequestException() {
        book.setAuthors(new ArrayList<>());
        assertThrows(BadRequestException.class, () -> bookService.create(book));
    }
    @Test
    void update_NullBook_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> bookService.update(null, 1));
    }
    @Test
    void update_NonExistingBook_ThrowsResourceNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.update(book, 1));
    }
    @Test
    void delete_BookNotFound_ThrowsResourceNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.delete(1));
    }

    @Test
    void createBulk_BookWithoutAuthors_ThrowsException() {
        book.setAuthors(new ArrayList<>());
        List<Book> books = List.of(book);
        assertThrows(BadRequestException.class, () -> bookService.createBulk(books));
    }
    @Test
    void findByTitle_ExistingTitle_ReturnsBook() {
        when(bookRepository.findByTitle("Test Book")).thenReturn(Optional.of(book));

        Book result = bookService.findByTitle("Test Book");

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void create_NullTitle_ThrowsBadRequestException() {
        book.setTitle(null);
        assertThrows(BadRequestException.class, () -> bookService.create(book));
    }
    @Test
    void createBulk_BookWithNullTitle_ThrowsException() {
        book.setTitle(null);
        List<Book> books = List.of(book);
        assertThrows(BadRequestException.class, () -> bookService.createBulk(books));
    }
    @Test
    void createBulk_BookWithEmptyTitle_ThrowsException() {
        book.setTitle("   ");
        List<Book> books = List.of(book);
        assertThrows(BadRequestException.class, () -> bookService.createBulk(books));
    }
    @Test
    void delete_BookWithoutAuthors_ReturnsTrue() {
        book.setAuthors(null);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        boolean result = bookService.delete(1);

        assertTrue(result);
        verify(bookRepository).delete(book);
    }
    @Test
    void delete_BookWithoutReviews_ReturnsTrue() {
        book.setReviews(null);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        boolean result = bookService.delete(1);

        assertTrue(result);
        verify(reviewRepository, never()).deleteAll(any());
    }
    @Test
    void delete_AuthorHasOtherBooks_NotDeleted() {
        Book anotherBook = new Book();
        anotherBook.setId(2);
        author.setBooks(new ArrayList<>(List.of(anotherBook))); // другая книга
        book.setAuthors(List.of(author));

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        boolean result = bookService.delete(1);

        assertTrue(result);
        verify(authorRepository, never()).delete(author);
    }
}