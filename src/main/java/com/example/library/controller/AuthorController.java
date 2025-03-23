package com.example.library.controller;

import com.example.library.dto.AuthorDto;
import com.example.library.mapper.AuthorMapper;
import com.example.library.model.Author;
import com.example.library.service.AuthorService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authors")
@Tag(name = "Author request", description = "CRUD operations for authors")
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @Autowired
    public AuthorController(AuthorService authorService, AuthorMapper authorMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
    }

    @PostMapping
    public ResponseEntity<AuthorDto> create(@RequestBody AuthorDto authorDto,
                                            @RequestParam int bookId) {
        Author author = authorMapper.toEntity(authorDto);
        Author createdAuthor = authorService.create(author, bookId);
        AuthorDto createdAuthorDto = authorMapper.toDto(createdAuthor);

        return ResponseEntity.status(201).body(createdAuthorDto);
    }

    @GetMapping
    public ResponseEntity<List<AuthorDto>> getAll() {
        List<Author> authors = authorService.readAll();

        List<AuthorDto> authorDtos = authors.stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(authorDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable int id) {
        Author author = authorService.findById(id);

        if (author != null) {
            AuthorDto authorDto = authorMapper.toDto(author);
            return ResponseEntity.ok(authorDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        boolean isDeleted = authorService.delete(id);
        return isDeleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}