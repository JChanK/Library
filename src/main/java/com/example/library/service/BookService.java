package com.example.library.service;

import com.example.library.model.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private static final HashMap<Integer, Book> Book_REPOSITORY_MAP = new HashMap<>();
    private static final AtomicInteger Book_ID_HOLDER = new AtomicInteger();

    public void create(Book book) {
        final int bookId = Book_ID_HOLDER.getAndIncrement();
        if (bookId < 0) {
            throw new IllegalStateException("ID overflow");
        }
        book.setId(bookId);
        if (book.getAuthor() != null && book.getAuthor().getId() == 0) {
            book.getAuthor().setId(bookId + 100);
        }
        Book_REPOSITORY_MAP.put(bookId, book);
    }

    public BookService() {
        create(new Book("Гарри Поттер",  "Дж. К.", "Роулинг"));
        create(new Book("Война и мир", "Лев", "Толстой"));
        create(new Book("Преступление и наказание",  "Фёдор", "Достоевский"));
        create(new Book("Мастер и Маргарита", "Михаил", "Булгаков"));
        create(new Book("Анна Каренина",  "Лев", "Толстой"));
    }

    public List<Book> readAll() {
        return new ArrayList<>(Book_REPOSITORY_MAP.values());
    }

    public Book findById(int id) {
        return Book_REPOSITORY_MAP.get(id);
    }

    public Book findByTitle(String title) {
        return Book_REPOSITORY_MAP.values().stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    public boolean update(Book book, int id) {
        if (Book_REPOSITORY_MAP.containsKey(id)) {
            book.setId(id);
            Book_REPOSITORY_MAP.put(id, book);
            return true;
        }
        return false;
    }

    public boolean delete(int id) {
        return Book_REPOSITORY_MAP.remove(id) != null;
    }
}
