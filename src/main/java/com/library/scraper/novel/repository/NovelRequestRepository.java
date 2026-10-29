package com.library.scraper.novel.repository;

import com.library.scraper.novel.model.NovelRequest;
import com.library.scraper.novel.model.NovelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NovelRequestRepository extends JpaRepository<NovelRequest, Long> {

    Optional<NovelRequest> findByUrl(String url);

    List<NovelRequest> findAllByOrderByIdDesc();

    List<NovelRequest> findByStatusIn(Collection<NovelRequestStatus> statuses);
}
