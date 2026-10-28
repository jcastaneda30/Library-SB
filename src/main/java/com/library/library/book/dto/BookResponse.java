package com.library.library.book.dto;

import com.library.library.author.dto.AuthorSummary;
import com.library.library.book.model.Book;
import com.library.library.genre.dto.GenreSummary;


import java.time.LocalDateTime;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String description,
        int pages,
        LocalDateTime publishedAt,
        List<AuthorSummary> authors,
        List<GenreSummary> genres
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPages(),
                book.getPublishedAt(),
                book.getAuthors().stream().map(AuthorSummary::from).toList(),
                book.getGenres().stream().map(GenreSummary::from).toList()
        );
    }
}
