package com.library.scraper.novel.service;

import com.library.scraper.novel.dto.CreateNovelRequest;
import com.library.scraper.novel.dto.NovelRequestResponse;
import com.library.scraper.novel.model.NovelRequest;
import com.library.scraper.novel.model.NovelRequestStatus;
import com.library.scraper.novel.repository.NovelRequestRepository;
import com.library.scraper.site.model.ScraperSite;
import com.library.scraper.site.service.ScraperSiteService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NovelRequestService {

    private final NovelRequestRepository novelRequestRepository;
    private final ScraperSiteService scraperSiteService;
    private final NovelImporter novelImporter;
    private final NovelScrapeRunner novelScrapeRunner;

    public NovelRequestService(NovelRequestRepository novelRequestRepository,
                               ScraperSiteService scraperSiteService,
                               NovelImporter novelImporter,
                               NovelScrapeRunner novelScrapeRunner) {
        this.novelRequestRepository = novelRequestRepository;
        this.scraperSiteService = scraperSiteService;
        this.novelImporter = novelImporter;
        this.novelScrapeRunner = novelScrapeRunner;
    }

    // Sin transacción: la petición tiene que estar guardada antes de que el hilo en segundo plano la lea
    public NovelRequestResponse requestNovel(CreateNovelRequest request) {
        if (request.fromChapter() != null && request.toChapter() != null && request.fromChapter() > request.toChapter()) {
            throw new IllegalArgumentException("fromChapter no puede ser mayor que toChapter");
        }
        String url = normalizeUrl(request.url());
        ScraperSite site = scraperSiteService.resolveSite(url);
        NovelRequestResponse prepared = novelImporter.prepare(
                site.getId(), url, request.includeChapters(), request.fromChapter(), request.toChapter());
        novelScrapeRunner.run(prepared.id());
        return prepared;
    }

    // Vuelve a procesar una novela ya pedida con los mismos parámetros (p. ej. para traer capítulos nuevos)
    public Optional<NovelRequestResponse> refresh(Long id) {
        return findNovelRequestById(id).map(existing -> requestNovel(new CreateNovelRequest(
                existing.url(), existing.includeChapters(), existing.fromChapter(), existing.toChapter())));
    }

    @Transactional(readOnly = true)
    public List<NovelRequestResponse> findAllNovelRequests() {
        return novelRequestRepository.findAllByOrderByIdDesc().stream().map(NovelRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<NovelRequestResponse> findNovelRequestById(Long id) {
        return novelRequestRepository.findById(id).map(NovelRequestResponse::from);
    }

    // Las peticiones que se estaban procesando cuando se apagó la aplicación ya no van a terminar
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void failInterruptedRequests() {
        for (NovelRequest request : novelRequestRepository.findByStatusIn(List.of(NovelRequestStatus.PENDING, NovelRequestStatus.RUNNING))) {
            request.setStatus(NovelRequestStatus.FAILED);
            request.setError("Interrumpida por un reinicio de la aplicación; vuelve a pedirla para continuar");
            request.setFinishedAt(LocalDateTime.now());
        }
    }

    // Misma novela, misma URL: sin espacios, fragmento ni "/" final
    static String normalizeUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL inválida: " + url);
        }
        String result = uri.getFragment() == null ? uri.toString() : uri.toString().substring(0, uri.toString().indexOf('#'));
        return uri.getRawQuery() == null && result.endsWith("/") ? result.substring(0, result.length() - 1) : result;
    }
}
