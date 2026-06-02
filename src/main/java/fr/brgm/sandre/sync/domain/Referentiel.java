package fr.brgm.sandre.sync.domain;

/**
 * Descripteur d'un référentiel SANDRE.
 *
 * <p>Les codes URL, clés naturelles et champs de date de mise à jour ont été déduits d'appels
 * réels à l'API ({@code limit=1} sur chaque endpoint). Ils correspondent au schéma SANDRE
 * effectivement renvoyé en juin 2026.</p>
 */
public enum Referentiel {

    PARAMETRE("par", "Parametre", "CdParametre", "DateMajParametre"),
    METHODE("met", "Methode", "CdMethode", "DateMajMethode"),
    COURS_EAU("cea", "CoursEau", "CdEntiteHydrographique", "DateMajEntiteHydrographique"),
    OBSTACLE("obs", "ObstEcoul", "CdObstEcoul", "DateMAJObstEcoul"),
    INTERVENANT("int", "Intervenant", "CdIntervenant", "DateMajIntervenant");

    private final String urlCode;
    private final String itemWrapperKey;
    private final String naturalKeyField;
    private final String dateMajField;

    Referentiel(String urlCode, String itemWrapperKey, String naturalKeyField, String dateMajField) {
        this.urlCode = urlCode;
        this.itemWrapperKey = itemWrapperKey;
        this.naturalKeyField = naturalKeyField;
        this.dateMajField = dateMajField;
    }

    public String urlCode() {
        return urlCode;
    }

    public String itemWrapperKey() {
        return itemWrapperKey;
    }

    public String naturalKeyField() {
        return naturalKeyField;
    }

    public String dateMajField() {
        return dateMajField;
    }
}
