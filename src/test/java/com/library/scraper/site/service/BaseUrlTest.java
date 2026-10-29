package com.library.scraper.site.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseUrlTest {

    @Test
    void normalizesBaseUrl() {
        assertThat(BaseUrl.normalize("https://novelbin.xyz")).isEqualTo("https://novelbin.xyz/");
        assertThat(BaseUrl.normalize("HTTPS://www.NovelBin.xyz/")).isEqualTo("https://novelbin.xyz/");
        assertThat(BaseUrl.normalize("https://sitio.com/novelas")).isEqualTo("https://sitio.com/novelas/");
    }

    @Test
    void containsNovelsUnderTheBase() {
        assertThat(BaseUrl.contains("https://novelbin.xyz/", "https://novelbin.xyz/novel/skyfire-avenue")).isTrue();
        assertThat(BaseUrl.contains("https://novelbin.xyz/", "http://www.novelbin.xyz/novel/skyfire-avenue")).isTrue();
        assertThat(BaseUrl.contains("https://sitio.com/novelas/", "https://sitio.com/novelas/una")).isTrue();
    }

    @Test
    void rejectsUrlsOutsideTheBase() {
        assertThat(BaseUrl.contains("https://novelbin.xyz/", "https://otro.com/novel/x")).isFalse();
        assertThat(BaseUrl.contains("https://novelbin.xyz/", "https://evil-novelbin.xyz/novel/x")).isFalse();
        assertThat(BaseUrl.contains("https://novelbin.xyz/", "https://novelbin.xyz:8080/novel/x")).isFalse();
        assertThat(BaseUrl.contains("https://sitio.com/novel/", "https://sitio.com/novelas/una")).isFalse();
    }

    @Test
    void moreSpecificBaseWins() {
        assertThat(BaseUrl.specificity("https://sitio.com/novelas/")).isGreaterThan(BaseUrl.specificity("https://sitio.com/"));
    }

    @Test
    void rejectsInvalidUrls() {
        assertThatThrownBy(() -> BaseUrl.normalize("novelbin.xyz")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BaseUrl.normalize("ftp://novelbin.xyz/")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BaseUrl.normalize("https://novelbin.xyz/?a=1")).isInstanceOf(IllegalArgumentException.class);
    }
}
