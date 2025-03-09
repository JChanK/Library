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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public AuthorDto create(AuthorDto authorDto, int bookId) {
        if (authorDto == null) {
            throw new CustomException("AuthorDto cannot be null", 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("Book not found with id: " + bookId, 404));

        Author existingAuthor = authorRepository.findByNameAndSurname(authorDto.getName(),
                authorDto.getSurname());
        if (existingAuthor != null) {
            if (book.getAuthors().contains(existingAuthor)) {
                throw new CustomException("Author is already associated with this book", 400);
            } else {
                book.getAuthors().add(existingAuthor);
                existingAuthor.getBooks().add(book);
                bookRepository.save(book);
                return AuthorMapper.toDto(existingAuthor);
            }
        } else {
            Author newAuthor = AuthorMapper.toEntity(authorDto);
            newAuthor.getBooks().add(book);
            book.getAuthors().add(newAuthor);

            Author savedAuthor = authorRepository.save(newAuthor);
            return AuthorMapper.toDto(savedAuthor);
        }
    }

    public List<AuthorDto> readAll() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream()
                .map(AuthorMapper::toDto)
                .collect(Collectors.toList());
    }

    public AuthorDto findById(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Author not found with id: " + id, 404));
        return AuthorMapper.toDto(author);
    }

    public AuthorDto update(int id, AuthorDto authorDto) {
        if (authorDto == null) {
            throw new CustomException("AuthorDto cannot be null", 400);
        }

        if (authorDto.getName() == null || authorDto.getName().trim().isEmpty()) {
            throw new CustomException("Author name cannot be empty", 400);
        }
        if (authorDto.getSurname() == null || authorDto.getSurname().trim().isEmpty()) {
            throw new CustomException("Author surname cannot be empty", 400);
        }

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Author not found with id: " + id, 404));

        existingAuthor.setName(authorDto.getName());
        existingAuthor.setSurname(authorDto.getSurname());

        Author updatedAuthor = authorRepository.save(existingAuthor);
        return AuthorMapper.toDto(updatedAuthor);
    }

    @Transactional
    public boolean delete(int authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(()
                        -> new CustomException("Author not found with id: " + authorId, 404));

        Set<Book> books = new HashSet<>(author.getBooks());
        for (Book book : books) {
            book.getAuthors().remove(author);
            bookRepository.save(book);

            if (book.getAuthors().isEmpty()) {
                //reviewRepository.deleteAll(book.getReviews());
                bookRepository.delete(book);
            }
        }
        author.getBooks().clear();
        authorRepository.save(author);
        authorRepository.delete(author);

        return true;
    }

}