package com.library.scraper.novel.service;

import com.library.library.book.dto.ImportBookRequest;
import com.library.library.book.model.Book;
import com.library.library.book.service.BookService;
import com.library.library.chapter.dto.ImportChapterRequest;
import com.library.library.chapter.dto.ImportParagraph;
import com.library.library.chapter.model.ParagraphType;
import com.library.library.chapter.service.ChapterService;
import com.library.scraper.engine.ExtractedParagraph;
import com.library.scraper.engine.ScrapedChapter;
import com.library.scraper.engine.ScrapedNovel;
import com.library.scraper.engine.StatusParser;
import com.library.scraper.novel.dto.NovelRequestResponse;
import com.library.scraper.novel.model.NovelRequest;
import com.library.scraper.novel.model.NovelRequestStatus;
import com.library.scraper.novel.repository.NovelRequestRepository;
import com.library.scraper.site.repository.ScraperSiteRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Estado de las peticiones de novela y entrega de resultados a la biblioteca. Lo que se guarda en libros
 * y capítulos pasa por los servicios de library; aquí solo se traduce el resultado del scraping.
 * Cada método es una transacción corta para que lo ya importado quede guardado aunque falle un capítulo.
 */
@Service
public class NovelImporter {

    public record RequestSnapshot(Long siteId, String url, boolean includeChapters, Integer fromChapter, Integer toChapter) {}

    private static final Set<NovelRequestStatus> IN_PROGRESS = EnumSet.of(NovelRequestStatus.PENDING, NovelRequestStatus.RUNNING);

    private final NovelRequestRepository novelRequestRepository;
    private final ScraperSiteRepository scraperSiteRepository;
    private final BookService bookService;
    private final ChapterService chapterService;
    private final EntityManager entityManager;

    public NovelImporter(NovelRequestRepository novelRequestRepository,
                         ScraperSiteRepository scraperSiteRepository,
                         BookService bookService,
                         ChapterService chapterService,
                         EntityManager entityManager) {
        this.novelRequestRepository = novelRequestRepository;
        this.scraperSiteRepository = scraperSiteRepository;
        this.bookService = bookService;
        this.chapterService = chapterService;
        this.entityManager = entityManager;
    }

    // Crea la petición, o reutiliza la existente para esa URL, y la deja lista para procesarse
    @Transactional
    public NovelRequestResponse prepare(Long siteId, String url, boolean includeChapters, Integer fromChapter, Integer toChapter) {
        NovelRequest request = novelRequestRepository.findByUrl(url).orElseGet(() -> {
            NovelRequest created = new NovelRequest();
            created.setUrl(url);
            return created;
        });
        if (request.getId() != null && IN_PROGRESS.contains(request.getStatus())) {
            throw new IllegalArgumentException("La novela " + url + " ya se está procesando");
        }
        request.setSite(scraperSiteRepository.getReferenceById(siteId));
        request.setIncludeChapters(includeChapters);
        request.setFromChapter(fromChapter);
        request.setToChapter(toChapter);
        request.setStatus(NovelRequestStatus.PENDING);
        request.setTotalChapters(null);
        request.setProcessedChapters(0);
        request.setError(null);
        request.setStartedAt(null);
        request.setFinishedAt(null);
        return NovelRequestResponse.from(novelRequestRepository.save(request));
    }

    @Transactional
    public RequestSnapshot start(Long requestId) {
        NovelRequest request = novelRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(NovelRequestStatus.RUNNING);
        request.setStartedAt(LocalDateTime.now());
        return new RequestSnapshot(request.getSite().getId(), request.getUrl(), request.isIncludeChapters(),
                request.getFromChapter(), request.getToChapter());
    }

    @Transactional
    public Long saveBook(Long requestId, String sourceUrl, ScrapedNovel novel, Long coverImageId) {
        Long bookId = bookService.importBook(new ImportBookRequest(
                sourceUrl,
                novel.title(),
                novel.synopsis(),
                StatusParser.parse(novel.status()),
                novel.chapterCount(),
                coverImageId,
                novel.authors(),
                novel.genres()));
        novelRequestRepository.findById(requestId).orElseThrow().setBook(entityManager.getReference(Book.class, bookId));
        return bookId;
    }

    // Si el sitio no muestra el total, se usa el número de capítulos encontrados
    public void updateBookChapterCount(Long bookId, int totalChapters) {
        bookService.setTotalChaptersIfMissing(bookId, totalChapters);
    }

    public Set<Integer> existingChapterNumbers(Long bookId) {
        return chapterService.findChapterNumbers(bookId);
    }

    public void saveChapter(Long bookId, int number, ScrapedChapter scraped, Map<String, Long> imageIdsByUrl) {
        List<ImportParagraph> paragraphs = new ArrayList<>();
        for (ExtractedParagraph extracted : scraped.paragraphs()) {
            if (extracted.type() == ParagraphType.IMAGE) {
                Long imageId = imageIdsByUrl.get(extracted.imageUrl());
                // Las imágenes que no se pudieron descargar se omiten
                if (imageId != null) {
                    paragraphs.add(new ImportParagraph(ParagraphType.IMAGE, null, imageId));
                }
            } else {
                paragraphs.add(new ImportParagraph(ParagraphType.TEXT, extracted.content(), null));
            }
        }
        chapterService.importChapter(new ImportChapterRequest(bookId, number, scraped.title(), scraped.url(), paragraphs));
    }

    @Transactional
    public void updateTotal(Long requestId, int totalChapters) {
        novelRequestRepository.findById(requestId).orElseThrow().setTotalChapters(totalChapters);
    }

    @Transactional
    public void updateProgress(Long requestId, int processedChapters) {
        novelRequestRepository.findById(requestId).orElseThrow().setProcessedChapters(processedChapters);
    }

    @Transactional
    public void complete(Long requestId) {
        finish(requestId, NovelRequestStatus.COMPLETED, null);
    }

    @Transactional
    public void fail(Long requestId, String error) {
        finish(requestId, NovelRequestStatus.FAILED, error);
    }

    private void finish(Long requestId, NovelRequestStatus status, String error) {
        NovelRequest request = novelRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(status);
        request.setError(error);
        request.setFinishedAt(LocalDateTime.now());
    }
}
