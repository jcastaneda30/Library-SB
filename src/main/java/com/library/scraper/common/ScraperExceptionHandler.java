package com.library.scraper.common;

import com.library.scraper.engine.ScrapeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScraperExceptionHandler {

    // El sitio scrapeado falló o no tenía lo que la configuración esperaba
    @ExceptionHandler(ScrapeException.class)
    public ProblemDetail handleScrapeError(ScrapeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problemDetail.setTitle("Error al scrapear el sitio");
        return problemDetail;
    }
}
