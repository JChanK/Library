package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.ErrorMessages;
import com.example.library.exception.ResourceNotFoundException;
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
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL.formatted("Author"));
        }
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.AUTHOR_NAME_EMPTY);
        }
        if (author.getSurname() == null || author.getSurname().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.AUTHOR_SURNAME_EMPTY);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(bookId)));

        if (author.getBooks() == null) {
            author.setBooks(new ArrayList<>());
        }

        Author existingAuthor = authorRepository.findByNameAndSurname(author.getName(),
                author.getSurname());
        if (existingAuthor != null) {
            if (book.getAuthors().contains(existingAuthor)) {
                throw new BadRequestException(ErrorMessages.AUTHOR_ALREADY_ASSOCIATED);
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.AUTHOR_NOT_FOUND.formatted(id)));

        authorCacheId.put(id, author);
        return author;
    }

    @Transactional
    public Author update(int id, Author author) {
        if (author == null) {
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL.formatted("Author"));
        }
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.AUTHOR_NAME_EMPTY);
        }
        if (author.getSurname() == null || author.getSurname().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.AUTHOR_SURNAME_EMPTY);
        }

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.AUTHOR_NOT_FOUND.formatted(id)));
        existingAuthor.setName(author.getName());
        existingAuthor.setSurname(author.getSurname());

        Author updatedAuthor = authorRepository.save(existingAuthor);

        authorCacheId.put(id, updatedAuthor);
        return updatedAuthor;
    }

    @Transactional
    public boolean delete(int authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.AUTHOR_NOT_FOUND.formatted(authorId)));

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