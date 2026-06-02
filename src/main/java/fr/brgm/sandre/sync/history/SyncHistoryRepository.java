package fr.brgm.sandre.sync.history;

import fr.brgm.sandre.sync.domain.SyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    @Query("""
           select max(h.startedAt)
           from SyncHistory h
           where h.referentiel = :referentiel
             and h.statut = 'SUCCESS'
           """)
    Optional<LocalDateTime> findLastSuccessStartedAt(@Param("referentiel") String referentiel);
}
