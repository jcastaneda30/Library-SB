-- Sitios que arman su contenido con JavaScript se abren en un navegador sin interfaz
ALTER TABLE scraper_site ADD COLUMN render_javascript BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE scraper_site ADD COLUMN wait_for_selector VARCHAR(512);
