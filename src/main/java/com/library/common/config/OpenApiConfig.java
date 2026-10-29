package com.library.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Documentación de la API en /swagger-ui.html (especificación en /v3/api-docs)
@Configuration
public class OpenApiConfig {

    public static final String AUTHORS = "Autores";
    public static final String GENRES = "Géneros";
    public static final String BOOKS = "Libros";
    public static final String CHAPTERS = "Capítulos";
    public static final String IMAGES = "Imágenes";
    public static final String SCRAPER_SITES = "Scraper: sitios";
    public static final String SCRAPER_NOVELS = "Scraper: novelas";

    @Bean
    public OpenAPI libraryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library API")
                        .version("0.0.1")
                        .description("""
                                Biblioteca de libros y novelas web.

                                **Flujo del scraper:** registrar un sitio con su URL base y sus reglas \
                                (`Scraper: sitios`), probarlo con *preview*, pedir novelas que estén bajo esa URL \
                                (`Scraper: novelas`) y leer el resultado en `Libros` y `Capítulos`.

                                Los errores siguen el formato Problem Details: `400` datos inválidos, \
                                `404` no encontrado, `502` el sitio scrapeado falló.
                                """))
                // El orden de las etiquetas es el orden en que aparecen en Swagger UI
                .tags(List.of(
                        new Tag().name(AUTHORS),
                        new Tag().name(GENRES),
                        new Tag().name(BOOKS).description("Libros creados a mano o importados por el scraper"),
                        new Tag().name(CHAPTERS).description("Capítulos guardados, divididos en párrafos de texto o imagen"),
                        new Tag().name(IMAGES).description("Portadas e imágenes de capítulos, en disco o S3"),
                        new Tag().name(SCRAPER_SITES).description("Sitios configurados: URL base y reglas de extracción"),
                        new Tag().name(SCRAPER_NOVELS).description("Peticiones para extraer una novela de un sitio configurado")
                ));
    }
}
