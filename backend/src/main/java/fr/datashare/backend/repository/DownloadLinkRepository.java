package fr.datashare.backend.repository;

import fr.datashare.backend.entity.DownloadLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DownloadLinkRepository
        extends JpaRepository<DownloadLink, Long> {

    Optional<DownloadLink> findByToken(String token);

    boolean existsByToken(String token);

}
