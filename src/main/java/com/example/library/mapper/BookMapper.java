package com.example.library.mapper;

import com.example.library.dto.BookDto;
import com.example.library.model.Book;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    private final AuthorMapper authorMapper;
    private final ReviewMapper reviewMapper;

    @Autowired
    public BookMapper(AuthorMapper authorMapper, ReviewMapper reviewMapper) {
        this.authorMapper = authorMapper;
        this.reviewMapper = reviewMapper;
    }

    public BookDto toDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());

        if (book.getAuthors() != null) {
            bookDto.setAuthors(
                    book.getAuthors().stream()
                            .map(authorMapper::toDto)
                            .collect(Collectors.toList())
            );
        }

        if (book.getReviews() != null) {
            bookDto.setReviews(
                    book.getReviews().stream()
                            .map(reviewMapper::toDto)
                            .collect(Collectors.toList())
            );
        }

        return bookDto;
    }

    public Book toEntity(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());

        if (bookDto.getAuthors() != null) {
            book.setAuthors(
                    bookDto.getAuthors().stream()
                            .map(authorMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }
        if (bookDto.getReviews() != null) {
            book.setReviews(
                    bookDto.getReviews().stream()
                            .map(reviewMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }
        return book;
    }
}