package com.library.library.chapter.dto;

import com.library.library.chapter.model.ParagraphType;

// TEXT: content con HTML ya saneado. IMAGE: imageId de una imagen ya guardada
public record ImportParagraph(ParagraphType type, String content, Long imageId) {}
