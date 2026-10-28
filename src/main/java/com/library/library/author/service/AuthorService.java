package com.library.library.author.service;

import com.library.library.author.dto.AuthorResponse;
import com.library.library.author.dto.CreateAuthorRequest;
import com.library.library.author.model.Author;
import com.library.library.author.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public AuthorResponse saveAuthor(CreateAuthorRequest request) {
        Author author = new Author();
        author.setAlias(request.alias());
        return AuthorResponse.from(authorRepository.save(author));
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAllAuthors() {
        return authorRepository.findAll().stream().map(AuthorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<AuthorResponse> findAuthorById(Long id) {
        return authorRepository.findById(id).map(AuthorResponse::from);
    }
}
