-- Se elige qué páginas se renderizan con navegador: NONE, CHAPTERS o ALL
ALTER TABLE scraper_site ADD COLUMN javascript_rendering VARCHAR(10) NOT NULL DEFAULT 'NONE';
UPDATE scraper_site SET javascript_rendering = 'ALL' WHERE render_javascript;
ALTER TABLE scraper_site DROP COLUMN render_javascript;
