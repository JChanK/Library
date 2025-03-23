package com.example.library.service;

import com.example.library.exception.CustomException;
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
            throw new CustomException("Book cannot be null", 400);
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new CustomException("Book title cannot be empty", 400);
        }
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            throw new CustomException("Book must have at least one author", 400);
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

        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            bookCacheId.put(id, book);
            return book;
        }
        return null;
    }

    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    @Transactional
    public Book update(Book book, int id) {
        Book existingBook = bookRepository.findById(id).orElse(null);
        if (existingBook != null) {

            List<Author> updatedAuthors = new ArrayList<>(new HashSet<>(book.getAuthors()));
            existingBook.setAuthors(updatedAuthors);

            Book updatedBook = bookRepository.save(book);
            bookCacheId.put(updatedBook.getId(), updatedBook);

            for (Author author : updatedAuthors) {
                authorCacheId.put(author.getId(), author);
            }

            return updatedBook;
        }
        return null;
    }

    @Transactional
    public boolean delete(int bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("Book not found with id: " + bookId, 404));

        reviewRepository.deleteAll(book.getReviews());
        for (Review review : book.getReviews()) {
            reviewCacheId.evict(review.getId());
        }

        Set<Author> authors = new HashSet<>(book.getAuthors());
        for (Author author : authors) {
            author.getBooks().remove(book);
            authorRepository.save(author);

            if (author.getBooks().isEmpty()) {
                authorRepository.delete(author);
                authorCacheId.evict(author.getId());
            }
        }

        bookRepository.delete(book);
        bookCacheId.evict(bookId);

        return true;
    }

    public List<Book> findBooksByReviewMessageContaining(String keyword) {
        List<Book> books = bookRepository.findBooksByReviewMessageContaining(keyword);
        if (books.isEmpty()) {
            throw new CustomException("No books found with reviews containing the message: "
                    + keyword, 404);
        }
        return books;
    }

    public List<Book> findBooksByAuthorNameAndSurnameNative(String authorName,
                                                            String authorSurname) {
        return bookRepository.findBooksByAuthorNameAndSurnameNative(authorName, authorSurname);
    }

}

