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
@Table(name = "ref_intervenant")
public class Intervenant {

    @Id
    @Column(name = "cd_intervenant", length = 50, nullable = false)
    private String cdIntervenant;

    @Column(name = "nom_intervenant", length = 500)
    private String nomIntervenant;

    @Column(name = "mn_intervenant", length = 100)
    private String mnIntervenant;

    @Column(name = "st_intervenant", length = 50)
    private String stIntervenant;

    @Column(name = "auteur_intervenant", length = 500)
    private String auteurIntervenant;

    @Column(name = "date_creation_intervenant")
    private LocalDateTime dateCreationIntervenant;

    @Column(name = "date_maj_intervenant")
    private LocalDateTime dateMajIntervenant;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    public String getCdIntervenant() { return cdIntervenant; }
    public void setCdIntervenant(String v) { this.cdIntervenant = v; }
    public String getNomIntervenant() { return nomIntervenant; }
    public void setNomIntervenant(String v) { this.nomIntervenant = v; }
    public String getMnIntervenant() { return mnIntervenant; }
    public void setMnIntervenant(String v) { this.mnIntervenant = v; }
    public String getStIntervenant() { return stIntervenant; }
    public void setStIntervenant(String v) { this.stIntervenant = v; }
    public String getAuteurIntervenant() { return auteurIntervenant; }
    public void setAuteurIntervenant(String v) { this.auteurIntervenant = v; }
    public LocalDateTime getDateCreationIntervenant() { return dateCreationIntervenant; }
    public void setDateCreationIntervenant(LocalDateTime v) { this.dateCreationIntervenant = v; }
    public LocalDateTime getDateMajIntervenant() { return dateMajIntervenant; }
    public void setDateMajIntervenant(LocalDateTime v) { this.dateMajIntervenant = v; }
    public JsonNode getRawData() { return rawData; }
    public void setRawData(JsonNode v) { this.rawData = v; }
}
