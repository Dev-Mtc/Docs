package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.Parametre;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.repository.ParametreRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParametreUpserter implements ReferentielUpserter {

    private final ParametreRepository repository;

    public ParametreUpserter(ParametreRepository repository) {
        this.repository = repository;
    }

    @Override
    public Referentiel referentiel() {
        return Referentiel.PARAMETRE;
    }

    @Override
    @Transactional
    public int upsert(List<JsonNode> items) {
        List<Parametre> entities = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            String cd = JsonReader.text(item, "CdParametre");
            if (cd == null) continue;
            Parametre p = repository.findById(cd).orElseGet(Parametre::new);
            p.setCdParametre(cd);
            p.setNomParametre(JsonReader.text(item, "NomParametre"));
            p.setStParametre(JsonReader.text(item, "StParametre"));
            p.setAuteurParametre(JsonReader.text(item, "AuteurParametre"));
            p.setDateCreationParametre(JsonReader.dateTime(item, "DateCreationParametre"));
            p.setDateMajParametre(JsonReader.dateTime(item, "DateMajParametre"));
            p.setRawData(item);
            entities.add(p);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
