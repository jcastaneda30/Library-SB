package com.library.library.genre.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGenreRequest(@NotBlank String name) {}
