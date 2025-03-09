package com.example.library.service;

import com.example.library.dto.AuthorDto;
import com.example.library.exception.CustomException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
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
    private static final String AUTHOR_DTO_NULL_MESSAGE = "AuthorDto cannot be null";
    private static final String AUTHOR_NAME_EMPTY_MESSAGE = "Author name cannot be empty";
    private static final String AUTHOR_SURNAME_EMPTY_MESSAGE =
            "Author surname cannot be empty";
    private static final String AUTHOR_ALREADY_ASSOCIATED_MESSAGE =
            "Author is already associated with this book";

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public AuthorDto create(AuthorDto authorDto, int bookId) {
        if (authorDto == null) {
            throw new CustomException(AUTHOR_DTO_NULL_MESSAGE, 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BOOK_NOT_FOUND_MESSAGE + bookId, 404));

        Author existingAuthor = authorRepository.findByNameAndSurname(authorDto.getName(),
                authorDto.getSurname());
        if (existingAuthor != null) {
            if (book.getAuthors().contains(existingAuthor)) {
                throw new CustomException(AUTHOR_ALREADY_ASSOCIATED_MESSAGE, 400);
            }
            book.getAuthors().add(existingAuthor);
            existingAuthor.getBooks().add(book);
            bookRepository.save(book);
            return AuthorMapper.toDto(existingAuthor);
        }

        Author newAuthor = AuthorMapper.toEntity(authorDto);
        newAuthor.getBooks().add(book);
        book.getAuthors().add(newAuthor);

        Author savedAuthor = authorRepository.save(newAuthor);
        return AuthorMapper.toDto(savedAuthor);
    }

    public List<AuthorDto> readAll() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream()
                .map(AuthorMapper::toDto)
                .toList();
    }

    public AuthorDto findById(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException(AUTHOR_NOT_FOUND_MESSAGE + id, 404));
        return AuthorMapper.toDto(author);
    }

    @Transactional
    public AuthorDto update(int id, AuthorDto authorDto) {
        if (authorDto == null) {
            throw new CustomException(AUTHOR_DTO_NULL_MESSAGE, 400);
        }

        if (authorDto.getName() == null || authorDto.getName().trim().isEmpty()) {
            throw new CustomException(AUTHOR_NAME_EMPTY_MESSAGE, 400);
        }
        if (authorDto.getSurname() == null || authorDto.getSurname().trim().isEmpty()) {
            throw new CustomException(AUTHOR_SURNAME_EMPTY_MESSAGE, 400);
        }

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException(AUTHOR_NOT_FOUND_MESSAGE + id, 404));

        existingAuthor.setName(authorDto.getName());
        existingAuthor.setSurname(authorDto.getSurname());

        Author updatedAuthor = authorRepository.save(existingAuthor);
        return AuthorMapper.toDto(updatedAuthor);
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
        return true;
    }
}