package com.example.library.service;

import com.example.library.exception.*;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CacheUtil<Integer, Author> authorCacheId;

    @InjectMocks
    private AuthorService authorService;

    private Author author;
    private Book book;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1);
        author.setName("John");
        author.setSurname("Doe");
        author.setBooks(new ArrayList<>());

        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setAuthors(new ArrayList<>());
    }

    @Test
    void create_ValidAuthor_ReturnsCreatedAuthor() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(null);
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        Author result = authorService.create(author, 1);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(authorCacheId).put(1, author);
    }

    @Test
    void create_NullAuthor_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> authorService.create(null, 1));
    }

    @Test
    void create_InvalidName_ThrowsInvalidProperNameException() {
        author.setName("john");
        assertThrows(InvalidProperNameException.class, () -> authorService.create(author, 1));
    }

    @Test
    void create_ExistingAuthor_ReturnsExistingAuthor() {
        Author existingAuthor = new Author();
        existingAuthor.setId(2);
        existingAuthor.setName("John");
        existingAuthor.setSurname("Doe");
        existingAuthor.setBooks(new ArrayList<>());

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(existingAuthor);

        Author result = authorService.create(author, 1);

        assertEquals(2, result.getId());
        verify(bookRepository).save(book);
    }

    @Test
    void create_AuthorAlreadyAssociated_ThrowsBadRequestException() {
        Author existingAuthor = new Author();
        existingAuthor.setId(2);
        existingAuthor.setName("John");
        existingAuthor.setSurname("Doe");
        book.getAuthors().add(existingAuthor);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(existingAuthor);

        assertThrows(BadRequestException.class, () -> authorService.create(author, 1));
    }

    @Test
    void createBulk_ValidAuthors_ReturnsCreatedAuthors() {
        List<Author> authors = List.of(author);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(null);
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        List<Author> result = authorService.createBulk(authors, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(authorCacheId).put(1, author);
    }

    @Test
    void createBulk_EmptyList_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> authorService.createBulk(Collections.emptyList(), 1));
    }

    @Test
    void findById_ExistingId_ReturnsAuthor() {
        when(authorCacheId.get(1)).thenReturn(null);
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        Author result = authorService.findById(1);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(authorCacheId).put(1, author);
    }

    @Test
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        when(authorCacheId.get(1)).thenReturn(null);
        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.findById(1));
    }

    @Test
    void update_ValidAuthor_ReturnsUpdatedAuthor() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        Author updatedAuthor = new Author();
        updatedAuthor.setName("Jane");
        updatedAuthor.setSurname("Smith");

        Author result = authorService.update(1, updatedAuthor);

        assertNotNull(result);
        assertEquals("Jane", result.getName());
        verify(authorCacheId).put(1, author);
    }

    @Test
    void delete_ExistingAuthor_ReturnsTrue() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        boolean result = authorService.delete(1);

        assertTrue(result);
        verify(authorCacheId).evict(1);
    }

    @Test
    void readAll_ReturnsAllAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<Author> result = authorService.readAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void validateAuthorName_InvalidName_ThrowsException() {
        assertThrows(InvalidProperNameException.class,
                () -> authorService.validateAuthorName("123", "name"));

        assertThrows(InvalidProperNameException.class,
                () -> authorService.validateAuthorName("john", "name"));

        assertThrows(BadRequestException.class,
                () -> authorService.validateAuthorName("", "name"));
    }

    @Test
    void create_BookNotFound_ThrowsResourceNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.create(author, 1));
    }

    @Test
    void update_NullAuthor_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> authorService.update(1, null));
    }

    @Test
    void update_AuthorNotFound_ThrowsResourceNotFoundException() {
        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        Author updateData = new Author();
        updateData.setName("New");
        updateData.setSurname("Name");

        assertThrows(ResourceNotFoundException.class, () -> authorService.update(1, updateData));
    }

    @Test
    void delete_AuthorNotFound_ThrowsResourceNotFoundException() {
        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.delete(1));
    }

    @Test
    void createBulk_AuthorAlreadyAssociated_ThrowsBadRequestException() {
        Author existingAuthor = new Author();
        existingAuthor.setId(2);
        existingAuthor.setName("John");
        existingAuthor.setSurname("Doe");
        existingAuthor.setBooks(new ArrayList<>());

        book.getAuthors().add(existingAuthor);

        List<Author> authors = List.of(author);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(existingAuthor);

        assertThrows(BadRequestException.class, () -> authorService.createBulk(authors, 1));
    }

    @Test
    void create_AuthorNameIsEmpty_ThrowsBadRequestException() {
        author.setName(" ");
        assertThrows(BadRequestException.class, () -> authorService.create(author, 1));
    }

    @Test
    void create_AuthorSurnameIsEmpty_ThrowsBadRequestException() {
        author.setSurname(" ");
        assertThrows(BadRequestException.class, () -> authorService.create(author, 1));
    }

    @Test
    void create_BooksIsNull_InitializesBookList() {
        author.setBooks(null);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(null);
        when(authorRepository.save(any())).thenReturn(author);

        Author result = authorService.create(author, 1);

        assertNotNull(result.getBooks());
    }

    @Test
    void findById_AuthorInCache_ReturnsCachedAuthor() {
        when(authorCacheId.get(1)).thenReturn(author);

        Author result = authorService.findById(1);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(authorRepository, never()).findById(anyInt());
    }

    @Test
    void delete_BookHasOtherAuthors_DoesNotDeleteBook() {
        Author anotherAuthor = new Author();
        anotherAuthor.setId(2);
        anotherAuthor.setName("Alice");
        anotherAuthor.setSurname("Smith");

        book.getAuthors().add(author);
        book.getAuthors().add(anotherAuthor);

        author.getBooks().add(book);

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        boolean result = authorService.delete(1);

        assertTrue(result);
        verify(bookRepository, never()).delete(book); // book не удаляется
    }

    @Test
    void createBulk_AuthorHasNullBooksList_InitializesBooksList() {
        author.setBooks(null);

        List<Author> authors = List.of(author);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(null);
        when(authorRepository.save(any())).thenReturn(author);

        List<Author> result = authorService.createBulk(authors, 1);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getBooks());
    }

    @Test
    void validateAuthorName_EmptySurname_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> authorService.validateAuthorName(" ", "surname"));
    }

    @Test
    void createBulk_ExistingAuthorNotAssociated_AddsToBook() {
        Author existingAuthor = new Author();
        existingAuthor.setId(2);
        existingAuthor.setName("John");
        existingAuthor.setSurname("Doe");
        existingAuthor.setBooks(new ArrayList<>());

        List<Author> authors = List.of(author);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(existingAuthor);

        List<Author> result = authorService.createBulk(authors, 1);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getId());
        verify(bookRepository).save(book);
    }

    @Test
    void createBulk_ExistingAuthorAlreadyAssociated_ThrowsException() {
        Author existingAuthor = new Author();
        existingAuthor.setId(2);
        existingAuthor.setName("John");
        existingAuthor.setSurname("Doe");
        existingAuthor.setBooks(new ArrayList<>());

        book.getAuthors().add(existingAuthor);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findByNameAndSurname("John", "Doe")).thenReturn(existingAuthor);

        List<Author> authors = List.of(author);

        assertThrows(BadRequestException.class,
                () -> authorService.createBulk(authors, 1));
    }

    @Test
    void delete_BookHasOnlyThisAuthor_DeletesBook() {
        book.getAuthors().add(author);
        author.getBooks().add(book);

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        boolean result = authorService.delete(1);

        assertTrue(result);
        verify(bookRepository).delete(book); // эта ветвь
    }

    @Test
    void update_InvalidAuthorData_ThrowsBadRequestException() {
        Author[] invalidAuthors = new Author[4];

        invalidAuthors[0] = new Author();
        invalidAuthors[0].setName("");
        invalidAuthors[0].setSurname("Smith");

        invalidAuthors[1] = new Author();
        invalidAuthors[1].setName(null);
        invalidAuthors[1].setSurname("Smith");

        invalidAuthors[2] = new Author();
        invalidAuthors[2].setName("Jane");
        invalidAuthors[2].setSurname("");

        invalidAuthors[3] = new Author();
        invalidAuthors[3].setName("Jane");
        invalidAuthors[3].setSurname(null);

        for (Author author : invalidAuthors) {
            assertThrows(BadRequestException.class, () -> authorService.update(1, author));
        }
    }

    @Test
    void createBulk_BookNotFound_ThrowsResourceNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authorService.createBulk(List.of(author), 1));
    }



}