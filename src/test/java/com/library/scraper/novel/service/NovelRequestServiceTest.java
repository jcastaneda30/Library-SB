package com.library.scraper.novel.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NovelRequestServiceTest {

    @Test
    void sameNovelAlwaysHasTheSameUrl() {
        String expected = "https://novelbin.xyz/novel/skyfire-avenue";

        assertThat(NovelRequestService.normalizeUrl(expected)).isEqualTo(expected);
        assertThat(NovelRequestService.normalizeUrl("  https://novelbin.xyz/novel/skyfire-avenue/ ")).isEqualTo(expected);
        assertThat(NovelRequestService.normalizeUrl("https://novelbin.xyz/novel/skyfire-avenue#chapters")).isEqualTo(expected);
    }

    @Test
    void keepsQueryStrings() {
        assertThat(NovelRequestService.normalizeUrl("https://sitio.com/novel?id=12"))
                .isEqualTo("https://sitio.com/novel?id=12");
    }
}
