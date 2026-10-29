package com.library.scraper.novel.controller;

import com.library.scraper.novel.dto.CreateNovelRequest;
import com.library.scraper.novel.dto.NovelRequestResponse;
import com.library.scraper.novel.service.NovelRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/scraper/novels")
public class NovelRequestController {
    private final NovelRequestService novelRequestService;
    public NovelRequestController(NovelRequestService novelRequestService) {
        this.novelRequestService = novelRequestService;
    }

    @GetMapping
    public List<NovelRequestResponse> getAllNovelRequests() {
        return novelRequestService.findAllNovelRequests();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NovelRequestResponse> getNovelRequestById(@PathVariable Long id) {
        return novelRequestService.findNovelRequestById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 202: la extracción corre en segundo plano; el progreso se consulta con GET /{id}
    @PostMapping
    public ResponseEntity<NovelRequestResponse> requestNovel(@Valid @RequestBody CreateNovelRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(novelRequestService.requestNovel(request));
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<NovelRequestResponse> refresh(@PathVariable Long id) {
        return novelRequestService.refresh(id)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
