package com.example.library.service;

import com.example.library.dto.ReviewDto;
import com.example.library.exception.CustomException;
import com.example.library.mapper.ReviewMapper;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    public ReviewDto create(ReviewDto reviewDto, int bookId) {
        if (reviewDto == null) {
            throw new CustomException("ReviewDto cannot be null", 400);
        }
        if (reviewDto.getMessage() == null || reviewDto.getMessage().trim().isEmpty()) {
            throw new CustomException("Review message cannot be empty", 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("Book not found with id: " + bookId, 404));

        Review review = ReviewMapper.toEntity(reviewDto);
        review.setBook(book);

        Review savedReview = reviewRepository.save(review);

        return ReviewMapper.toDto(savedReview);
    }

    public List<ReviewDto> getReviewsByBookId(int bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException("Book not found with id: " + bookId, 404);
        }

        List<Review> reviews = reviewRepository.findByBookId(bookId);

        return reviews.stream()
                .map(ReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto getReviewById(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException("Review not found with id: " + id, 404));
        return ReviewMapper.toDto(review);
    }

    public ReviewDto update(int id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException("Review not found with id: " + id, 404));

        existingReview.setMessage(reviewDto.getMessage());
        Review updatedReview = reviewRepository.save(existingReview);
        return ReviewMapper.toDto(updatedReview);
    }

    public void delete(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException("Review not found with id: " + id, 404));
        reviewRepository.delete(review);
    }
}