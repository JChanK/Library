package com.example.library.controller;

import com.example.library.dto.ReviewDto;
import com.example.library.mapper.ReviewMapper;
import com.example.library.model.Review;
import com.example.library.service.ReviewService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books/{bookId}/reviews")
@Tag(name = "Review Controller", description = "API для управления отзывами")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewController(ReviewService reviewService, ReviewMapper reviewMapper) {
        this.reviewService = reviewService;
        this.reviewMapper = reviewMapper;
    }

    @PostMapping
    @Operation(
            summary = "Создать отзыв",
            description = "Создает новый отзыв для указанной книги",
            responses = {   @ApiResponse(
                            responseCode = "201",
                            description = "Отзыв успешно создан",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные отзыва"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<ReviewDto> create(
            @PathVariable
            @Parameter(description = "ID книги", example = "1")
            int bookId,

            @RequestBody
            @Schema(description = "Данные отзыва")
            ReviewDto reviewDto) {

        Review review = reviewMapper.toEntity(reviewDto);
        Review createdReview = reviewService.create(review, bookId);
        ReviewDto createdReviewDto = reviewMapper.toDto(createdReview);

        return ResponseEntity.status(201).body(createdReviewDto);
    }

    @GetMapping
    @Operation(
            summary = "Получить отзывы книги",
            description = "Возвращает все отзывы для указанной книги",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Успешный запрос",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена")
            }
    )
    public ResponseEntity<List<ReviewDto>> getReviewsByBookId(
            @PathVariable
            @Parameter(description = "ID книги", example = "1")
            int bookId) {

        List<Review> reviews = reviewService.getReviewsByBookId(bookId);

        List<ReviewDto> reviewDtos = reviews.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить отзыв по ID",
            description = "Возвращает отзыв по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Отзыв найден",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Отзыв не найден")
            }
    )
    public ResponseEntity<ReviewDto> getReviewById(
            @PathVariable
            @Parameter(description = "ID отзыва", example = "1")
            int id) {

        Review review = reviewService.getReviewById(id);
        ReviewDto reviewDto = reviewMapper.toDto(review);

        return ResponseEntity.ok(reviewDto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить отзыв",
            description = "Обновляет отзыв по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "200",
                            description = "Отзыв успешно обновлен",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
                            @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные отзыва"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Отзыв не найден")
            }
    )
    public ResponseEntity<ReviewDto> update(
            @PathVariable
            @Parameter(description = "ID отзыва", example = "1")
            int id,
            @RequestBody
            @Schema(description = "Обновленные данные отзыва", required = true)
            ReviewDto reviewDto) {

        Review review = reviewMapper.toEntity(reviewDto);
        Review updatedReview = reviewService.update(id, review);
        ReviewDto updatedReviewDto = reviewMapper.toDto(updatedReview);

        return ResponseEntity.ok(updatedReviewDto);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить отзыв",
            description = "Удаляет отзыв по указанному ID",
            responses = {   @ApiResponse(
                            responseCode = "204",
                            description = "Отзыв успешно удален"),
                            @ApiResponse(
                            responseCode = "404",
                            description = "Отзыв не найден")
            }
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID отзыва", example = "1")
            int id) {

        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}