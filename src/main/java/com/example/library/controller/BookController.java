package com.example.library.controller;


import com.example.library.dto.BookDto;
import com.example.library.mapper.BookMapper;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;


    @Autowired
    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    // Создание книги
    @PostMapping
    public ResponseEntity<BookDto> create(@RequestBody BookDto bookDto) {
        Book book = bookMapper.toEntity(bookDto);

        Book createdBook = bookService.create(book);

        BookDto createdBookDto = bookMapper.toDto(createdBook);

        return ResponseEntity.status(201).body(createdBookDto);
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAll() {
        List<Book> books = bookService.readAll();

        List<BookDto> bookDtos = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable int id) {
        Book book = bookService.findById(id);

        if (book != null) {
            BookDto bookDto = bookMapper.toDto(book);
            return ResponseEntity.ok(bookDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/by-title")
    public ResponseEntity<BookDto> getBookByTitle(@RequestParam String title) {
        Book book = bookService.findByTitle(title);

        if (book != null) {
            BookDto bookDto = bookMapper.toDto(book);
            return ResponseEntity.ok(bookDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/contain")
    public ResponseEntity<List<BookDto>> getBooksByReviewMessageContaining(
            @RequestParam String message) {

        List<Book> books = bookService.findBooksByReviewMessageContaining(message);

        if (books != null && !books.isEmpty()) {
            List<BookDto> bookDtos = books.stream()
                    .map(bookMapper::toDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(bookDtos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/by-author")
    public ResponseEntity<List<BookDto>> getBooksByAuthorNameAndSurname(
            @RequestParam String name,
            @RequestParam String surname) {
        List<Book> books = bookService.findBooksByAuthorNameAndSurnameNative(name, surname);

        List<BookDto> bookDtos = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDtos);
    }

    // Обновление книги
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(@RequestBody BookDto bookDto, @PathVariable int id) {
        Book book = bookMapper.toEntity(bookDto);

        Book updatedBook = bookService.update(book, id);

        if (updatedBook != null) {
            BookDto updatedBookDto = bookMapper.toDto(updatedBook);
            return ResponseEntity.ok(updatedBookDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        boolean isDeleted = bookService.delete(id);
        return isDeleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}