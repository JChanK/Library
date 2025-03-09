package com.example.library.controller;

import com.example.library.dto.AuthorDto;
import com.example.library.service.AuthorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorDto> create(@RequestBody AuthorDto authorDto,
                                            @RequestParam int bookId) {
        AuthorDto createdAuthor = authorService.create(authorDto, bookId);
        return ResponseEntity.status(201).body(createdAuthor);
    }

    @GetMapping
    public ResponseEntity<List<AuthorDto>> getAll() {
        List<AuthorDto> authors = authorService.readAll();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable int id) {
        AuthorDto author = authorService.findById(id);
        return author != null
                ? ResponseEntity.ok(author)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        boolean isDeleted = authorService.delete(id);
        return isDeleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}