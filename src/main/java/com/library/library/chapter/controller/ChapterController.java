package com.library.library.chapter.controller;

import com.library.library.chapter.dto.ChapterResponse;
import com.library.library.chapter.dto.ChapterSummary;
import com.library.library.chapter.service.ChapterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/books/{bookId}/chapters")
public class ChapterController {
    private final ChapterService chapterService;
    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping
    public ResponseEntity<List<ChapterSummary>> getChapters(@PathVariable Long bookId) {
        return chapterService.findChaptersByBook(bookId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{number}")
    public ResponseEntity<ChapterResponse> getChapter(@PathVariable Long bookId, @PathVariable int number) {
        return chapterService.findChapter(bookId, number)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
