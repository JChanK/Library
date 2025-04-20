package com.example.library.controller;

import com.example.library.dto.AuthorDto;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.model.Author;
import com.example.library.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
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
@Tag(name = "Author Controller", description = "API для управления авторами")
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @Autowired
    public AuthorController(AuthorService authorService, AuthorMapper authorMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
    }

    @PostMapping
    @Operation(
            summary = "Создать автора",
            description = "Создает нового автора и связывает его с книгой",
            responses = {   @ApiResponse(
                            responseCode = "201",
                            description = "Автор успешно создан",
                            content = @Content(schema = @Schema(implementation = AuthorDto.class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные автора"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<AuthorDto> create(
            @RequestBody
            @Schema(description = "Данные автора", required = true)
            AuthorDto authorDto,

            @RequestParam
            @Parameter(description = "ID книги для связи", example = "1")
            int bookId) {

        try {
            Author author = authorMapper.toEntity(authorDto);
            Author createdAuthor = authorService.create(author, bookId);
            AuthorDto createdAuthorDto = authorMapper.toDto(createdAuthor);
            return ResponseEntity.status(201).body(createdAuthorDto);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Internal server error", ex);
        }
    }

    @GetMapping
    @Operation(
            summary = "Получить всех авторов",
            description = "Возвращает список всех авторов",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = AuthorDto.class)))
            }
    )
    public ResponseEntity<List<AuthorDto>> getAll() {
        try {
            List<Author> authors = authorService.readAll();
            List<AuthorDto> authorDtos = authors.stream()
                    .map(authorMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(authorDtos);
        } catch (Exception ex) {
            throw new RuntimeException("Internal server error", ex);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить автора по ID",
            description = "Возвращает автора по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Автор найден",
                            content = @Content(schema = @Schema(implementation = AuthorDto.class))),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Автор не найден")
            }
    )
    public ResponseEntity<AuthorDto> getAuthorById(
            @PathVariable
            @Parameter(description = "ID автора", example = "1")
            int id) {

        try {
            Author author = authorService.findById(id);
            AuthorDto authorDto = authorMapper.toDto(author);
            return ResponseEntity.ok(authorDto);
        } catch (ResourceNotFoundException ex) {
            throw ex; // Вернет 404
        } catch (Exception ex) {
            throw new RuntimeException("Internal server error", ex);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить автора",
            description = "Удаляет автора по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Автор успешно удален"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Автор не найден")
            }
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID автора для удаления", example = "1")
            int id) {

        try {
            authorService.delete(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Internal server error", ex);
        }
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Создать несколько авторов",
            description = "Создает несколько авторов и связывает их с книгой",
            responses = {   @ApiResponse(
                            responseCode = "201",
                            description = "Авторы успешно созданы",
                            content = @Content(schema = @Schema
                                    (implementation = AuthorDto[].class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные авторов"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<List<AuthorDto>> createBulk(
            @RequestBody
            @Schema(description = "Список данных авторов", required = true)
            List<AuthorDto> authorDtos,

            @RequestParam
            @Parameter(description = "ID книги для связи", example = "1")
            int bookId) {

        List<Author> authors = authorDtos.stream()
                .map(authorMapper::toEntity)
                .collect(Collectors.toList());

        List<Author> createdAuthors = authorService.createBulk(authors, bookId);
        List<AuthorDto> createdAuthorDtos = createdAuthors.stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.status(201).body(createdAuthorDtos);
    }
}