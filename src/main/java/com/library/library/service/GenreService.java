package com.library.library.service;

import com.library.library.model.Genre;

import com.library.library.repository.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre saveGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public List<Genre> findAllGenre() {
        return genreRepository.findAll();
    }

    public Optional<Genre> findGenreById(Long id) {
        return genreRepository.findById(id);
    }

}
