package com.example.library.service;

import com.example.library.dto.ReviewDto;
import com.example.library.exception.CustomException;
import com.example.library.mapper.ReviewMapper;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private static final String REVIEW_NOT_FOUND_MESSAGE = "Review not found with id: ";
    private static final String BOOK_NOT_FOUND_MESSAGE = "Book not found with id: ";
    private static final String REVIEW_DTO_NULL_MESSAGE = "ReviewDto cannot be null";
    private static final String REVIEW_MESSAGE_EMPTY_MESSAGE = "Review message cannot be empty";

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ReviewDto create(ReviewDto reviewDto, int bookId) {
        if (reviewDto == null) {
            throw new CustomException(REVIEW_DTO_NULL_MESSAGE, 400);
        }
        if (reviewDto.getMessage() == null || reviewDto.getMessage().trim().isEmpty()) {
            throw new CustomException(REVIEW_MESSAGE_EMPTY_MESSAGE, 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BOOK_NOT_FOUND_MESSAGE + bookId, 404));

        Review review = ReviewMapper.toEntity(reviewDto);
        review.setBook(book);

        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toDto(savedReview);
    }

    public List<ReviewDto> getReviewsByBookId(int bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new CustomException(BOOK_NOT_FOUND_MESSAGE + bookId, 404);
        }

        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return reviews.stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    public ReviewDto getReviewById(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));
        return ReviewMapper.toDto(review);
    }

    @Transactional
    public ReviewDto update(int id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));

        existingReview.setMessage(reviewDto.getMessage());
        Review updatedReview = reviewRepository.save(existingReview);
        return ReviewMapper.toDto(updatedReview);
    }

    @Transactional
    public void delete(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));
        reviewRepository.delete(review);
    }
}