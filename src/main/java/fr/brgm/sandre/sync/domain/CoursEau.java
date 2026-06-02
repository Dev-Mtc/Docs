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
@Table(name = "ref_cours_eau")
public class CoursEau {

    @Id
    @Column(name = "cd_entite_hydrographique", length = 50, nullable = false)
    private String cdEntiteHydrographique;

    @Column(name = "nom_entite_hydrographique", length = 500)
    private String nomEntiteHydrographique;

    @Column(name = "st_entite_hydrographique", length = 50)
    private String stEntiteHydrographique;

    @Column(name = "date_cre_entite_hydrographique")
    private LocalDateTime dateCreEntiteHydrographique;

    @Column(name = "date_maj_entite_hydrographique")
    private LocalDateTime dateMajEntiteHydrographique;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    public String getCdEntiteHydrographique() { return cdEntiteHydrographique; }
    public void setCdEntiteHydrographique(String v) { this.cdEntiteHydrographique = v; }
    public String getNomEntiteHydrographique() { return nomEntiteHydrographique; }
    public void setNomEntiteHydrographique(String v) { this.nomEntiteHydrographique = v; }
    public String getStEntiteHydrographique() { return stEntiteHydrographique; }
    public void setStEntiteHydrographique(String v) { this.stEntiteHydrographique = v; }
    public LocalDateTime getDateCreEntiteHydrographique() { return dateCreEntiteHydrographique; }
    public void setDateCreEntiteHydrographique(LocalDateTime v) { this.dateCreEntiteHydrographique = v; }
    public LocalDateTime getDateMajEntiteHydrographique() { return dateMajEntiteHydrographique; }
    public void setDateMajEntiteHydrographique(LocalDateTime v) { this.dateMajEntiteHydrographique = v; }
    public JsonNode getRawData() { return rawData; }
    public void setRawData(JsonNode v) { this.rawData = v; }
}
