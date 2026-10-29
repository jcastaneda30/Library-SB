package com.library.scraper.engine;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Convierte el contenido de un capítulo en párrafos individuales. Los bloques (p, div, h1...) y los
 * br cortan párrafo; las imágenes son párrafos propios. Dentro de cada párrafo solo se conservan las
 * etiquetas de formato de {@link #SAFELIST}.
 */
@Component
public class ParagraphExtractor {

    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "div", "section", "article", "main", "header", "footer", "aside", "blockquote", "center",
            "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li", "dl", "dt", "dd", "pre",
            "table", "thead", "tbody", "tr", "td", "th", "figure", "figcaption", "hr"
    );
    private static final Set<String> IGNORED_TAGS = Set.of(
            "script", "style", "noscript", "iframe", "button", "form", "input", "select", "textarea", "svg", "ins"
    );
    private static final String STRUCTURE_QUERY = "img, br, " + String.join(", ", BLOCK_TAGS);

    private static final Safelist SAFELIST = new Safelist()
            .addTags("b", "strong", "i", "em", "u", "s", "strike", "del", "sup", "sub", "small", "mark", "code", "span")
            .addAttributes("span", "style");
    private static final Set<String> ALLOWED_STYLES = Set.of(
            "color", "background-color", "font-weight", "font-style", "text-decoration", "text-decoration-line"
    );
    private static final Pattern SAFE_STYLE_VALUE = Pattern.compile("[#\\w\\s(),.%-]+");

    public List<ExtractedParagraph> extract(Element root, String baseUrl) {
        List<ExtractedParagraph> paragraphs = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        walk(root, baseUrl, buffer, paragraphs);
        flush(buffer, paragraphs);
        return paragraphs;
    }

    private void walk(Element parent, String baseUrl, StringBuilder buffer, List<ExtractedParagraph> paragraphs) {
        for (Node node : parent.childNodes()) {
            if (node instanceof TextNode text) {
                buffer.append(text.outerHtml());
            } else if (node instanceof Element element) {
                String tag = element.normalName();
                if (IGNORED_TAGS.contains(tag)) {
                    continue;
                }
                if (tag.equals("br")) {
                    flush(buffer, paragraphs);
                } else if (tag.equals("img")) {
                    flush(buffer, paragraphs);
                    String src = imageSource(element, baseUrl);
                    if (src != null) {
                        paragraphs.add(ExtractedParagraph.image(src));
                    }
                } else if (BLOCK_TAGS.contains(tag)) {
                    flush(buffer, paragraphs);
                    walk(element, baseUrl, buffer, paragraphs);
                    flush(buffer, paragraphs);
                } else if (element.selectFirst(STRUCTURE_QUERY) != null) {
                    // Etiqueta en línea que envuelve bloques o imágenes: se recorre por dentro
                    walk(element, baseUrl, buffer, paragraphs);
                } else {
                    buffer.append(element.outerHtml());
                }
            }
        }
    }

    private void flush(StringBuilder buffer, List<ExtractedParagraph> paragraphs) {
        if (buffer.isEmpty()) {
            return;
        }
        String html = sanitize(buffer.toString());
        buffer.setLength(0);
        if (!Jsoup.parseBodyFragment(html).text().isBlank()) {
            paragraphs.add(ExtractedParagraph.text(html));
        }
    }

    static String sanitize(String html) {
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        String cleaned = Jsoup.clean(html, "", SAFELIST, settings);

        Document fragment = Jsoup.parseBodyFragment(cleaned);
        fragment.outputSettings(settings);
        for (Element span : fragment.body().select("span")) {
            String style = filterStyle(span.attr("style"));
            if (style.isEmpty()) {
                span.unwrap();
            } else {
                span.attr("style", style);
            }
        }
        // Los espacios duros y saltos de línea del HTML original no aportan nada dentro de un párrafo
        return fragment.body().html().replace(' ', ' ').replaceAll("\\s+", " ").strip();
    }

    private static String filterStyle(String style) {
        List<String> kept = new ArrayList<>();
        for (String declaration : style.split(";")) {
            String[] parts = declaration.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String property = parts[0].strip().toLowerCase();
            String value = parts[1].strip();
            if (ALLOWED_STYLES.contains(property) && SAFE_STYLE_VALUE.matcher(value).matches()
                    && !value.toLowerCase().contains("url(") && !value.toLowerCase().contains("expression(")) {
                kept.add(property + ": " + value);
            }
        }
        return String.join("; ", kept);
    }

    // Muchos sitios cargan las imágenes en diferido y dejan la URL real en data-src
    private static String imageSource(Element img, String baseUrl) {
        for (String attribute : List.of("data-src", "data-lazy-src", "data-original", "src")) {
            String value = img.attr(attribute).strip();
            if (value.isEmpty()) {
                continue;
            }
            if (value.startsWith("data:")) {
                return value.startsWith("data:image/") ? value : null;
            }
            String resolved = StringUtil.resolve(baseUrl, value);
            if (!resolved.isEmpty()) {
                return resolved;
            }
        }
        return null;
    }
}
