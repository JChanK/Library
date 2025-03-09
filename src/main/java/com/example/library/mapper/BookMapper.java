package com.example.library.mapper;

import com.example.library.dto.AuthorDto;
import com.example.library.dto.BookDto;
import com.example.library.dto.ReviewDto;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.Review;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    // Преобразование Book в BookDto
    public static BookDto toDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());

        // Преобразование авторов в AuthorDTO
        if (book.getAuthors() != null) {
            Set<AuthorDto> authorDto = book.getAuthors().stream()
                    .map(AuthorMapper::toDto)
                    .collect(Collectors.toSet());
            bookDto.setAuthors(authorDto);
        }

        if (book.getReviews() != null) {
            Set<ReviewDto> reviewDto = book.getReviews().stream()
                    .map(ReviewMapper::toDto)
                    .collect(Collectors.toSet());
            bookDto.setReviews(reviewDto);
        }

        return bookDto;
    }

    // Преобразование BookDto в Book
    public static Book toEntity(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());

        // Преобразование авторов из AuthorDTO в Author
        if (bookDto.getAuthors() != null) {
            Set<Author> authors = bookDto.getAuthors().stream()
                    .map(AuthorMapper::toEntity)
                    .collect(Collectors.toSet());
            book.setAuthors(authors);
        }

        if (bookDto.getReviews() != null) {
            Set<Review> reviews = bookDto.getReviews().stream()
                    .map(ReviewMapper::toEntity)
                    .collect(Collectors.toSet());
            book.setReviews(reviews);
        }
        return book;
    }
}