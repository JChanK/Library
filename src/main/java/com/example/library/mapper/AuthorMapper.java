package com.example.library.mapper;

import com.example.library.dto.AuthorDto;
import com.example.library.model.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    private AuthorMapper() {
        // Private constructor to prevent instantiation
    }

    public static AuthorDto toDto(Author author) {
        AuthorDto authorDto = new AuthorDto();
        authorDto.setId(author.getId());
        authorDto.setName(author.getName());
        authorDto.setSurname(author.getSurname());
        return authorDto;
    }

    public static Author toEntity(AuthorDto authorDto) {
        Author author = new Author();
        author.setId(authorDto.getId());
        author.setName(authorDto.getName());
        author.setSurname(authorDto.getSurname());
        return author;
    }
}