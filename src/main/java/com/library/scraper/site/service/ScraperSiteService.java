package com.library.scraper.site.service;

import com.library.scraper.site.dto.PreviewResponse;
import com.library.scraper.site.dto.SaveScraperSiteRequest;
import com.library.scraper.site.dto.ScraperRuleRequest;
import com.library.scraper.site.dto.ScraperSiteResponse;
import com.library.scraper.engine.NovelScraper;
import com.library.scraper.engine.ScrapeException;
import com.library.scraper.engine.ScrapedChapter;
import com.library.scraper.site.model.JavascriptRendering;
import com.library.scraper.site.model.ScraperRule;
import com.library.scraper.site.model.ScraperSite;
import com.library.scraper.site.repository.ScraperSiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class ScraperSiteService {

    private static final int PREVIEW_CHAPTER_URLS = 5;

    private final ScraperSiteRepository scraperSiteRepository;
    private final NovelScraper novelScraper;

    public ScraperSiteService(ScraperSiteRepository scraperSiteRepository, NovelScraper novelScraper) {
        this.scraperSiteRepository = scraperSiteRepository;
        this.novelScraper = novelScraper;
    }

    @Transactional
    public ScraperSiteResponse saveSite(SaveScraperSiteRequest request) {
        String baseUrl = BaseUrl.normalize(request.baseUrl());
        if (scraperSiteRepository.existsByBaseUrl(baseUrl)) {
            throw new IllegalArgumentException("Ya existe un scraper para " + baseUrl);
        }
        ScraperSite site = new ScraperSite();
        apply(site, request, baseUrl);
        return ScraperSiteResponse.from(scraperSiteRepository.save(site));
    }

    @Transactional
    public Optional<ScraperSiteResponse> updateSite(Long id, SaveScraperSiteRequest request) {
        String baseUrl = BaseUrl.normalize(request.baseUrl());
        if (scraperSiteRepository.existsByBaseUrlAndIdNot(baseUrl, id)) {
            throw new IllegalArgumentException("Ya existe un scraper para " + baseUrl);
        }
        return scraperSiteRepository.findById(id).map(site -> {
            apply(site, request, baseUrl);
            return ScraperSiteResponse.from(scraperSiteRepository.save(site));
        });
    }

    @Transactional(readOnly = true)
    public List<ScraperSiteResponse> findAllSites() {
        return scraperSiteRepository.findAll().stream().map(ScraperSiteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ScraperSiteResponse> findSiteById(Long id) {
        return scraperSiteRepository.findById(id).map(ScraperSiteResponse::from);
    }

    // Elige el sitio configurado bajo cuya URL base está la novela pedida (la base más específica gana)
    @Transactional(readOnly = true)
    public ScraperSite resolveSite(String url) {
        BaseUrl.parse(url);
        return scraperSiteRepository.findByEnabledTrue().stream()
                .filter(site -> BaseUrl.contains(site.getBaseUrl(), url))
                .max(Comparator.comparingInt(site -> BaseUrl.specificity(site.getBaseUrl())))
                .orElseThrow(() -> new IllegalArgumentException("No hay un sitio configurado que contenga " + url));
    }

    // Prueba la configuración contra una URL real sin guardar nada ni descargar imágenes.
    // Sin transacción para no retener una conexión de BD mientras se espera al sitio
    public Optional<PreviewResponse> preview(Long id, String url) {
        BaseUrl.parse(url);
        return scraperSiteRepository.findById(id).map(site -> {
            if (!BaseUrl.contains(site.getBaseUrl(), url)) {
                throw new IllegalArgumentException(url + " no está bajo " + site.getBaseUrl());
            }
            NovelScraper.NovelScrape scrape = novelScraper.scrapeNovel(site, url);
            List<String> chapterUrls = List.of();
            try {
                chapterUrls = novelScraper.locateChapters(site, url, scrape);
                ScrapedChapter firstChapter = chapterUrls.isEmpty()
                        ? null
                        : novelScraper.scrapeChapter(site, chapterUrls.get(0));
                return new PreviewResponse(scrape.novel(), chapterUrls.size(), firstUrls(chapterUrls), firstChapter, null);
            } catch (ScrapeException e) {
                // Se conserva lo que sí se encontró para ver en qué paso falla la configuración
                return new PreviewResponse(scrape.novel(), chapterUrls.size(), firstUrls(chapterUrls), null, e.getMessage());
            }
        });
    }

    private static List<String> firstUrls(List<String> chapterUrls) {
        return chapterUrls.subList(0, Math.min(PREVIEW_CHAPTER_URLS, chapterUrls.size()));
    }

    private void apply(ScraperSite site, SaveScraperSiteRequest request, String baseUrl) {
        site.setName(request.name());
        site.setBaseUrl(baseUrl);
        site.setEnabled(request.enabled() == null || request.enabled());
        site.setUserAgent(blankToNull(request.userAgent()));
        site.setRequestDelayMs(request.requestDelayMs() == null ? 1000 : request.requestDelayMs());
        site.setJavascriptRendering(request.javascriptRendering() == null ? JavascriptRendering.NONE : request.javascriptRendering());
        site.setWaitForSelector(blankToNull(request.waitForSelector()));
        site.setChapterListUrlTemplate(blankToNull(request.chapterListUrlTemplate()));
        site.setChapterUrlTemplate(blankToNull(request.chapterUrlTemplate()));
        site.replaceRules(request.rules().stream().map(ScraperSiteService::toRule).toList());
    }

    private static ScraperRule toRule(ScraperRuleRequest request) {
        String pattern = blankToNull(request.pattern());
        if (pattern != null) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Regex inválida en la regla " + request.field() + ": " + e.getDescription());
            }
        }
        ScraperRule rule = new ScraperRule();
        rule.setField(request.field());
        rule.setSelector(request.selector().strip());
        rule.setAttribute(blankToNull(request.attribute()));
        rule.setPattern(pattern);
        return rule;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
