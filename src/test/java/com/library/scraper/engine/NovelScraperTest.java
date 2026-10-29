package com.library.scraper.engine;

import com.library.library.book.model.NovelStatus;
import com.library.scraper.site.model.ScraperField;
import com.library.scraper.site.model.ScraperRule;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NovelScraperTest {

    private final RuleEvaluator ruleEvaluator = new RuleEvaluator();
    private final NovelScraper novelScraper = new NovelScraper(null, ruleEvaluator, new ParagraphExtractor());

    // Misma estructura que la ficha de novelbin.xyz/novel/...
    private static final String NOVEL_HTML = """
            <div id="ssr-novel-card">
              <img src="https://cdn.example.com/thumbnail/skyfire-avenue.jpg" alt="Cover">
              <h1>Skyfire Avenue</h1>
              <div>
                <p><strong>Author:</strong> Tang Jia San Shao</p>
                <p><strong>Status:</strong> Completed</p>
                <p><strong>Total Chapters:</strong> 1,912 Chapters</p>
                <div><strong>Genres:</strong> <div><a href="/genre/action">Action</a> <a href="/genre/sci-fi">Sci-fi</a></div></div>
              </div>
              <section><h2>Synopsis</h2><div><p>Primer párrafo.</p><p>Segundo párrafo.</p></div></section>
            </div>
            """;

    private static final List<ScraperRule> NOVEL_RULES = List.of(
            rule(ScraperField.TITLE, "#ssr-novel-card h1", null, null),
            rule(ScraperField.COVER, "#ssr-novel-card img", "abs:src", null),
            rule(ScraperField.AUTHOR, "#ssr-novel-card p:has(strong:containsOwn(Author))", "ownText", null),
            rule(ScraperField.STATUS, "#ssr-novel-card p:has(strong:containsOwn(Status))", "ownText", null),
            rule(ScraperField.CHAPTER_COUNT, "#ssr-novel-card p:has(strong:containsOwn(Total Chapters))", "ownText", null),
            rule(ScraperField.GENRE, "#ssr-novel-card div:has(> strong:containsOwn(Genres)) a", null, null),
            rule(ScraperField.SYNOPSIS, "#ssr-novel-card section p", null, null)
    );

    @Test
    void parsesNovelMetadataFromHtml() {
        ScrapedNovel novel = novelScraper.parseNovel(html("https://novelbin.xyz/novel/skyfire-avenue", NOVEL_HTML), NOVEL_RULES);

        assertThat(novel.title()).isEqualTo("Skyfire Avenue");
        assertThat(novel.coverUrl()).isEqualTo("https://cdn.example.com/thumbnail/skyfire-avenue.jpg");
        assertThat(novel.authors()).containsExactly("Tang Jia San Shao");
        assertThat(novel.genres()).containsExactly("Action", "Sci-fi");
        assertThat(novel.chapterCount()).isEqualTo(1912);
        assertThat(novel.synopsis()).isEqualTo("Primer párrafo.\n\nSegundo párrafo.");
        assertThat(StatusParser.parse(novel.status())).isEqualTo(NovelStatus.COMPLETED);
    }

    @Test
    void fallsBackToNextRuleOfSameField() {
        List<ScraperRule> rules = List.of(
                rule(ScraperField.TITLE, ".does-not-exist", null, null),
                rule(ScraperField.TITLE, "h1", null, null));

        assertThat(novelScraper.parseNovel(html("https://x.test/n", NOVEL_HTML), rules).title()).isEqualTo("Skyfire Avenue");
    }

    @Test
    void failsWhenTitleIsMissing() {
        assertThatThrownBy(() -> novelScraper.parseNovel(html("https://x.test/n", "<p>nada</p>"), NOVEL_RULES))
                .isInstanceOf(ScrapeException.class)
                .hasMessageContaining("título");
    }

    @Test
    void parsesNovelMetadataFromJsonWithPointers() {
        Page page = json("https://api.example.com/novel/1.json", """
                {"id": 77, "title": "Skyfire Avenue", "status": "Ongoing",
                 "author": {"name": "Tang Jia San Shao"},
                 "genres": [{"name": "Action"}, {"name": "Mecha"}]}
                """);
        List<ScraperRule> rules = List.of(
                rule(ScraperField.TITLE, "/title", null, null),
                rule(ScraperField.AUTHOR, "/author/name", null, null),
                rule(ScraperField.GENRE, "/genres", "/name", null),
                rule(ScraperField.STATUS, "/status", null, null),
                rule(ScraperField.NOVEL_ID, "/id", null, null));

        ScrapedNovel novel = novelScraper.parseNovel(page, rules);

        assertThat(novel.authors()).containsExactly("Tang Jia San Shao");
        assertThat(novel.genres()).containsExactly("Action", "Mecha");
        assertThat(novel.novelId()).isEqualTo("77");
        assertThat(StatusParser.parse(novel.status())).isEqualTo(NovelStatus.ONGOING);
    }

    @Test
    void regexKeepsFirstGroup() {
        Page page = html("https://x.test/n", "<h1>Skyfire Avenue</h1><span class='id'>novel-id: 4521</span>");
        List<ScraperRule> rules = List.of(
                rule(ScraperField.TITLE, "h1", null, null),
                rule(ScraperField.NOVEL_ID, "span.id", null, "novel-id: (\\d+)"));

        assertThat(novelScraper.parseNovel(page, rules).novelId()).isEqualTo("4521");
    }

    @Test
    void splitsChapterIntoParagraphsKeepingFormattingAndImages() {
        Page page = html("https://site.test/read/novel/chapter-1", """
                <h2 class="title">Chapter 1: Inicio</h2>
                <div id="content">
                  <p>Texto con <strong>negrita</strong>, <em>cursiva</em> y <a href="/x">enlace</a>.</p>
                  <div class="ads"><p>Publicidad</p></div>
                  <p><span style="color: red; position: absolute">rojo</span> <span class="x">plano</span></p>
                  <p><img data-src="/img/mapa.png" src="placeholder.gif"></p>
                  Línea suelta<br>Otra línea<br><br>
                  <script>alert(1)</script>
                  <p onclick="evil()">&nbsp;</p>
                </div>
                """);
        List<ScraperRule> rules = List.of(
                rule(ScraperField.CHAPTER_TITLE, "h2.title", null, null),
                rule(ScraperField.CHAPTER_CONTENT, "#content", null, null),
                rule(ScraperField.CHAPTER_EXCLUDE, ".ads", null, null));

        ScrapedChapter chapter = novelScraper.parseChapter(page, rules);

        assertThat(chapter.title()).isEqualTo("Chapter 1: Inicio");
        assertThat(chapter.paragraphs()).containsExactly(
                ExtractedParagraph.text("Texto con <strong>negrita</strong>, <em>cursiva</em> y enlace."),
                ExtractedParagraph.text("<span style=\"color: red\">rojo</span> plano"),
                ExtractedParagraph.image("https://site.test/img/mapa.png"),
                ExtractedParagraph.text("Línea suelta"),
                ExtractedParagraph.text("Otra línea"));
    }

    @Test
    void jsonChapterWithoutMarkupSplitsByLine() {
        Page page = json("https://api.example.com/read/77/0.json", """
                {"content": "Primera línea\\n\\nSegunda línea & más"}
                """);
        List<ScraperRule> rules = List.of(rule(ScraperField.CHAPTER_CONTENT, "/content", null, null));

        assertThat(novelScraper.parseChapter(page, rules).paragraphs()).containsExactly(
                ExtractedParagraph.text("Primera línea"),
                ExtractedParagraph.text("Segunda línea &amp; más"));
    }

    @Test
    void expandsUrlTemplates() {
        Map<String, String> variables = new java.util.HashMap<>(UrlTemplate.variablesFor("https://novelbin.xyz/novel/skyfire-avenue/"));
        variables.put("n", "3");

        assertThat(UrlTemplate.expand("{origin}/read/{slug}/chapter-{n}", variables))
                .isEqualTo("https://novelbin.xyz/read/skyfire-avenue/chapter-3");
        assertThatThrownBy(() -> UrlTemplate.expand("{origin}/{id}", variables))
                .isInstanceOf(ScrapeException.class);
    }

    @Test
    void mapsStatusText() {
        assertThat(StatusParser.parse("En curso")).isEqualTo(NovelStatus.ONGOING);
        assertThat(StatusParser.parse("Hiatus")).isEqualTo(NovelStatus.HIATUS);
        assertThat(StatusParser.parse("Finalizada")).isEqualTo(NovelStatus.COMPLETED);
        assertThat(StatusParser.parse(null)).isEqualTo(NovelStatus.UNKNOWN);
    }

    private static Page html(String url, String body) {
        return new Page.Html(url, Jsoup.parse(body, url));
    }

    private static Page json(String url, String body) {
        return new Page.Json(url, JsonMapper.builder().build().readTree(body));
    }

    private static ScraperRule rule(ScraperField field, String selector, String attribute, String pattern) {
        ScraperRule rule = new ScraperRule();
        rule.setField(field);
        rule.setSelector(selector);
        rule.setAttribute(attribute);
        rule.setPattern(pattern);
        return rule;
    }
}
