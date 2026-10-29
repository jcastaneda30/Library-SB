package com.library.scraper.novel.service;

import com.library.library.chapter.model.ParagraphType;
import com.library.library.image.service.ImageStorageService;
import com.library.scraper.engine.ExtractedParagraph;
import com.library.scraper.engine.FetchResult;
import com.library.scraper.engine.NovelScraper;
import com.library.scraper.engine.PageFetcher;
import com.library.scraper.engine.ScrapedChapter;
import com.library.scraper.site.model.ScraperSite;
import com.library.scraper.site.repository.ScraperSiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Procesa una NovelRequest en segundo plano con las reglas de su sitio. No es transaccional: cada escritura va por NovelImporter
@Service
public class NovelScrapeRunner {

    private static final Logger log = LoggerFactory.getLogger(NovelScrapeRunner.class);

    private final NovelImporter novelImporter;
    private final ScraperSiteRepository scraperSiteRepository;
    private final NovelScraper novelScraper;
    private final PageFetcher pageFetcher;
    private final ImageStorageService imageStorageService;

    public NovelScrapeRunner(NovelImporter novelImporter,
                             ScraperSiteRepository scraperSiteRepository,
                             NovelScraper novelScraper,
                             PageFetcher pageFetcher,
                             ImageStorageService imageStorageService) {
        this.novelImporter = novelImporter;
        this.scraperSiteRepository = scraperSiteRepository;
        this.novelScraper = novelScraper;
        this.pageFetcher = pageFetcher;
        this.imageStorageService = imageStorageService;
    }

    @Async("scraperExecutor")
    public void run(Long requestId) {
        try {
            NovelImporter.RequestSnapshot request = novelImporter.start(requestId);
            ScraperSite site = scraperSiteRepository.findById(request.siteId()).orElseThrow();

            NovelScraper.NovelScrape scrape = novelScraper.scrapeNovel(site, request.url());
            Long coverId = downloadImage(site, scrape.novel().coverUrl());
            Long bookId = novelImporter.saveBook(requestId, request.url(), scrape.novel(), coverId);

            if (request.includeChapters()) {
                importChapters(requestId, request, site, scrape, bookId);
            }
            novelImporter.complete(requestId);
        } catch (Exception e) {
            log.error("Falló la extracción de la petición de novela {}", requestId, e);
            novelImporter.fail(requestId, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    private void importChapters(Long requestId, NovelImporter.RequestSnapshot request, ScraperSite site,
                                NovelScraper.NovelScrape scrape, Long bookId) {
        List<String> chapterUrls = novelScraper.locateChapters(site, request.url(), scrape);
        novelImporter.updateBookChapterCount(bookId, chapterUrls.size());

        int from = request.fromChapter() == null ? 1 : request.fromChapter();
        int to = request.toChapter() == null ? chapterUrls.size() : Math.min(request.toChapter(), chapterUrls.size());
        novelImporter.updateTotal(requestId, Math.max(0, to - from + 1));

        // Los capítulos ya guardados se saltan, así volver a pedir la novela solo trae lo que falta
        Set<Integer> existing = novelImporter.existingChapterNumbers(bookId);
        int processed = 0;
        for (int number = from; number <= to; number++) {
            if (!existing.contains(number)) {
                ScrapedChapter chapter = novelScraper.scrapeChapter(site, chapterUrls.get(number - 1));
                novelImporter.saveChapter(bookId, number, chapter, downloadImages(site, chapter));
            }
            novelImporter.updateProgress(requestId, ++processed);
        }
    }

    private Map<String, Long> downloadImages(ScraperSite site, ScrapedChapter chapter) {
        Map<String, Long> imageIds = new HashMap<>();
        for (ExtractedParagraph paragraph : chapter.paragraphs()) {
            if (paragraph.type() == ParagraphType.IMAGE && !imageIds.containsKey(paragraph.imageUrl())) {
                Long id = downloadImage(site, paragraph.imageUrl());
                if (id != null) {
                    imageIds.put(paragraph.imageUrl(), id);
                }
            }
        }
        return imageIds;
    }

    // Una imagen que falla no debe tumbar todo el capítulo
    private Long downloadImage(ScraperSite site, String url) {
        if (url == null) {
            return null;
        }
        try {
            FetchResult image = url.startsWith("data:") ? decodeDataUri(url) : pageFetcher.fetch(url, site);
            return imageStorageService.store(image.body(), image.contentType(), url.startsWith("data:") ? null : url).getId();
        } catch (Exception e) {
            log.warn("No se pudo guardar la imagen {}: {}", abbreviate(url), e.getMessage());
            return null;
        }
    }

    // data:image/png;base64,....
    private static FetchResult decodeDataUri(String uri) {
        int comma = uri.indexOf(',');
        String header = uri.substring("data:".length(), comma);
        if (!header.endsWith(";base64")) {
            throw new IllegalArgumentException("Solo se admiten data URI en base64");
        }
        String contentType = header.substring(0, header.length() - ";base64".length());
        return new FetchResult(null, Base64.getDecoder().decode(uri.substring(comma + 1)), contentType);
    }

    private static String abbreviate(String url) {
        return url.length() > 120 ? url.substring(0, 120) + "..." : url;
    }
}
