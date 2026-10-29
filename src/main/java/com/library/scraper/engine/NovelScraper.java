package com.library.scraper.engine;

import com.library.scraper.site.model.ScraperField;
import com.library.scraper.site.model.ScraperRule;
import com.library.scraper.site.model.ScraperSite;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Aplica la configuración de un sitio: datos de la novela, índice de capítulos y contenido de cada capítulo
@Component
public class NovelScraper {

    // Evita bucles infinitos si el enlace "siguiente" del índice nunca se acaba
    private static final int MAX_CHAPTER_LIST_PAGES = 500;
    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,.\\s]*");

    private final PageFetcher pageFetcher;
    private final RuleEvaluator ruleEvaluator;
    private final ParagraphExtractor paragraphExtractor;

    public NovelScraper(PageFetcher pageFetcher, RuleEvaluator ruleEvaluator, ParagraphExtractor paragraphExtractor) {
        this.pageFetcher = pageFetcher;
        this.ruleEvaluator = ruleEvaluator;
        this.paragraphExtractor = paragraphExtractor;
    }

    public record NovelScrape(Page page, ScrapedNovel novel) {}

    // La ficha siempre se lee de la URL de la novela que se pidió
    public NovelScrape scrapeNovel(ScraperSite site, String novelUrl) {
        Page page = pageFetcher.fetchPage(novelUrl, site);
        return new NovelScrape(page, parseNovel(page, site.getRules()));
    }

    public ScrapedNovel parseNovel(Page page, List<ScraperRule> rules) {
        String title = ruleEvaluator.first(page, rules, ScraperField.TITLE)
                .orElseThrow(() -> new ScrapeException("No se encontró el título en " + page.url()));
        return new ScrapedNovel(
                title,
                ruleEvaluator.first(page, rules, ScraperField.COVER).map(cover -> resolve(page.url(), cover)).orElse(null),
                distinct(ruleEvaluator.values(page, rules, ScraperField.AUTHOR)),
                String.join("\n\n", ruleEvaluator.values(page, rules, ScraperField.SYNOPSIS)),
                distinct(ruleEvaluator.values(page, rules, ScraperField.GENRE)),
                ruleEvaluator.first(page, rules, ScraperField.STATUS).orElse(null),
                ruleEvaluator.first(page, rules, ScraperField.CHAPTER_COUNT).flatMap(NovelScraper::parseInt).orElse(null),
                ruleEvaluator.first(page, rules, ScraperField.NOVEL_ID).orElse(null)
        );
    }

    /**
     * URLs de los capítulos en orden. Si hay reglas CHAPTER_LINK se leen del índice (siguiendo
     * CHAPTER_LIST_NEXT); si no, se generan con chapterUrlTemplate y el número de capítulos.
     */
    public List<String> locateChapters(ScraperSite site, String novelUrl, NovelScrape scrape) {
        List<ScraperRule> rules = site.getRules();
        Map<String, String> variables = variablesFor(novelUrl, scrape.novel());

        boolean hasLinkRules = rules.stream().anyMatch(rule -> rule.getField() == ScraperField.CHAPTER_LINK);
        if (!hasLinkRules) {
            Integer count = scrape.novel().chapterCount();
            if (site.getChapterUrlTemplate() == null || count == null) {
                throw new ScrapeException("No se puede armar la lista de capítulos: configura reglas CHAPTER_LINK "
                        + "o chapterUrlTemplate junto con CHAPTER_COUNT");
            }
            List<String> urls = new ArrayList<>(count);
            for (int n = 1; n <= count; n++) {
                Map<String, String> chapterVariables = new HashMap<>(variables);
                chapterVariables.put("n", String.valueOf(n));
                chapterVariables.put("i", String.valueOf(n - 1));
                urls.add(UrlTemplate.expand(site.getChapterUrlTemplate(), chapterVariables));
            }
            return urls;
        }

        Page current = site.getChapterListUrlTemplate() == null
                ? scrape.page()
                : pageFetcher.fetchPage(UrlTemplate.expand(site.getChapterListUrlTemplate(), variables), site);
        Set<String> chapters = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>(Set.of(current.url()));
        for (int pages = 1; current != null; pages++) {
            for (String value : ruleEvaluator.values(current, rules, ScraperField.CHAPTER_LINK)) {
                chapters.add(chapterUrl(site, current.url(), value, variables));
            }
            String pageUrl = current.url();
            Optional<String> next = ruleEvaluator.first(current, rules, ScraperField.CHAPTER_LIST_NEXT)
                    .map(link -> resolve(pageUrl, link))
                    .filter(visited::add);
            current = next.isPresent() && pages < MAX_CHAPTER_LIST_PAGES ? pageFetcher.fetchPage(next.get(), site) : null;
        }
        return new ArrayList<>(chapters);
    }

    public ScrapedChapter scrapeChapter(ScraperSite site, String chapterUrl) {
        return parseChapter(pageFetcher.fetchPage(chapterUrl, site), site.getRules());
    }

    public ScrapedChapter parseChapter(Page page, List<ScraperRule> rules) {
        Element content = ruleEvaluator.content(page, rules)
                .orElseThrow(() -> new ScrapeException("No se encontró el contenido del capítulo en " + page.url()));
        List<ExtractedParagraph> paragraphs = paragraphExtractor.extract(content, page.url());
        if (paragraphs.isEmpty()) {
            throw new ScrapeException("El capítulo " + page.url() + " no tiene párrafos");
        }
        String title = ruleEvaluator.first(page, rules, ScraperField.CHAPTER_TITLE).orElse(null);
        return new ScrapedChapter(page.url(), title, paragraphs);
    }

    private static Map<String, String> variablesFor(String novelUrl, ScrapedNovel novel) {
        Map<String, String> variables = new HashMap<>(UrlTemplate.variablesFor(novelUrl));
        if (novel.novelId() != null) {
            variables.put("id", novel.novelId());
        }
        return variables;
    }

    private static String chapterUrl(ScraperSite site, String pageUrl, String value, Map<String, String> variables) {
        if (site.getChapterUrlTemplate() == null) {
            return resolve(pageUrl, value);
        }
        Map<String, String> chapterVariables = new HashMap<>(variables);
        chapterVariables.put("chapter", value);
        return UrlTemplate.expand(site.getChapterUrlTemplate(), chapterVariables);
    }

    private static String resolve(String baseUrl, String url) {
        String resolved = StringUtil.resolve(baseUrl, url);
        return resolved.isEmpty() ? url : resolved;
    }

    private static List<String> distinct(List<String> values) {
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    // "912 Chapters", "1,234" -> 912, 1234
    static Optional<Integer> parseInt(String text) {
        Matcher matcher = NUMBER.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(matcher.group().replaceAll("[,.\\s]", "")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
