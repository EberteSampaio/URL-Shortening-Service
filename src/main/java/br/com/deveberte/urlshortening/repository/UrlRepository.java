package br.com.deveberte.urlshortening.repository;

import br.com.deveberte.urlshortening.domain.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByUrl(String url);
    Optional<Link> findFirstByShortCode(String shortCode);

    @Transactional
    @Modifying
    @Query("update Link  l set l.accessCount = l.accessCount + 1 where  l.shortCode = :shortCode")
    int incrementAccesCount(@Param("shortCode") String shortCode);
}
