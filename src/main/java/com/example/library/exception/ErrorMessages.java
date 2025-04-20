package com.example.library.exception;

public final class ErrorMessages {
    public static final String ENTITY_NOT_FOUND = "%s not found with %s: %s";
    public static final String FIELD_CANNOT_BE_NULL_OR_EMPTY = "%s cannot be null or empty";
    public static final String ENTITY_CANNOT_BE_NULL = "%s cannot be null";

    public static final String AUTHOR_NOT_FOUND =
            String.format(ENTITY_NOT_FOUND, "Author", "id", "%s");
    public static final String AUTHOR_NAME_EMPTY =
            String.format(FIELD_CANNOT_BE_NULL_OR_EMPTY, "Author name");
    public static final String AUTHOR_SURNAME_EMPTY =
            String.format(FIELD_CANNOT_BE_NULL_OR_EMPTY, "Author surname");
    public static final String AUTHOR_ALREADY_ASSOCIATED =
            "Author is already associated with this book";

    public static final String BOOK_NOT_FOUND =
            String.format(ENTITY_NOT_FOUND, "Book", "id", "%s");
    public static final String BOOK_TITLE_EMPTY =
            String.format(FIELD_CANNOT_BE_NULL_OR_EMPTY, "Book title");

    public static final String REVIEW_NOT_FOUND =
            String
            .format(ENTITY_NOT_FOUND, "Review", "id", "%s");
    public static final String REVIEW_MESSAGE_EMPTY =
            String.format(FIELD_CANNOT_BE_NULL_OR_EMPTY, "Review message");
    public static final String REVIEWS_NOT_FOUND_WITH_KEYWORD =
            "No books found with reviews containing the message: %s";

    public static final String AUTHOR_NAME_INVALID =
            "Author name must start with capital letter and contain"
                    + " only letters, spaces or hyphens";
    public static final String AUTHOR_SURNAME_INVALID =
            "Author surname must start with capital letter and contain"
                    + " only letters, spaces or hyphens";

    public static final String LIST_CANNOT_BE_NULL_OR_EMPTY = "%s cannot be null or empty";
    public static final String BOOK_AUTHORS_EMPTY = "is empty";

    private ErrorMessages() {}
}