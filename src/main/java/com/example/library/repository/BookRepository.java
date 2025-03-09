package com.example.library.repository;

import com.example.library.model.Author;
import com.example.library.model.Book;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    Book findByTitle(String title);

    List<Book> findByTitleAndAuthors(String title, Set<Author> authors);
}