package com.library.library.chapter.service;

import com.library.library.book.repository.BookRepository;
import com.library.library.chapter.dto.ChapterResponse;
import com.library.library.chapter.dto.ChapterSummary;
import com.library.library.chapter.dto.ImportChapterRequest;
import com.library.library.chapter.dto.ImportParagraph;
import com.library.library.chapter.model.Chapter;
import com.library.library.chapter.model.ChapterParagraph;
import com.library.library.chapter.model.ParagraphType;
import com.library.library.chapter.repository.ChapterRepository;
import com.library.library.image.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;
    private final ImageRepository imageRepository;

    public ChapterService(ChapterRepository chapterRepository,
                          BookRepository bookRepository,
                          ImageRepository imageRepository) {
        this.chapterRepository = chapterRepository;
        this.bookRepository = bookRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void importChapter(ImportChapterRequest request) {
        Chapter chapter = new Chapter();
        chapter.setBook(bookRepository.getReferenceById(request.bookId()));
        chapter.setNumber(request.number());
        chapter.setTitle(request.title());
        chapter.setSourceUrl(request.sourceUrl());
        for (ImportParagraph imported : request.paragraphs()) {
            ChapterParagraph paragraph = new ChapterParagraph();
            paragraph.setType(imported.type());
            if (imported.type() == ParagraphType.IMAGE) {
                paragraph.setImage(imageRepository.getReferenceById(imported.imageId()));
            } else {
                paragraph.setContent(imported.content());
            }
            chapter.addParagraph(paragraph);
        }
        chapterRepository.save(chapter);
    }

    @Transactional(readOnly = true)
    public Set<Integer> findChapterNumbers(Long bookId) {
        return new HashSet<>(chapterRepository.findNumbersByBookId(bookId));
    }

    @Transactional(readOnly = true)
    public Optional<List<ChapterSummary>> findChaptersByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            return Optional.empty();
        }
        return Optional.of(chapterRepository.findByBookIdOrderByNumber(bookId).stream()
                .map(ChapterSummary::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<ChapterResponse> findChapter(Long bookId, int number) {
        return chapterRepository.findByBookIdAndNumber(bookId, number).map(ChapterResponse::from);
    }
}
