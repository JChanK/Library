package com.example.library.controller;

import com.example.library.dto.BookDto;
import com.example.library.service.BookService;
import java.util.List;
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

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.create(bookDto);
        return ResponseEntity.status(201).body(createdBook);
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAll() {
        List<BookDto> books = bookService.readAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable int id) {
        BookDto book = bookService.findById(id);
        return book != null
                ? ResponseEntity.ok(book)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<BookDto> getBookByTitle(@RequestParam String title) {
        BookDto book = bookService.findByTitle(title);
        return book != null
                ? ResponseEntity.ok(book)
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(@RequestBody BookDto bookDto, @PathVariable int id) {
        BookDto updatedBook = bookService.update(bookDto, id);
        return updatedBook != null
                ? ResponseEntity.ok(updatedBook)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        boolean isDeleted = bookService.delete(id);
        return isDeleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}