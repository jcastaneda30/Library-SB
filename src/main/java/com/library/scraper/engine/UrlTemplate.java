package com.library.scraper.engine;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Plantillas tipo "{origin}/read/{slug}/chapter-{n}"
public final class UrlTemplate {

    private static final Pattern VARIABLE = Pattern.compile("\\{(\\w+)}");

    private UrlTemplate() {
    }

    // Variables que se pueden sacar de la URL de la novela
    public static Map<String, String> variablesFor(String novelUrl) {
        URI uri = URI.create(novelUrl);
        String path = uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", "");
        Map<String, String> variables = new HashMap<>();
        variables.put("url", novelUrl);
        variables.put("origin", uri.getScheme() + "://" + uri.getRawAuthority());
        variables.put("slug", path.substring(path.lastIndexOf('/') + 1));
        return variables;
    }

    public static String expand(String template, Map<String, String> variables) {
        Matcher matcher = VARIABLE.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = variables.get(matcher.group(1));
            if (value == null) {
                throw new ScrapeException("La plantilla " + template + " usa {" + matcher.group(1) + "}, que no tiene valor");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
