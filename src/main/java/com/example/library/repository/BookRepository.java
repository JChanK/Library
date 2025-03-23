package com.example.library.repository;

import com.example.library.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    Book findByTitle(String title);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.reviews r"
            + " WHERE LOWER(r.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> findBooksByReviewMessageContaining(@Param("keyword") String keyword);

    @Query(value = "SELECT b.* FROM book b "
            + "JOIN book_author ba ON b.id = ba.book_id "
            + "JOIN author a ON ba.author_id = a.id "
            + "WHERE LOWER(a.name) = LOWER(:authorName) AND"
            + " LOWER(a.surname) = LOWER(:authorSurname)", nativeQuery = true)
    List<Book> findBooksByAuthorNameAndSurnameNative(
            @Param("authorName") String authorName,
            @Param("authorSurname") String authorSurname);
}