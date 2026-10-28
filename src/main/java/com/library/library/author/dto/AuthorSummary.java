package com.library.library.author.dto;

import com.library.library.author.model.Author;

public record AuthorSummary(Long id, String alias) {
    public static AuthorSummary from(Author author) {
        return new AuthorSummary(author.getId(), author.getAlias());
    }
}
