package com.library.scraper.site.service;

import java.net.URI;

/**
 * URL base de un sitio configurado ("https://novelbin.xyz/"). Una novela pertenece al sitio si comparte
 * host (con o sin www) y su ruta empieza por la ruta de la base; el esquema http/https no importa.
 */
final class BaseUrl {

    private BaseUrl() {
    }

    // "HTTPS://www.NovelBin.xyz" -> "https://novelbin.xyz/"
    static String normalize(String baseUrl) {
        URI uri = parse(baseUrl);
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("La URL base no puede tener query ni fragmento: " + baseUrl);
        }
        return uri.getScheme().toLowerCase() + "://" + host(uri) + port(uri) + directory(uri.getRawPath());
    }

    static boolean contains(String baseUrl, String url) {
        URI base = parse(baseUrl);
        URI target = parse(url);
        return host(base).equals(host(target))
                && port(base).equals(port(target))
                && directory(target.getRawPath()).startsWith(directory(base.getRawPath()));
    }

    // Longitud de la ruta, para que gane la base más específica si varias contienen la URL
    static int specificity(String baseUrl) {
        return directory(parse(baseUrl).getRawPath()).length();
    }

    static URI parse(String url) {
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("URL inválida: " + url);
        }
        if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("La URL debe ser http(s): " + url);
        }
        return uri;
    }

    private static String host(URI uri) {
        String host = uri.getHost().toLowerCase();
        return host.startsWith("www.") ? host.substring(4) : host;
    }

    private static String port(URI uri) {
        return uri.getPort() == -1 ? "" : ":" + uri.getPort();
    }

    // Termina en "/" para que "/novel" no contenga a "/novelas"
    private static String directory(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.endsWith("/") ? path : path + "/";
    }
}
