package com.library.scraper.site.controller;

import com.library.scraper.site.dto.PreviewRequest;
import com.library.scraper.site.dto.PreviewResponse;
import com.library.scraper.site.dto.SaveScraperSiteRequest;
import com.library.scraper.site.dto.ScraperSiteResponse;
import com.library.scraper.site.service.ScraperSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/scraper/sites")
public class ScraperSiteController {
    private final ScraperSiteService scraperSiteService;
    public ScraperSiteController(ScraperSiteService scraperSiteService) {
        this.scraperSiteService = scraperSiteService;
    }

    @GetMapping
    public List<ScraperSiteResponse> getAllSites() {
        return scraperSiteService.findAllSites();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScraperSiteResponse> getSiteById(@PathVariable Long id) {
        return scraperSiteService.findSiteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScraperSiteResponse> createSite(@Valid @RequestBody SaveScraperSiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scraperSiteService.saveSite(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScraperSiteResponse> updateSite(@PathVariable Long id,
                                                          @Valid @RequestBody SaveScraperSiteRequest request) {
        return scraperSiteService.updateSite(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<PreviewResponse> preview(@PathVariable Long id, @Valid @RequestBody PreviewRequest request) {
        return scraperSiteService.preview(id, request.url())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
