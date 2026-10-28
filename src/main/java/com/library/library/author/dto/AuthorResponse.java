package com.library.library.author.dto;

import com.library.library.author.model.Author;
import com.library.library.book.dto.BookSummary;


import java.util.List;

public record AuthorResponse(Long id, String alias, List<BookSummary> books) {
    public static AuthorResponse from(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getAlias(),
                author.getBooks().stream().map(BookSummary::from).toList()
        );
    }
}
