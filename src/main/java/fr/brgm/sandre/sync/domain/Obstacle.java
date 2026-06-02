package fr.brgm.sandre.sync.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ref_obstacle")
public class Obstacle {

    @Id
    @Column(name = "cd_obst_ecoul", length = 50, nullable = false)
    private String cdObstEcoul;

    @Column(name = "nom_principal_obst_ecoul", length = 500)
    private String nomPrincipalObstEcoul;

    @Column(name = "st_obst_ecoul", length = 50)
    private String stObstEcoul;

    @Column(name = "date_valid_obst_ecoul")
    private LocalDateTime dateValidObstEcoul;

    @Column(name = "date_maj_obst_ecoul")
    private LocalDateTime dateMajObstEcoul;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    public String getCdObstEcoul() { return cdObstEcoul; }
    public void setCdObstEcoul(String v) { this.cdObstEcoul = v; }
    public String getNomPrincipalObstEcoul() { return nomPrincipalObstEcoul; }
    public void setNomPrincipalObstEcoul(String v) { this.nomPrincipalObstEcoul = v; }
    public String getStObstEcoul() { return stObstEcoul; }
    public void setStObstEcoul(String v) { this.stObstEcoul = v; }
    public LocalDateTime getDateValidObstEcoul() { return dateValidObstEcoul; }
    public void setDateValidObstEcoul(LocalDateTime v) { this.dateValidObstEcoul = v; }
    public LocalDateTime getDateMajObstEcoul() { return dateMajObstEcoul; }
    public void setDateMajObstEcoul(LocalDateTime v) { this.dateMajObstEcoul = v; }
    public JsonNode getRawData() { return rawData; }
    public void setRawData(JsonNode v) { this.rawData = v; }
}
