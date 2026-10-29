package com.library.scraper.site.repository;

import com.library.scraper.site.model.ScraperSite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScraperSiteRepository extends JpaRepository<ScraperSite, Long> {

    // Las reglas siempre se usan junto al sitio, se cargan en la misma consulta
    @Override
    @EntityGraph(attributePaths = {"rules"})
    List<ScraperSite> findAll();

    @Override
    @EntityGraph(attributePaths = {"rules"})
    Optional<ScraperSite> findById(Long id);

    @EntityGraph(attributePaths = {"rules"})
    List<ScraperSite> findByEnabledTrue();

    boolean existsByBaseUrl(String baseUrl);

    boolean existsByBaseUrlAndIdNot(String baseUrl, Long id);
}
