package com.library.scraper.engine;

// El sitio no respondió o no tenía lo que la configuración esperaba
public class ScrapeException extends RuntimeException {
    public ScrapeException(String message) {
        super(message);
    }

    public ScrapeException(String message, Throwable cause) {
        super(message, cause);
    }
}
