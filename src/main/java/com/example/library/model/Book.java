package com.example.library.model;

public class Book {
    private String title;
    private int id;
    Author author;

    public Book(String title, String authorName, String authorSurname) {
        this.title = title;
        author = new Author(authorName, authorSurname);
    }

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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
