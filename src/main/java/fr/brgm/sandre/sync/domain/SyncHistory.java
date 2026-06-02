package fr.brgm.sandre.sync.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_history", schema = "public")
public class SyncHistory {

    public enum Statut { STARTED, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @Column(name = "referentiel", length = 50, nullable = false)
    private String referentiel;

    @Column(name = "statut", length = 20, nullable = false)
    private String statut;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "nb_recus")
    private Integer nbRecus;

    @Column(name = "nb_upsert")
    private Integer nbUpsert;

    @Column(name = "derniere_date_maj")
    private LocalDateTime derniereDateMaj;

    @Column(name = "message_erreur", columnDefinition = "text")
    private String messageErreur;

    @Column(name = "duree_ms")
    private Long dureeMs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getJobExecutionId() { return jobExecutionId; }
    public void setJobExecutionId(Long v) { this.jobExecutionId = v; }
    public String getReferentiel() { return referentiel; }
    public void setReferentiel(String v) { this.referentiel = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { this.statut = v; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime v) { this.startedAt = v; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime v) { this.finishedAt = v; }
    public Integer getNbRecus() { return nbRecus; }
    public void setNbRecus(Integer v) { this.nbRecus = v; }
    public Integer getNbUpsert() { return nbUpsert; }
    public void setNbUpsert(Integer v) { this.nbUpsert = v; }
    public LocalDateTime getDerniereDateMaj() { return derniereDateMaj; }
    public void setDerniereDateMaj(LocalDateTime v) { this.derniereDateMaj = v; }
    public String getMessageErreur() { return messageErreur; }
    public void setMessageErreur(String v) { this.messageErreur = v; }
    public Long getDureeMs() { return dureeMs; }
    public void setDureeMs(Long v) { this.dureeMs = v; }
}
