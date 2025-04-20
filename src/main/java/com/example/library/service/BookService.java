package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.ErrorMessages;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.util.CacheUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ReviewRepository reviewRepository;
    private final CacheUtil<Integer, Book> bookCacheId;
    private final CacheUtil<Integer, Author> authorCacheId;
    private final CacheUtil<Integer, List<Review>> reviewCacheId;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       ReviewRepository reviewRepository,
                       CacheUtil<Integer, Book> bookCacheId,
                       CacheUtil<Integer, Author> authorCacheId,
                       CacheUtil<Integer, List<Review>> reviewCacheId) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.reviewRepository = reviewRepository;
        this.bookCacheId = bookCacheId;
        this.authorCacheId = authorCacheId;
        this.reviewCacheId = reviewCacheId;
    }

    @Transactional
    public Book create(Book book) {
        if (book == null) {
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL
                    .formatted("Book"));
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.BOOK_TITLE_EMPTY);
        }
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            throw new BadRequestException(ErrorMessages.BOOK_NOT_FOUND);
        }

        Set<Author> authorsToAdd = new HashSet<>();
        for (Author author : book.getAuthors()) {
            Author existingAuthor = authorRepository.findByNameAndSurname(author.getName(),
                    author.getSurname());
            authorsToAdd.add(Objects.requireNonNullElse(existingAuthor, author));
        }

        book.setAuthors(new ArrayList<>(authorsToAdd));
        Book savedBook = bookRepository.save(book);

        bookCacheId.put(savedBook.getId(), savedBook);
        for (Author author : savedBook.getAuthors()) {
            authorCacheId.put(author.getId(), author);
        }

        return savedBook;
    }

    public List<Book> readAll() {
        return bookRepository.findAll();
    }

    public Book findById(int id) {
        Book cachedBook = bookCacheId.get(id);
        if (cachedBook != null) {
            return cachedBook;
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(id)));

        bookCacheId.put(id, book);
        return book;
    }

    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title).orElseThrow(() ->
                new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(title)));
    }

    @Transactional
    public Book update(Book book, int id) {
        if (book == null) {
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL
                    .formatted("Book"));
        }

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(id)));

        List<Author> updatedAuthors = new ArrayList<>(new HashSet<>(book.getAuthors()));
        existingBook.setAuthors(updatedAuthors);
        existingBook.setTitle(book.getTitle());

        Book updatedBook = bookRepository.save(existingBook);
        bookCacheId.put(updatedBook.getId(), updatedBook);

        for (Author author : updatedAuthors) {
            authorCacheId.put(author.getId(), author);
        }

        return updatedBook;
    }

    @Transactional
    public boolean delete(int bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(bookId)));

        // Удаление отзывов
        if (book.getReviews() != null) {
            reviewRepository.deleteAll(book.getReviews());
            for (Review review : book.getReviews()) {
                reviewCacheId.evict(review.getId());
            }
        }

        if (book.getAuthors() != null) {
            Set<Author> authors = new HashSet<>(book.getAuthors());
            for (Author author : authors) {
                if (author.getBooks() != null) {
                    author.getBooks().remove(book);
                    authorRepository.save(author);

                    if (author.getBooks().isEmpty()) {
                        authorRepository.delete(author);
                        authorCacheId.evict(author.getId());
                    }
                }
            }
        }

        bookCacheId.evict(bookId);
        bookRepository.delete(book);

        return true;
    }

    public List<Book> findBooksByReviewMessageContaining(String keyword) {
        List<Book> books = bookRepository.findBooksByReviewMessageContaining(keyword);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.REVIEWS_NOT_FOUND_WITH_KEYWORD, keyword));
        }
        return books;
    }

    public List<Book> findBooksByAuthorNameAndSurnameNative(String authorName,
                                                            String authorSurname) {
        List<Book> books = bookRepository.findBooksByAuthorNameAndSurnameNative(authorName,
                authorSurname);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No books found for author: %s %s", authorName, authorSurname));
        }
        return books;
    }

    @Transactional
    public List<Book> createBulk(List<Book> books) {
        if (books == null || books.isEmpty()) {
            throw new BadRequestException(ErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY
                    .formatted("Books"));
        }

        return books.stream()
                .peek(book -> {
                    if (book == null) {
                        throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL
                                .formatted("Book"));
                    }
                    if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                        throw new BadRequestException(ErrorMessages.BOOK_TITLE_EMPTY);
                    }
                    if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
                        throw new BadRequestException(ErrorMessages.BOOK_AUTHORS_EMPTY);
                    }
                })
                .map(book -> {
                    Set<Author> authorsToAdd = new HashSet<>();
                    for (Author author : book.getAuthors()) {
                        Author existingAuthor = authorRepository
                                .findByNameAndSurname(author.getName(), author.getSurname());
                        authorsToAdd.add(Objects.requireNonNullElse(existingAuthor, author));
                    }
                    book.setAuthors(new ArrayList<>(authorsToAdd));
                    Book savedBook = bookRepository.save(book);
                    bookCacheId.put(savedBook.getId(), savedBook);
                    for (Author author : savedBook.getAuthors()) {
                        authorCacheId.put(author.getId(), author);
                    }
                    return savedBook;
                })
                .collect(Collectors.toList());
    }

}