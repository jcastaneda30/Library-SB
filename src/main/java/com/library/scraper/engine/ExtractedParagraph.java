package com.library.scraper.engine;

import com.library.library.chapter.model.ParagraphType;

// TEXT trae el HTML saneado en content; IMAGE trae la URL absoluta de la imagen
public record ExtractedParagraph(ParagraphType type, String content, String imageUrl) {
    public static ExtractedParagraph text(String html) {
        return new ExtractedParagraph(ParagraphType.TEXT, html, null);
    }

    public static ExtractedParagraph image(String url) {
        return new ExtractedParagraph(ParagraphType.IMAGE, null, url);
    }
}
