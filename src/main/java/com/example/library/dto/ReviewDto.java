package com.example.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Review DTO")
public class ReviewDto {
    @Schema(description = "ID отзыва", example = "1")
    private int id;

    @Schema(description = "Текст отзыва", example = "Отличная книга!")
    private String message;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}