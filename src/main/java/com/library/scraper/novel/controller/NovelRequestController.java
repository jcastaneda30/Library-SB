package com.library.scraper.novel.controller;

import com.library.common.config.OpenApiConfig;
import com.library.scraper.novel.dto.CreateNovelRequest;
import com.library.scraper.novel.dto.NovelRequestResponse;
import com.library.scraper.novel.service.NovelRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/scraper/novels")
@Tag(name = OpenApiConfig.SCRAPER_NOVELS)
public class NovelRequestController {
    private final NovelRequestService novelRequestService;
    public NovelRequestController(NovelRequestService novelRequestService) {
        this.novelRequestService = novelRequestService;
    }

    @Operation(summary = "Listar peticiones de novela", description = "Las más recientes primero.")
    @GetMapping
    public List<NovelRequestResponse> getAllNovelRequests() {
        return novelRequestService.findAllNovelRequests();
    }

    @Operation(summary = "Estado de una petición", description = "Consultar hasta que `status` sea `COMPLETED` o `FAILED`; el libro queda en `bookId`.")
    @GetMapping("/{id}")
    public ResponseEntity<NovelRequestResponse> getNovelRequestById(@PathVariable Long id) {
        return novelRequestService.findNovelRequestById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 202: la extracción corre en segundo plano; el progreso se consulta con GET /{id}
    @Operation(summary = "Pedir una novela", description = "Se procesa en segundo plano con el sitio cuya URL base contiene la URL. Hay una petición por novela: pedirla de nuevo la reutiliza y solo trae los capítulos que falten.")
    @PostMapping
    public ResponseEntity<NovelRequestResponse> requestNovel(@Valid @RequestBody CreateNovelRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(novelRequestService.requestNovel(request));
    }

    @Operation(summary = "Volver a procesar una petición", description = "Mismos parámetros que la última vez; sirve para traer capítulos nuevos o reintentar una que falló.")
    @PostMapping("/{id}/refresh")
    public ResponseEntity<NovelRequestResponse> refresh(@PathVariable Long id) {
        return novelRequestService.refresh(id)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
