package com.library.library.genre.dto;

import com.library.library.genre.model.Genre;

public record GenreSummary(Long id, String name) {
    public static GenreSummary from(Genre genre) {
        return new GenreSummary(genre.getId(), genre.getName());
    }
}
