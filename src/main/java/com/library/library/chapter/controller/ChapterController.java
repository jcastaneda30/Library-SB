package com.library.library.chapter.controller;

import com.library.common.config.OpenApiConfig;
import com.library.library.chapter.dto.ChapterResponse;
import com.library.library.chapter.dto.ChapterSummary;
import com.library.library.chapter.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/books/{bookId}/chapters")
@Tag(name = OpenApiConfig.CHAPTERS)
public class ChapterController {
    private final ChapterService chapterService;
    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @Operation(summary = "Listar capítulos de un libro", description = "Capítulos guardados, ordenados por número y sin contenido.")
    @GetMapping
    public ResponseEntity<List<ChapterSummary>> getChapters(@PathVariable Long bookId) {
        return chapterService.findChaptersByBook(bookId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Leer un capítulo", description = "Párrafos en orden. Los `TEXT` traen HTML saneado que solo conserva formato (negrita, cursiva, subrayado, tachado, sup/sub, color...); los `IMAGE` traen `imageUrl`.")
    @GetMapping("/{number}")
    public ResponseEntity<ChapterResponse> getChapter(@PathVariable Long bookId, @PathVariable int number) {
        return chapterService.findChapter(bookId, number)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
