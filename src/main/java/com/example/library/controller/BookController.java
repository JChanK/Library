package com.example.library.controller;

import com.example.library.dto.BookDto;
import com.example.library.exception.ErrorMessages;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.BookMapper;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Book Controller", description = "API для управления книгами")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    @Autowired
    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @PostMapping
    @Operation(
            summary = "Создать книгу",
            description = "Создает новую книгу и связывает её с авторами.",
            responses = {   @ApiResponse(
                            responseCode = "201",
                            description = "Книга успешно создана",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные книги")
            }
    )
    public ResponseEntity<Book> create(@Valid @RequestBody Book book) {
        Book createdBook = bookService.create(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping
    @Operation(summary = "Получить все книги", description = "Возвращает список всех книг")
    @ApiResponse(responseCode = "200", description = "Успешный запрос",
            content = @Content(schema = @Schema(implementation = BookDto.class)))
    public ResponseEntity<List<BookDto>> getAll() {
        List<Book> books = bookService.readAll();
        List<BookDto> bookDtos = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID", description = "Возвращает книгу по указанному ID")
    @ApiResponse(responseCode = "200", description = "Книга найдена",
            content = @Content(schema = @Schema(implementation = BookDto.class)))
    @ApiResponse(responseCode = "404", description = "Книга не найдена")
    public ResponseEntity<BookDto> getBookById(@PathVariable int id) {
        Book book = bookService.findById(id);

        if (book == null) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.BOOK_NOT_FOUND, "id", id));
        }

        return ResponseEntity.ok(bookMapper.toDto(book));
    }

    @GetMapping("/search/by-title")
    @Operation(summary = "Получить книгу по названию", description = "Возвращает книгу по названию")
    public ResponseEntity<BookDto> getBookByTitle(
            @RequestParam
            @Parameter(description = "Название книги для поиска", example = "Animal Farm")
            String title) {

        Book book = bookService.findByTitle(title);

        if (book == null) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.BOOK_NOT_FOUND, "title", title));
        }

        BookDto bookDto = bookMapper.toDto(book);
        return ResponseEntity.ok(bookDto);
    }

    @GetMapping("/contain")
    @Operation(summary = "Получить книгу по слову в отзыве",
            description = "Возвращает книгу по слову в отзыве")
    @ApiResponse(responseCode = "200", description = "Книга найдена",
            content = @Content(schema = @Schema(implementation = BookDto.class)))
    @ApiResponse(responseCode = "404", description = "Книга не найдена")
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
    @Operation(summary = "Получить книгу по имени и фамилии автора",
            description = "Возвращает книгу по имени и фамилии автора")
    @ApiResponse(responseCode = "200", description = "Книга найдена",
            content = @Content(schema = @Schema(implementation = BookDto.class)))
    @ApiResponse(responseCode = "404", description = "Книга не найдена")
    public ResponseEntity<List<BookDto>> getBooksByAuthorNameAndSurname(
            @RequestParam
            @Parameter(description = "Имя автора", example = "George")
            String name,

            @RequestParam
            @Parameter(description = "Фамилия автора", example = "Orwell")
            String surname) {
        List<Book> books = bookService.findBooksByAuthorNameAndSurnameNative(name, surname);

        List<BookDto> bookDtos = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDtos);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить книгу",
            description = "Обновляет данные книги по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно обновлена",
                            content = @Content(schema = @Schema(implementation = BookDto.class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные книги"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<BookDto> update(
            @RequestBody BookDto bookDto,
            @PathVariable int id) {

        Book book = bookMapper.toEntity(bookDto);
        Book updatedBook = bookService.update(book, id);

        if (updatedBook == null) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.BOOK_NOT_FOUND, "id", id));
        }

        return ResponseEntity.ok(bookMapper.toDto(updatedBook));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить книгу",
            description = "Удаляет книгу по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно удалена"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!bookService.delete(id)) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.BOOK_NOT_FOUND, "id", id));
        }
        return ResponseEntity.ok().build();
    }

}