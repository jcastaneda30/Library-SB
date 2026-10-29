package com.library.scraper.site.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dato que extrae una {@link ScraperRule}. Si hay varias reglas para el mismo campo se prueban en orden
 * y gana la primera que devuelva algo, así se pueden declarar alternativas.
 */
@Schema(description = """
        Dato que extrae una regla:
        - Novela: `TITLE` (obligatorio), `COVER`, `AUTHOR`, `SYNOPSIS`, `GENRE`, `STATUS`, `CHAPTER_COUNT` \
        y `NOVEL_ID` (disponible como `{id}` en las plantillas).
        - Índice: `CHAPTER_LINK` (enlaces o ids de capítulos) y `CHAPTER_LIST_NEXT` (siguiente página).
        - Capítulo: `CHAPTER_TITLE`, `CHAPTER_CONTENT` (contenedor que se divide en párrafos) y \
        `CHAPTER_EXCLUDE` (elementos a quitar del contenido).
        """)
public enum ScraperField {
    // Página principal de la novela
    TITLE,
    COVER,
    AUTHOR,
    SYNOPSIS,
    GENRE,
    STATUS,
    CHAPTER_COUNT,
    // Identificador interno del sitio, disponible como {id} en las plantillas de URL
    NOVEL_ID,

    // Índice de capítulos: enlaces (o ids si hay chapterUrlTemplate) y enlace a la siguiente página del índice
    CHAPTER_LINK,
    CHAPTER_LIST_NEXT,

    // Página de un capítulo
    CHAPTER_TITLE,
    CHAPTER_CONTENT,
    // Selector CSS de elementos a eliminar dentro del contenido (anuncios, notas del traductor, ...)
    CHAPTER_EXCLUDE
}
