package fr.brgm.sandre.sync.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Dégrade le statut global du Job en {@link BatchStatus#FAILED} si au moins un Step (référentiel)
 * a stocké une marque d'échec dans le contexte d'exécution. Le code de sortie sera ≠ 0,
 * conformément à la convention attendue par l'ordonnanceur externe.
 */
@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(JobCompletionListener.class);

    public static final String FAILED_REF_KEY_PREFIX = "sandre.failed.";

    @Override
    public void afterJob(JobExecution jobExecution) {
        Map<String, Object> entries = jobExecution.getExecutionContext().toMap();
        long failures = entries.keySet().stream()
                .filter(k -> k.startsWith(FAILED_REF_KEY_PREFIX))
                .count();

        if (failures > 0) {
            LOG.error("Job sandreSyncJob terminé avec {} référentiel(s) en échec, statut global=FAILED",
                    failures);
            jobExecution.setStatus(BatchStatus.FAILED);
            jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(
                    "Échec sur " + failures + " référentiel(s)"));
        } else {
            LOG.info("Job sandreSyncJob terminé : tous les référentiels OK");
        }
    }
}
