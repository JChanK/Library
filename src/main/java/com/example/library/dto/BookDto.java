package com.example.library.dto;

import java.util.Set;

public class BookDto {
    private int id;
    private String title;
    private Set<AuthorDto> authors;
    private Set<ReviewDto> reviews;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<AuthorDto> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<AuthorDto> authors) {
        this.authors = authors;
    }

    public Set<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(Set<ReviewDto> reviews) {
        this.reviews = reviews;
    }
}