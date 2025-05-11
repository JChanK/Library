package com.example.library.config;

import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.Review;
import com.example.library.util.CacheUtil;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheUtil<Integer, Book> bookCacheId() {
        return new CacheUtil<>(10);
    }

    @Bean
    public CacheUtil<Integer, Author> authorCacheId() {
        return new CacheUtil<>(10);
    }

    @Bean
    public CacheUtil<Integer, List<Review>> reviewCacheId() {
        return new CacheUtil<>(5);
    }
}