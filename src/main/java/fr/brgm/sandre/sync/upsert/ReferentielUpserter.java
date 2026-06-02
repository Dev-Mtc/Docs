package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.Referentiel;

import java.util.List;

/**
 * Stratégie d'upsert pour un référentiel donné.
 *
 * <p>L'implémentation est responsable de transformer le JSON brut renvoyé par l'API en entité
 * JPA puis d'effectuer un insert/update par clé naturelle SANDRE.</p>
 */
public interface ReferentielUpserter {

    Referentiel referentiel();

    /**
     * Upserts les éléments donnés et renvoie le nombre effectivement persisté.
     */
    int upsert(List<JsonNode> items);
}
