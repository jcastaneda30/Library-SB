package com.library.scraper.engine;

import org.jsoup.nodes.Document;
import tools.jackson.databind.JsonNode;

// Página descargada; el formato se detecta por la respuesta
public sealed interface Page {
    String url();

    record Html(String url, Document document) implements Page {}

    record Json(String url, JsonNode root) implements Page {}
}
