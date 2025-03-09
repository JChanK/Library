package com.example.library.controller;

import com.example.library.dto.ReviewDto;
import com.example.library.service.ReviewService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books/{bookId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@PathVariable int bookId,
                                            @RequestBody ReviewDto reviewDto) {
        ReviewDto createdReview = reviewService.create(reviewDto, bookId);
        return ResponseEntity.status(201).body(createdReview);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviewsByBookId(@PathVariable int bookId) {
        List<ReviewDto> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable int id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable int id,
                                            @RequestBody ReviewDto reviewDto) {
        ReviewDto updatedReview = reviewService.update(id, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}