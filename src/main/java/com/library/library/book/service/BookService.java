package com.library.library.book.service;

import com.library.library.author.model.Author;
import com.library.library.author.repository.AuthorRepository;
import com.library.library.book.dto.BookResponse;
import com.library.library.book.dto.CreateBookRequest;
import com.library.library.book.dto.ImportBookRequest;
import com.library.library.book.model.Book;
import com.library.library.book.model.NovelStatus;
import com.library.library.book.repository.BookRepository;
import com.library.library.genre.model.Genre;
import com.library.library.genre.repository.GenreRepository;
import com.library.library.image.repository.ImageRepository;
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
    private final ImageRepository imageRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       GenreRepository genreRepository,
                       ImageRepository imageRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.imageRepository = imageRepository;
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

    @Transactional
    public Long importBook(ImportBookRequest request) {
        // Se reutiliza el libro si ya se importó antes, o si se creó a mano con el mismo título
        Book book = bookRepository.findBySourceUrl(request.sourceUrl())
                .or(() -> bookRepository.findByTitle(request.title()).filter(existing -> existing.getSourceUrl() == null))
                .orElseGet(Book::new);

        book.setTitle(request.title());
        if ((request.description() != null && !request.description().isBlank()) || book.getDescription() == null) {
            book.setDescription(request.description() == null ? "" : request.description());
        }
        book.setStatus(request.status() == null ? NovelStatus.UNKNOWN : request.status());
        if (request.totalChapters() != null) {
            book.setTotalChapters(request.totalChapters());
        }
        book.setSourceUrl(request.sourceUrl());
        if (request.coverImageId() != null) {
            book.setCoverImage(imageRepository.getReferenceById(request.coverImageId()));
        }
        for (String alias : request.authors()) {
            book.getAuthors().add(authorRepository.findFirstByAliasIgnoreCase(alias).orElseGet(() -> {
                Author author = new Author();
                author.setAlias(alias);
                return authorRepository.save(author);
            }));
        }
        for (String name : request.genres()) {
            book.getGenres().add(genreRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                Genre genre = new Genre();
                genre.setName(name);
                return genreRepository.save(genre);
            }));
        }
        return bookRepository.save(book).getId();
    }

    @Transactional
    public void setTotalChaptersIfMissing(Long bookId, int totalChapters) {
        Book book = bookRepository.getReferenceById(bookId);
        if (book.getTotalChapters() == null) {
            book.setTotalChapters(totalChapters);
        }
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
