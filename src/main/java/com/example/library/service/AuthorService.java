package com.example.library.service;

import com.example.library.exception.CustomException;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.util.CacheUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Author not found with id: ";
    private static final String BOOK_NOT_FOUND_MESSAGE = "Book not found with id: ";
    private static final String AUTHOR_NULL_MESSAGE = "Author cannot be null";
    private static final String AUTHOR_NAME_EMPTY_MESSAGE = "Author name cannot be empty";
    private static final String AUTHOR_SURNAME_EMPTY_MESSAGE = "Author surname cannot be empty";
    private static final String AUTHOR_ALREADY_ASSOCIATED_MESSAGE =
            "Author is already associated with this book";

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CacheUtil<Integer, Author> authorCacheId; // Кэш для авторов

    @Autowired
    public AuthorService(AuthorRepository authorRepository,
                         BookRepository bookRepository, CacheUtil<Integer,
                    Author> authorCacheId) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.authorCacheId = authorCacheId;
    }

    @Transactional
    public Author create(Author author, int bookId) {
        if (author == null) {
            throw new CustomException(AUTHOR_NULL_MESSAGE, 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BOOK_NOT_FOUND_MESSAGE + bookId, 404));

        if (author.getBooks() == null) {
            author.setBooks(new ArrayList<>());
        }

        Author existingAuthor = authorRepository.findByNameAndSurname(author.getName(),
                author.getSurname());
        if (existingAuthor != null) {
            if (book.getAuthors().contains(existingAuthor)) {
                throw new CustomException(AUTHOR_ALREADY_ASSOCIATED_MESSAGE, 400);
            }
            book.getAuthors().add(existingAuthor);
            existingAuthor.getBooks().add(book);
            bookRepository.save(book);
            return existingAuthor;
        }

        author.getBooks().add(book);
        book.getAuthors().add(author);

        Author savedAuthor = authorRepository.save(author);

        authorCacheId.put(savedAuthor.getId(), savedAuthor);

        return savedAuthor;
    }

    public List<Author> readAll() {
        return authorRepository.findAll();
    }

    public Author findById(int id) {
        Author cachedAuthor = authorCacheId.get(id);
        if (cachedAuthor != null) {
            return cachedAuthor;
        }

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException(AUTHOR_NOT_FOUND_MESSAGE + id, 404));

        authorCacheId.put(id, author);

        return author;
    }

    @Transactional
    public Author update(int id, Author author) {
        if (author == null) {
            throw new CustomException(AUTHOR_NULL_MESSAGE, 400);
        }

        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new CustomException(AUTHOR_NAME_EMPTY_MESSAGE, 400);
        }
        if (author.getSurname() == null || author.getSurname().trim().isEmpty()) {
            throw new CustomException(AUTHOR_SURNAME_EMPTY_MESSAGE, 400);
        }

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException(AUTHOR_NOT_FOUND_MESSAGE + id, 404));

        existingAuthor.setName(author.getName());
        existingAuthor.setSurname(author.getSurname());

        Author updatedAuthor = authorRepository.save(existingAuthor);

        authorCacheId.put(id, updatedAuthor);

        return updatedAuthor;
    }

    @Transactional
    public boolean delete(int authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(AUTHOR_NOT_FOUND_MESSAGE + authorId, 404));

        Set<Book> books = new HashSet<>(author.getBooks());
        for (Book book : books) {
            book.getAuthors().remove(author);
            bookRepository.save(book);

            if (book.getAuthors().isEmpty()) {
                bookRepository.delete(book);
            }
        }

        authorRepository.delete(author);
        authorCacheId.evict(authorId);
        return true;
    }

}