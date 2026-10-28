package com.library.library.book.service;

import com.library.library.author.model.Author;
import com.library.library.author.repository.AuthorRepository;
import com.library.library.book.dto.BookResponse;
import com.library.library.book.dto.CreateBookRequest;
import com.library.library.book.model.Book;
import com.library.library.book.repository.BookRepository;
import com.library.library.genre.model.Genre;
import com.library.library.genre.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional
    public BookResponse saveBook(CreateBookRequest request) {
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.authorIds()));
        if (authors.size() != request.authorIds().size()) {
            throw new IllegalArgumentException("Uno o más autores no existen");
        }
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.genreIds()));
        if (genres.size() != request.genreIds().size()) {
            throw new IllegalArgumentException("Uno o más géneros no existen");
        }

        Book book = new Book();
        book.setTitle(request.title());
        book.setDescription(request.description());
        book.setPages(request.pages());
        book.setPublishedAt(request.publishedAt());
        book.setAuthors(authors);
        book.setGenres(genres);
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAllBooks() {
        return bookRepository.findAll().stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<BookResponse> findBookById(Long id) {
        return bookRepository.findById(id).map(BookResponse::from);
    }

}
