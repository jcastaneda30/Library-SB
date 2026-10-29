package com.library.scraper.engine;

import com.library.scraper.site.model.ScraperSite;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PageFetcher {

    private static final String DEFAULT_USER_AGENT = "LibraryScraper/0.1";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(TIMEOUT)
            .build();
    private final ObjectMapper objectMapper;
    // Momento a partir del cual se puede volver a pedir algo a cada host
    private final Map<String, Long> nextRequestByHost = new ConcurrentHashMap<>();

    public PageFetcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Page fetchPage(String url, ScraperSite site) {
        FetchResult result = fetch(url, site);
        if (isJson(result)) {
            try {
                return new Page.Json(result.url(), objectMapper.readTree(result.body()));
            } catch (JacksonException e) {
                throw new ScrapeException("La respuesta de " + url + " no es un JSON válido", e);
            }
        }
        try {
            // Jsoup detecta el charset a partir de la propia página
            return new Page.Html(result.url(), Jsoup.parse(new ByteArrayInputStream(result.body()), null, result.url()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FetchResult fetch(String url, ScraperSite site) {
        URI uri = URI.create(url);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("User-Agent", site.getUserAgent() == null ? DEFAULT_USER_AGENT : site.getUserAgent())
                .header("Accept", "text/html,application/json,image/*;q=0.9,*/*;q=0.8")
                .GET()
                .build();

        ScrapeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            throttle(uri.getHost(), site.getRequestDelayMs() * (long) attempt);
            try {
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return new FetchResult(
                            response.uri().toString(),
                            response.body(),
                            response.headers().firstValue("Content-Type").orElse(null));
                }
                lastError = new ScrapeException("HTTP " + status + " al descargar " + url);
                // Solo vale la pena reintentar errores temporales
                if (status != 429 && status < 500) {
                    throw lastError;
                }
            } catch (IOException e) {
                lastError = new ScrapeException("No se pudo descargar " + url + ": " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ScrapeException("Descarga interrumpida: " + url, e);
            }
        }
        throw lastError;
    }

    private void throttle(String host, long delayMs) {
        long waitMs;
        synchronized (nextRequestByHost) {
            long now = System.currentTimeMillis();
            long allowedAt = Math.max(now, nextRequestByHost.getOrDefault(host, now));
            nextRequestByHost.put(host, allowedAt + delayMs);
            waitMs = allowedAt - now;
        }
        if (waitMs > 0) {
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ScrapeException("Descarga interrumpida", e);
            }
        }
    }

    private static boolean isJson(FetchResult result) {
        if (result.contentType() != null && result.contentType().toLowerCase().contains("json")) {
            return true;
        }
        for (byte b : result.body()) {
            if (!Character.isWhitespace(b)) {
                return b == '{' || b == '[';
            }
        }
        return false;
    }
}
