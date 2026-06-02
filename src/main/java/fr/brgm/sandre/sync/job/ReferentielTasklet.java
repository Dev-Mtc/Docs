package fr.brgm.sandre.sync.job;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.api.SandreApiClient;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.domain.SyncHistory;
import fr.brgm.sandre.sync.history.SyncHistoryService;
import fr.brgm.sandre.sync.upsert.ReferentielUpserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tasklet générique : exécute le cycle complet de synchro d'un référentiel.
 *
 * <ol>
 *   <li>crée une ligne historique {@code STARTED}</li>
 *   <li>lit la date de dernière synchro réussie</li>
 *   <li>appelle l'API SANDRE (avec pagination)</li>
 *   <li>upsert par clé naturelle, en transaction propre au référentiel</li>
 *   <li>met l'historique à {@code SUCCESS} ou {@code FAILED}</li>
 * </ol>
 *
 * <p>En cas d'échec, le tasklet attrape l'exception, marque l'historique en {@code FAILED},
 * place une marque {@code "stepFailed"} dans le contexte du Job et termine néanmoins en
 * {@link RepeatStatus#FINISHED} pour laisser les Steps suivants s'exécuter. Le statut global
 * du Job est dégradé en {@code FAILED} par {@link JobCompletionListener}.</p>
 */
public class ReferentielTasklet implements Tasklet {

    private static final Logger LOG = LoggerFactory.getLogger(ReferentielTasklet.class);

    private final Referentiel referentiel;
    private final SandreApiClient apiClient;
    private final ReferentielUpserter upserter;
    private final SyncHistoryService historyService;

    public ReferentielTasklet(Referentiel referentiel,
                              SandreApiClient apiClient,
                              ReferentielUpserter upserter,
                              SyncHistoryService historyService) {
        this.referentiel = referentiel;
        this.apiClient = apiClient;
        this.upserter = upserter;
        this.historyService = historyService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getId();

        Optional<LocalDateTime> derniereDateMaj = historyService.derniereDateMajReussie(referentiel);
        SyncHistory hist = historyService.start(referentiel, jobExecutionId, derniereDateMaj);

        LOG.info("Synchro référentiel {} : début ({}, jobExecutionId={})",
                referentiel.name(),
                derniereDateMaj.isPresent() ? "delta depuis " + derniereDateMaj.get() : "complète (premier run)",
                jobExecutionId);

        try {
            List<JsonNode> items = apiClient.recuperer(
                    referentiel,
                    derniereDateMaj.map(LocalDateTime::toLocalDate));
            int nbRecus = items.size();
            int nbUpsert = upserter.upsert(items);
            historyService.success(hist, nbRecus, nbUpsert);
            LOG.info("Synchro référentiel {} : SUCCESS (reçus={}, upsert={}, durée={} ms)",
                    referentiel.name(), nbRecus, nbUpsert, hist.getDureeMs());
            return RepeatStatus.FINISHED;
        } catch (Exception ex) {
            String message = ex.getClass().getSimpleName() + " : " + ex.getMessage();
            historyService.failed(hist, stack(ex));
            LOG.error("Synchro référentiel {} : FAILED ({})", referentiel.name(), message, ex);
            contribution.getStepExecution().getJobExecution().getExecutionContext()
                    .put(JobCompletionListener.FAILED_REF_KEY_PREFIX + referentiel.name(), message);
            // On termine le step en FINISHED malgré l'échec : la "vraie" décision de statut
            // global est portée par JobCompletionListener pour ne pas court-circuiter
            // l'exécution des référentiels suivants.
            return RepeatStatus.FINISHED;
        }
    }

    private static String stack(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
