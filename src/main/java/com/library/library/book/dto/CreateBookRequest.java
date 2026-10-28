package com.library.library.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String description,
        @Positive int pages,
        @NotNull LocalDateTime publishedAt,
        @NotNull Set<Long> authorIds,
        @NotNull Set<Long> genreIds
) {}
