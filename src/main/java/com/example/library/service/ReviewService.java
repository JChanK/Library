package com.example.library.service;

import com.example.library.exception.CustomException;
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

    private static final String REVIEW_NOT_FOUND_MESSAGE = "Review not found with id: ";
    private static final String BOOK_NOT_FOUND_MESSAGE = "Book not found with id: ";
    private static final String REVIEW_NULL_MESSAGE = "Review cannot be null";
    private static final String REVIEW_MESSAGE_EMPTY_MESSAGE = "Review message cannot be empty";

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
            throw new CustomException(REVIEW_NULL_MESSAGE, 400);
        }
        if (review.getMessage() == null || review.getMessage().trim().isEmpty()) {
            throw new CustomException(REVIEW_MESSAGE_EMPTY_MESSAGE, 400);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BOOK_NOT_FOUND_MESSAGE + bookId, 404));

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
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));
    }

    @Transactional
    public Review update(int id, Review review) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));

        existingReview.setMessage(review.getMessage());
        Review updatedReview = reviewRepository.save(existingReview);

        reviewCacheId.evict(existingReview.getBook().getId());

        bookCacheId.evict(existingReview.getBook().getId());

        return updatedReview;
    }

    @Transactional
    public void delete(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(REVIEW_NOT_FOUND_MESSAGE + id, 404));
        int bookId = review.getBook().getId();
        reviewRepository.delete(review);

        reviewCacheId.evict(bookId);

        bookCacheId.evict(bookId);
    }

}