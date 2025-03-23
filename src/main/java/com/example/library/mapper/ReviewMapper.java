package com.example.library.mapper;

import com.example.library.dto.ReviewDto;
import com.example.library.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setId(review.getId());
        reviewDto.setMessage(review.getMessage());
        return reviewDto;
    }

    public Review toEntity(ReviewDto reviewDto) {
        Review review = new Review();
        review.setId(reviewDto.getId());
        review.setMessage(reviewDto.getMessage());
        return review;
    }
}