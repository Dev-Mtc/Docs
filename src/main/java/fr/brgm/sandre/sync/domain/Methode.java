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
@Table(name = "ref_methode")
public class Methode {

    @Id
    @Column(name = "cd_methode", length = 50, nullable = false)
    private String cdMethode;

    @Column(name = "nom_methode", length = 500)
    private String nomMethode;

    @Column(name = "st_methode", length = 50)
    private String stMethode;

    @Column(name = "auteur_methode", length = 500)
    private String auteurMethode;

    @Column(name = "date_creation_methode")
    private LocalDateTime dateCreationMethode;

    @Column(name = "date_maj_methode")
    private LocalDateTime dateMajMethode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    public String getCdMethode() { return cdMethode; }
    public void setCdMethode(String v) { this.cdMethode = v; }
    public String getNomMethode() { return nomMethode; }
    public void setNomMethode(String v) { this.nomMethode = v; }
    public String getStMethode() { return stMethode; }
    public void setStMethode(String v) { this.stMethode = v; }
    public String getAuteurMethode() { return auteurMethode; }
    public void setAuteurMethode(String v) { this.auteurMethode = v; }
    public LocalDateTime getDateCreationMethode() { return dateCreationMethode; }
    public void setDateCreationMethode(LocalDateTime v) { this.dateCreationMethode = v; }
    public LocalDateTime getDateMajMethode() { return dateMajMethode; }
    public void setDateMajMethode(LocalDateTime v) { this.dateMajMethode = v; }
    public JsonNode getRawData() { return rawData; }
    public void setRawData(JsonNode v) { this.rawData = v; }
}
