package com.library.library.author.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(@NotBlank String alias) {}
