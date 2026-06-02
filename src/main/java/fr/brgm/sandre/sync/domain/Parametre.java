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
@Table(name = "ref_parametre")
public class Parametre {

    @Id
    @Column(name = "cd_parametre", length = 50, nullable = false)
    private String cdParametre;

    @Column(name = "nom_parametre", length = 500)
    private String nomParametre;

    @Column(name = "st_parametre", length = 50)
    private String stParametre;

    @Column(name = "auteur_parametre", length = 500)
    private String auteurParametre;

    @Column(name = "date_creation_parametre")
    private LocalDateTime dateCreationParametre;

    @Column(name = "date_maj_parametre")
    private LocalDateTime dateMajParametre;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    public String getCdParametre() { return cdParametre; }
    public void setCdParametre(String v) { this.cdParametre = v; }
    public String getNomParametre() { return nomParametre; }
    public void setNomParametre(String v) { this.nomParametre = v; }
    public String getStParametre() { return stParametre; }
    public void setStParametre(String v) { this.stParametre = v; }
    public String getAuteurParametre() { return auteurParametre; }
    public void setAuteurParametre(String v) { this.auteurParametre = v; }
    public LocalDateTime getDateCreationParametre() { return dateCreationParametre; }
    public void setDateCreationParametre(LocalDateTime v) { this.dateCreationParametre = v; }
    public LocalDateTime getDateMajParametre() { return dateMajParametre; }
    public void setDateMajParametre(LocalDateTime v) { this.dateMajParametre = v; }
    public JsonNode getRawData() { return rawData; }
    public void setRawData(JsonNode v) { this.rawData = v; }
}
