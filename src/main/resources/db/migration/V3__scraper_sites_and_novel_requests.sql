-- Un sitio se define por su URL base (https://novelbin.xyz/) y no solo por el dominio
ALTER TABLE scraper_site RENAME COLUMN domain TO base_url;
ALTER TABLE scraper_site ALTER COLUMN base_url TYPE VARCHAR(1024);
UPDATE scraper_site SET base_url = 'https://' || base_url || '/' WHERE base_url NOT LIKE 'http%';

-- La ficha siempre se lee de la URL de la novela pedida
ALTER TABLE scraper_site DROP COLUMN novel_url_template;

-- Las peticiones de novela reemplazan a los jobs: una por URL, que se reutiliza al volver a pedirla
DROP TABLE scrape_job;

CREATE TABLE novel_request (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES scraper_site(id),
    book_id BIGINT REFERENCES book(id) ON DELETE SET NULL,
    url VARCHAR(1024) NOT NULL UNIQUE,
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
CREATE INDEX idx_novel_request_site ON novel_request(site_id);
CREATE INDEX idx_novel_request_book ON novel_request(book_id);
CREATE INDEX idx_novel_request_status ON novel_request(status);
