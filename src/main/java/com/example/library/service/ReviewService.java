package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.ErrorMessages;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.util.CacheUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final CacheUtil<Integer, List<Review>> reviewCacheId;
    private final CacheUtil<Integer, Book> bookCacheId;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository,
                         CacheUtil<Integer, List<Review>> reviewCacheId,
                         CacheUtil<Integer, Book> bookCacheId) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.reviewCacheId = reviewCacheId;
        this.bookCacheId = bookCacheId;
    }

    @Transactional
    public Review create(Review review, int bookId) {
        if (review == null) {
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL.formatted("Review"));
        }
        if (review.getMessage() == null || review.getMessage().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.REVIEW_MESSAGE_EMPTY);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.BOOK_NOT_FOUND.formatted(bookId)));

        review.setBook(book);
        Review savedReview = reviewRepository.save(review);

        reviewCacheId.evict(bookId);
        bookCacheId.evict(bookId);

        return savedReview;
    }

    public List<Review> getReviewsByBookId(int bookId) {
        List<Review> cachedReviews = reviewCacheId.get(bookId);
        if (cachedReviews != null) {
            return cachedReviews;
        }

        List<Review> reviews = reviewRepository.findByBookId(bookId);

        reviewCacheId.put(bookId, reviews);
        return reviews;
    }

    public Review getReviewById(int id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.REVIEW_NOT_FOUND.formatted(id)));
    }

    @Transactional
    public Review update(int id, Review review) {
        if (review == null) {
            throw new BadRequestException(ErrorMessages.ENTITY_CANNOT_BE_NULL.formatted("Review"));
        }

        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.REVIEW_NOT_FOUND.formatted(id)));

        existingReview.setMessage(review.getMessage());
        Review updatedReview = reviewRepository.save(existingReview);

        reviewCacheId.evict(existingReview.getBook().getId());
        bookCacheId.evict(existingReview.getBook().getId());

        return updatedReview;
    }

    @Transactional
    public void delete(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.REVIEW_NOT_FOUND.formatted(id)));

        int bookId = review.getBook().getId();
        reviewRepository.delete(review);

        reviewCacheId.evict(bookId);
        bookCacheId.evict(bookId);
    }

}