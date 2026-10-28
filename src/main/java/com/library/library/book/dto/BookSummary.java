package com.library.library.book.dto;

import com.library.library.book.model.Book;

public record BookSummary(Long id, String title) {
    public static BookSummary from(Book book) {
        return new BookSummary(book.getId(), book.getTitle());
    }
}
