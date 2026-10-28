package com.library.library.genre.dto;

import com.library.library.book.dto.BookSummary;
import com.library.library.genre.model.Genre;


import java.util.List;

public record GenreResponse(Long id, String name, List<BookSummary> books) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getBooks().stream().map(BookSummary::from).toList()
        );
    }
}
