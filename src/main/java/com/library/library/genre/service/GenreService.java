package com.library.library.genre.service;

import com.library.library.genre.dto.CreateGenreRequest;
import com.library.library.genre.dto.GenreResponse;
import com.library.library.genre.model.Genre;
import com.library.library.genre.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional
    public GenreResponse saveGenre(CreateGenreRequest request) {
        Genre genre = new Genre();
        genre.setName(request.name());
        return GenreResponse.from(genreRepository.save(genre));
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> findAllGenre() {
        return genreRepository.findAll().stream().map(GenreResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<GenreResponse> findGenreById(Long id) {
        return genreRepository.findById(id).map(GenreResponse::from);
    }

}
