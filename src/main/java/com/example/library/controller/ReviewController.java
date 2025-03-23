package com.example.library.controller;

import com.example.library.dto.ReviewDto;
import com.example.library.mapper.ReviewMapper;
import com.example.library.model.Review;
import com.example.library.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewController(ReviewService reviewService, ReviewMapper reviewMapper) {
        this.reviewService = reviewService;
        this.reviewMapper = reviewMapper;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@PathVariable int bookId,
                                            @RequestBody ReviewDto reviewDto) {
        Review review = reviewMapper.toEntity(reviewDto);

        Review createdReview = reviewService.create(review, bookId);

        ReviewDto createdReviewDto = reviewMapper.toDto(createdReview);

        return ResponseEntity.status(201).body(createdReviewDto);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviewsByBookId(@PathVariable int bookId) {
        List<Review> reviews = reviewService.getReviewsByBookId(bookId);

        List<ReviewDto> reviewDtos = reviews.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable int id) {
        Review review = reviewService.getReviewById(id);
        ReviewDto reviewDto = reviewMapper.toDto(review);

        return ResponseEntity.ok(reviewDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable int id,
                                            @RequestBody ReviewDto reviewDto) {
        Review review = reviewMapper.toEntity(reviewDto);
        Review updatedReview = reviewService.update(id, review);
        ReviewDto updatedReviewDto = reviewMapper.toDto(updatedReview);

        return ResponseEntity.ok(updatedReviewDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}