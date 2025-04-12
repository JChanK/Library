package com.example.library.exception;

public class InvalidProperNameException extends BadRequestException {
    public InvalidProperNameException(String cityName) {
        super("Proper name '"
                + cityName
                + "' must start with capital letter and contain only letters");
    }
}