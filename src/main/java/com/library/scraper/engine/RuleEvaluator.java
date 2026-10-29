package com.library.scraper.engine;

import com.library.scraper.site.model.ScraperField;
import com.library.scraper.site.model.ScraperRule;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// Aplica las reglas configuradas a una página HTML (selectores CSS) o JSON (JSON Pointer)
@Component
public class RuleEvaluator {

    public List<String> values(Page page, List<ScraperRule> rules, ScraperField field) {
        for (ScraperRule rule : rules) {
            if (rule.getField() != field) {
                continue;
            }
            List<String> values = apply(page, rule);
            if (!values.isEmpty()) {
                return values;
            }
        }
        return List.of();
    }

    public Optional<String> first(Page page, List<ScraperRule> rules, ScraperField field) {
        return values(page, rules, field).stream().findFirst();
    }

    /**
     * Devuelve el contenedor del capítulo ya sin los elementos de CHAPTER_EXCLUDE. En JSON el valor
     * se interpreta como HTML, o como texto plano con un párrafo por línea si no tiene etiquetas.
     */
    public Optional<Element> content(Page page, List<ScraperRule> rules) {
        Element content = null;
        for (ScraperRule rule : rules) {
            if (rule.getField() != ScraperField.CHAPTER_CONTENT) {
                continue;
            }
            content = page instanceof Page.Html html
                    ? merge(select(html, rule))
                    : apply(page, rule).stream().findFirst().map(value -> toHtml(value, page.url())).orElse(null);
            if (content != null) {
                break;
            }
        }
        if (content == null) {
            return Optional.empty();
        }
        for (ScraperRule rule : rules) {
            if (rule.getField() == ScraperField.CHAPTER_EXCLUDE) {
                content.select(rule.getSelector()).remove();
            }
        }
        return Optional.of(content);
    }

    private List<String> apply(Page page, ScraperRule rule) {
        List<String> raw = new ArrayList<>();
        if (page instanceof Page.Html html) {
            for (Element element : select(html, rule)) {
                raw.add(read(element, rule.getAttribute()));
            }
        } else if (page instanceof Page.Json json) {
            JsonNode node = at(json.root(), rule.getSelector());
            if (node.isArray()) {
                for (JsonNode item : node) {
                    raw.add(read(item, rule.getAttribute()));
                }
            } else {
                raw.add(read(node, rule.getAttribute()));
            }
        }

        Pattern pattern = compile(rule.getPattern());
        List<String> values = new ArrayList<>();
        for (String value : raw) {
            String matched = match(value, pattern);
            if (matched != null && !matched.isBlank()) {
                values.add(matched.strip());
            }
        }
        return values;
    }

    private static Elements select(Page.Html html, ScraperRule rule) {
        try {
            return html.document().select(rule.getSelector());
        } catch (Selector.SelectorParseException e) {
            throw new ScrapeException("Selector CSS inválido en la regla " + rule.getField() + ": " + rule.getSelector(), e);
        }
    }

    private static JsonNode at(JsonNode root, String pointer) {
        try {
            return root.at(pointer);
        } catch (IllegalArgumentException e) {
            throw new ScrapeException("JSON Pointer inválido: " + pointer, e);
        }
    }

    private static String read(Element element, String attribute) {
        if (attribute == null || attribute.isBlank() || attribute.equals("text")) {
            return element.text();
        }
        return switch (attribute) {
            case "html" -> element.html();
            case "ownText" -> element.ownText();
            default -> element.attr(attribute);
        };
    }

    private static String read(JsonNode node, String attribute) {
        JsonNode target = attribute == null || attribute.isBlank() ? node : at(node, attribute);
        if (target.isMissingNode() || target.isNull()) {
            return null;
        }
        return target.isValueNode() ? target.asString() : target.toString();
    }

    // Si el selector devuelve varios elementos (p. ej. "#content p") se juntan en un solo contenedor
    private static Element merge(Elements elements) {
        if (elements.isEmpty()) {
            return null;
        }
        if (elements.size() == 1) {
            return elements.first().clone();
        }
        Element container = new Element("div");
        elements.forEach(element -> container.appendChild(element.clone()));
        return container;
    }

    private static Element toHtml(String value, String baseUrl) {
        if (value.contains("<")) {
            return Jsoup.parseBodyFragment(value, baseUrl).body();
        }
        StringBuilder html = new StringBuilder();
        for (String line : value.split("\\R")) {
            html.append("<p>").append(Entities.escape(line)).append("</p>");
        }
        return Jsoup.parseBodyFragment(html.toString(), baseUrl).body();
    }

    private static Pattern compile(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new ScrapeException("Regex inválida: " + pattern, e);
        }
    }

    private static String match(String value, Pattern pattern) {
        if (value == null || pattern == null) {
            return value;
        }
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group();
    }
}
