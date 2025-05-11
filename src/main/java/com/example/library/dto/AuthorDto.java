package com.example.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Author DTO")
public class AuthorDto {
    @Schema(description = "ID автора", example = "1")
    private int id;

    @NotBlank
    @Schema(description = "Имя автора", example = "Лев")
    private String name;

    @NotBlank
    @Schema(description = "Фамилия автора", example = "Толстой")
    private String surname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}