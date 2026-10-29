package com.library.scraper.engine;

public record FetchResult(String url, byte[] body, String contentType) {}
