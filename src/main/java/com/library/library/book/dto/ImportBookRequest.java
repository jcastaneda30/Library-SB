package com.library.library.book.dto;

import com.library.library.book.model.NovelStatus;

import java.util.List;

// Libro que llega de una fuente externa ya procesado; sourceUrl lo identifica al volver a importarlo
public record ImportBookRequest(
        String sourceUrl,
        String title,
        String description,
        NovelStatus status,
        Integer totalChapters,
        Long coverImageId,
        List<String> authors,
        List<String> genres
) {}
