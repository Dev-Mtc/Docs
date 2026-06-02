package fr.brgm.sandre.sync.config;

import fr.brgm.sandre.sync.api.SandreApiClient;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.history.SyncHistoryService;
import fr.brgm.sandre.sync.job.JobCompletionListener;
import fr.brgm.sandre.sync.job.ReferentielTasklet;
import fr.brgm.sandre.sync.upsert.ReferentielUpserter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BatchConfig {

    private final SandreApiClient apiClient;
    private final SyncHistoryService historyService;
    private final Map<Referentiel, ReferentielUpserter> upserters;

    public BatchConfig(SandreApiClient apiClient,
                       SyncHistoryService historyService,
                       List<ReferentielUpserter> upsertersList) {
        this.apiClient = apiClient;
        this.historyService = historyService;
        this.upserters = new EnumMap<>(Referentiel.class);
        for (ReferentielUpserter u : upsertersList) {
            this.upserters.put(u.referentiel(), u);
        }
    }

    @Bean
    public Job sandreSyncJob(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             JobCompletionListener listener) {
        JobBuilder jb = new JobBuilder("sandreSyncJob", jobRepository)
                .listener(listener);

        Step first = buildStep(Referentiel.PARAMETRE, jobRepository, txManager);
        var flow = jb.start(first);
        for (Referentiel r : Referentiel.values()) {
            if (r == Referentiel.PARAMETRE) continue;
            flow = flow.next(buildStep(r, jobRepository, txManager));
        }
        return flow.build();
    }

    private Step buildStep(Referentiel referentiel,
                           JobRepository jobRepository,
                           PlatformTransactionManager txManager) {
        ReferentielUpserter upserter = upserters.get(referentiel);
        if (upserter == null) {
            throw new IllegalStateException("Aucun ReferentielUpserter pour " + referentiel);
        }
        ReferentielTasklet tasklet = new ReferentielTasklet(referentiel, apiClient, upserter, historyService);
        return new StepBuilder("step-" + referentiel.urlCode(), jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }
}
