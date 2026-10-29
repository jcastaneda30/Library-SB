package com.library.scraper.site.controller;

import com.library.common.config.OpenApiConfig;
import com.library.scraper.site.dto.PreviewRequest;
import com.library.scraper.site.dto.PreviewResponse;
import com.library.scraper.site.dto.SaveScraperSiteRequest;
import com.library.scraper.site.dto.ScraperSiteResponse;
import com.library.scraper.site.service.ScraperSiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/scraper/sites")
@Tag(name = OpenApiConfig.SCRAPER_SITES)
public class ScraperSiteController {
    private final ScraperSiteService scraperSiteService;
    public ScraperSiteController(ScraperSiteService scraperSiteService) {
        this.scraperSiteService = scraperSiteService;
    }

    @Operation(summary = "Listar sitios")
    @GetMapping
    public List<ScraperSiteResponse> getAllSites() {
        return scraperSiteService.findAllSites();
    }

    @Operation(summary = "Obtener un sitio")
    @GetMapping("/{id}")
    public ResponseEntity<ScraperSiteResponse> getSiteById(@PathVariable Long id) {
        return scraperSiteService.findSiteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registrar un sitio", description = "La URL base se normaliza (`https://www.Sitio.com` → `https://sitio.com/`) y debe ser única.")
    @PostMapping
    public ResponseEntity<ScraperSiteResponse> createSite(@Valid @RequestBody SaveScraperSiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scraperSiteService.saveSite(request));
    }

    @Operation(summary = "Reemplazar un sitio", description = "Las reglas enviadas sustituyen a todas las anteriores.")
    @PutMapping("/{id}")
    public ResponseEntity<ScraperSiteResponse> updateSite(@PathVariable Long id,
                                                          @Valid @RequestBody SaveScraperSiteRequest request) {
        return scraperSiteService.updateSite(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Probar la configuración", description = "Extrae la ficha y el primer capítulo de una novela real sin guardar nada ni descargar imágenes. Si falla la parte de capítulos, la ficha se devuelve igual y `chapterError` explica el motivo.")
    @PostMapping("/{id}/preview")
    public ResponseEntity<PreviewResponse> preview(@PathVariable Long id, @Valid @RequestBody PreviewRequest request) {
        return scraperSiteService.preview(id, request.url())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
