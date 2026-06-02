package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.CoursEau;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.repository.CoursEauRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class CoursEauUpserter implements ReferentielUpserter {

    private final CoursEauRepository repository;

    public CoursEauUpserter(CoursEauRepository repository) {
        this.repository = repository;
    }

    @Override
    public Referentiel referentiel() {
        return Referentiel.COURS_EAU;
    }

    @Override
    @Transactional
    public int upsert(List<JsonNode> items) {
        List<CoursEau> entities = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            String cd = JsonReader.text(item, "CdEntiteHydrographique");
            if (cd == null) continue;
            CoursEau c = repository.findById(cd).orElseGet(CoursEau::new);
            c.setCdEntiteHydrographique(cd);
            c.setNomEntiteHydrographique(JsonReader.text(item, "NomEntiteHydrographique"));
            c.setStEntiteHydrographique(JsonReader.text(item, "StEntiteHydrographique"));
            c.setDateCreEntiteHydrographique(JsonReader.dateTime(item, "DateCreEntiteHydrographique"));
            c.setDateMajEntiteHydrographique(JsonReader.dateTime(item, "DateMajEntiteHydrographique"));
            c.setRawData(item);
            entities.add(c);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
