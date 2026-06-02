package fr.brgm.sandre.sync.history;

import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.domain.SyncHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Gestion de l'historique de synchronisation. Chaque méthode tourne dans une transaction
 * indépendante ({@link Propagation#REQUIRES_NEW}) afin que les lignes d'historique soient
 * commitées même si la transaction d'upsert du référentiel a échoué.
 */
@Service
public class SyncHistoryService {

    private final SyncHistoryRepository repository;

    public SyncHistoryService(SyncHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncHistory start(Referentiel referentiel, Long jobExecutionId, Optional<LocalDateTime> derniereDateMaj) {
        SyncHistory h = new SyncHistory();
        h.setReferentiel(referentiel.name());
        h.setJobExecutionId(jobExecutionId);
        h.setStatut(SyncHistory.Statut.STARTED.name());
        h.setStartedAt(LocalDateTime.now());
        h.setDerniereDateMaj(derniereDateMaj.orElse(null));
        return repository.save(h);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(SyncHistory h, int nbRecus, int nbUpsert) {
        LocalDateTime end = LocalDateTime.now();
        h.setStatut(SyncHistory.Statut.SUCCESS.name());
        h.setFinishedAt(end);
        h.setNbRecus(nbRecus);
        h.setNbUpsert(nbUpsert);
        h.setDureeMs(Duration.between(h.getStartedAt(), end).toMillis());
        repository.save(h);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(SyncHistory h, String messageErreur) {
        LocalDateTime end = LocalDateTime.now();
        h.setStatut(SyncHistory.Statut.FAILED.name());
        h.setFinishedAt(end);
        h.setMessageErreur(truncate(messageErreur));
        h.setDureeMs(Duration.between(h.getStartedAt(), end).toMillis());
        repository.save(h);
    }

    /**
     * Date de démarrage de la dernière synchro réussie pour ce référentiel.
     * {@link Optional#empty()} pour le premier run (synchro complète).
     */
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> derniereDateMajReussie(Referentiel referentiel) {
        return repository.findLastSuccessStartedAt(referentiel.name());
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > 8000 ? s.substring(0, 8000) : s;
    }
}
