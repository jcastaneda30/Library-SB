package com.library.scraper.site.model;

// Qué páginas de un sitio se abren en el navegador sin interfaz en lugar de descargarse directamente
public enum JavascriptRendering {
    // Todo se descarga directamente
    NONE,
    // Solo las páginas de capítulo; la ficha y el índice vienen en el HTML
    CHAPTERS,
    // Todas las páginas: ficha, índice y capítulos
    ALL
}
