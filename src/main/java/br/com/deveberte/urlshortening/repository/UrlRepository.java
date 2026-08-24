package br.com.deveberte.urlshortening.repository;

import br.com.deveberte.urlshortening.domain.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByUrl(String url);
    Optional<Link> findFirstByShortCode(String shortCode);
}
