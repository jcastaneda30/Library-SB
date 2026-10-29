CREATE TABLE image (
    id BIGSERIAL PRIMARY KEY,
    source_url VARCHAR(2048),
    storage_path VARCHAR(512) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Un libro scrapeado no tiene páginas ni fecha de publicación
ALTER TABLE book ALTER COLUMN pages DROP NOT NULL;
ALTER TABLE book ALTER COLUMN published_at DROP NOT NULL;
ALTER TABLE book ADD COLUMN cover_image_id BIGINT REFERENCES image(id);
ALTER TABLE book ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE book ADD COLUMN total_chapters INTEGER;
ALTER TABLE book ADD COLUMN source_url VARCHAR(1024) UNIQUE;
CREATE INDEX idx_book_cover_image ON book(cover_image_id);

CREATE TABLE chapter (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    number INTEGER NOT NULL,
    title VARCHAR(512),
    source_url VARCHAR(1024),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (book_id, number)
);

CREATE TABLE chapter_paragraph (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL REFERENCES chapter(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    type VARCHAR(10) NOT NULL,
    content TEXT,
    image_id BIGINT REFERENCES image(id),
    UNIQUE (chapter_id, position)
);
CREATE INDEX idx_chapter_paragraph_image ON chapter_paragraph(image_id);

CREATE TABLE scraper_site (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    user_agent VARCHAR(512),
    request_delay_ms INTEGER NOT NULL DEFAULT 1000,
    novel_url_template VARCHAR(1024),
    chapter_list_url_template VARCHAR(1024),
    chapter_url_template VARCHAR(1024),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE scraper_rule (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES scraper_site(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    field VARCHAR(30) NOT NULL,
    selector VARCHAR(1024) NOT NULL,
    attribute VARCHAR(255),
    pattern VARCHAR(512)
);
CREATE INDEX idx_scraper_rule_site ON scraper_rule(site_id);

CREATE TABLE scrape_job (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES scraper_site(id),
    book_id BIGINT REFERENCES book(id) ON DELETE SET NULL,
    url VARCHAR(1024) NOT NULL,
    include_chapters BOOLEAN NOT NULL,
    from_chapter INTEGER,
    to_chapter INTEGER,
    status VARCHAR(20) NOT NULL,
    total_chapters INTEGER,
    processed_chapters INTEGER NOT NULL DEFAULT 0,
    error TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_scrape_job_site ON scrape_job(site_id);
CREATE INDEX idx_scrape_job_book ON scrape_job(book_id);
CREATE INDEX idx_scrape_job_status ON scrape_job(status);
