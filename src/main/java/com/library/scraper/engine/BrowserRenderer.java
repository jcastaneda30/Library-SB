package com.library.scraper.engine;

import com.library.scraper.site.model.ScraperSite;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Abre páginas en Chromium sin interfaz para los sitios que arman su contenido con JavaScript.
 * El navegador se arranca la primera vez que se necesita y se comparte; como Playwright no es
 * seguro entre hilos, las páginas se renderizan de una en una.
 */
@Component
public class BrowserRenderer {

    private static final Logger log = LoggerFactory.getLogger(BrowserRenderer.class);

    private static final double NAVIGATION_TIMEOUT_MS = 30_000;
    private static final double WAIT_TIMEOUT_MS = 15_000;
    // No hacen falta para leer el texto y son lo más pesado de la página
    private static final Set<String> SKIPPED_RESOURCES = Set.of("image", "media", "font");
    private static final String INSTALL_HINT = "Instala Chromium para Playwright con: ./mvnw exec:java "
            + "-Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=\"install chromium\"";

    private Playwright playwright;
    private Browser browser;

    public synchronized RenderedPage render(String url, ScraperSite site) {
        Browser chromium = browser();
        Browser.NewContextOptions options = new Browser.NewContextOptions();
        if (site.getUserAgent() != null) {
            options.setUserAgent(site.getUserAgent());
        }
        // Un contexto nuevo por página: sin cookies ni estado compartido entre peticiones
        try (BrowserContext context = chromium.newContext(options)) {
            context.route("**/*", route -> {
                if (SKIPPED_RESOURCES.contains(route.request().resourceType())) {
                    route.abort();
                } else {
                    route.resume();
                }
            });
            com.microsoft.playwright.Page page = context.newPage();
            page.setDefaultTimeout(WAIT_TIMEOUT_MS);
            page.navigate(url, new com.microsoft.playwright.Page.NavigateOptions()
                    .setTimeout(NAVIGATION_TIMEOUT_MS)
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            waitForContent(page, site.getWaitForSelector(), url);
            return new RenderedPage(page.url(), page.content());
        } catch (TimeoutError e) {
            throw new ScrapeException("La página " + url + " no cargó a tiempo en el navegador", e);
        } catch (PlaywrightException e) {
            throw new ScrapeException("El navegador no pudo abrir " + url + ": " + firstLine(e.getMessage()), e);
        }
    }

    public record RenderedPage(String url, String html) {}

    private static void waitForContent(com.microsoft.playwright.Page page, String waitForSelector, String url) {
        if (waitForSelector != null) {
            try {
                page.waitForSelector(waitForSelector);
            } catch (TimeoutError e) {
                // Se sigue con lo que haya; las reglas dirán qué faltó
                log.warn("No apareció {} en {} tras {} ms", waitForSelector, url, (long) WAIT_TIMEOUT_MS);
            }
            return;
        }
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (TimeoutError e) {
            // Los anuncios pueden mantener la red ocupada indefinidamente
            log.debug("La red no quedó inactiva en {}, se usa el contenido actual", url);
        }
    }

    private Browser browser() {
        if (browser == null || !browser.isConnected()) {
            close();
            try {
                // Solo se usa Chromium: no se descargan Firefox ni WebKit al arrancar
                playwright = Playwright.create(new Playwright.CreateOptions()
                        .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
                browser = playwright.chromium().launch();
            } catch (PlaywrightException e) {
                close();
                String message = e.getMessage() != null && e.getMessage().contains("Executable doesn't exist")
                        ? "Chromium no está instalado. " + INSTALL_HINT
                        : "No se pudo iniciar el navegador: " + firstLine(e.getMessage());
                throw new ScrapeException(message, e);
            }
        }
        return browser;
    }

    @PreDestroy
    public synchronized void close() {
        if (playwright != null) {
            try {
                playwright.close();
            } catch (PlaywrightException e) {
                log.debug("Error al cerrar Playwright", e);
            }
        }
        playwright = null;
        browser = null;
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }
}
