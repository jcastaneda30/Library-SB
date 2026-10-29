package com.library.library.chapter.dto;

import com.library.library.chapter.model.ChapterParagraph;
import com.library.library.chapter.model.ParagraphType;
import com.library.library.image.controller.ImageUrls;

public record ParagraphResponse(int position, ParagraphType type, String content, String imageUrl) {
    public static ParagraphResponse from(ChapterParagraph paragraph) {
        return new ParagraphResponse(
                paragraph.getPosition(),
                paragraph.getType(),
                paragraph.getContent(),
                paragraph.getImage() == null ? null : ImageUrls.of(paragraph.getImage().getId())
        );
    }
}
