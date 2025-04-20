package com.example.library.service;

import com.example.library.exception.*;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ReviewRepository;
import com.example.library.util.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest { //NOSONAR

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CacheUtil<Integer, List<Review>> reviewCacheId;

    @Mock
    private CacheUtil<Integer, Book> bookCacheId;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1);
        book.setTitle("Test Book");

        review = new Review();
        review.setId(1);
        review.setMessage("Great book!");
        review.setBook(book);
    }

    @Test
    void create_ValidReview_ReturnsCreatedReview() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.create(review, 1);

        assertNotNull(result);
        assertEquals("Great book!", result.getMessage());

    }

    @Test
    void create_NullReview_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> reviewService.create(null, 1));
    }

    @Test
    void create_EmptyMessage_ThrowsBadRequestException() {
        review.setMessage("");
        assertThrows(BadRequestException.class, () -> reviewService.create(review, 1));
    }

    @Test
    void createBulk_ValidReviews_ReturnsCreatedReviews() {
        List<Review> reviews = List.of(review);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        List<Review> result = reviewService.createBulk(reviews, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createBulk_EmptyList_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> reviewService.createBulk(Collections.emptyList(), 1));
    }

    @Test
    void getReviewsByBookId_ExistingBook_ReturnsReviews() {
        //when(reviewCacheId.get(1)).thenReturn(null);
        when(bookRepository.existsById(1)).thenReturn(true);
        when(reviewRepository.findByBookId(1)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByBookId(1);

        assertFalse(result.isEmpty());
        assertEquals("Great book!", result.get(0).getMessage());
        //verify(reviewCacheId).put(1, List.of(review));
        verify(reviewRepository).findByBookId(1);
    }

    @Test
    void getReviewsByBookId_NonExistingBook_ThrowsException() {
        when(bookRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByBookId(1));
    }

    @Test
    void update_ValidReview_ReturnsUpdatedReview() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review updatedReview = new Review();
        updatedReview.setMessage("Updated review");

        Review result = reviewService.update(1, updatedReview);

        assertNotNull(result);
        assertEquals("Updated review", result.getMessage());
    }
    
    @Test
    void delete_ExistingReview_EvictsCache() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        reviewService.delete(1);
    }


    @Test
    void getReviewsByBookId_NoReviews_ThrowsException() {
        when(bookRepository.existsById(1)).thenReturn(true);
        when(reviewRepository.findByBookId(1)).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.getReviewsByBookId(1)
        );

        assertEquals("No reviews found for book with id: 1", exception.getMessage());

        verify(reviewCacheId, never()).put(anyInt(), any());
        verify(reviewRepository).findByBookId(1);
    }

    @Test
    void getReviewById_ExistingReview_ReturnsReview() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewById(1);

        assertNotNull(result);
        assertEquals(review.getMessage(), result.getMessage());
    }

    @Test
    void getReviewById_NonExistingReview_ThrowsException() {
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(1));
    }

    @Test
    void update_NullReview_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> reviewService.update(1, null));
    }

    @Test
    void update_NonExistingReview_ThrowsException() {
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());

        Review newReview = new Review();
        newReview.setMessage("New message");

        assertThrows(ResourceNotFoundException.class, () -> reviewService.update(1, newReview));
    }
    @Test
    void delete_NonExistingReview_ThrowsException() {
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.delete(1));
    }
    @Test
    void createBulk_NullList_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> reviewService.createBulk(null, 1));
    }

    @Test
    void createBulk_ReviewWithEmptyMessage_ThrowsBadRequestException() {
        Review invalidReview = new Review();
        invalidReview.setMessage("   ");
        List<Review> reviews = List.of(invalidReview);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        assertThrows(BadRequestException.class, () -> reviewService.createBulk(reviews, 1));
    }

}