package com.example.library.service;

import com.example.library.dto.BookDto;
import com.example.library.exception.CustomException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.mapper.BookMapper;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import java.util.HashSet;
import java.util.List;
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

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.reviewRepository = reviewRepository;
    }

    public BookDto create(BookDto bookDto) {
        if (bookDto == null) {
            throw new CustomException("BookDto cannot be null", 400);
        }

        if (bookDto.getTitle() == null || bookDto.getTitle().trim().isEmpty()) {
            throw new CustomException("Book title cannot be empty", 400);
        }

        if (bookDto.getAuthors() == null || bookDto.getAuthors().isEmpty()) {
            throw new CustomException("Book must have at least one author", 400);
        }

        Set<Author> authors = bookDto.getAuthors().stream()
                .map(authorDto -> {
                    Author existingAuthor =
                            authorRepository.findByNameAndSurname(authorDto.getName(),
                            authorDto.getSurname());
                    if (existingAuthor != null) {
                        return existingAuthor;
                    } else {
                        Author newAuthor = AuthorMapper.toEntity(authorDto);
                        return authorRepository.save(newAuthor);
                    }
                })
                .collect(Collectors.toSet());

        List<Book> existingBooks =
                bookRepository.findByTitleAndAuthors(bookDto.getTitle(), authors);
        if (!existingBooks.isEmpty()) {
            throw new CustomException("Book with the same title and authors already exists", 400);
        }

        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthors(authors);

        for (Author author : authors) {
            author.getBooks().add(book);
        }

        Book savedBook = bookRepository.save(book);
        return BookMapper.toDto(savedBook);
    }

    public List<BookDto> readAll() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(BookMapper::toDto)
                .toList();
    }

    public BookDto findById(int id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            return BookMapper.toDto(book);
        }
        return null;
    }

    public BookDto findByTitle(String title) {
        Book book = bookRepository.findByTitle(title);
        if (book != null) {
            return BookMapper.toDto(book);
        }
        return null;
    }

    public BookDto update(BookDto bookDto, int id) {
        Book existingBook = bookRepository.findById(id).orElse(null);
        if (existingBook != null) {
            Book book = BookMapper.toEntity(bookDto);
            book.setId(id);
            Book updatedBook = bookRepository.save(book);
            return BookMapper.toDto(updatedBook);
        }
        return null;
    }

    @Transactional
    public boolean delete(int bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("Book not found with id: " + bookId, 404));

        reviewRepository.deleteAll(book.getReviews());

        book.getReviews().clear();
        Set<Author> authors = new HashSet<>(book.getAuthors());

        for (Author author : authors) {
            author.getBooks().remove(book);
            authorRepository.save(author);

            if (author.getBooks().isEmpty()) {
                authorRepository.delete(author);
            }
        }

        book.getAuthors().clear();
        bookRepository.save(book);

        bookRepository.delete(book);

        return true;
    }
}