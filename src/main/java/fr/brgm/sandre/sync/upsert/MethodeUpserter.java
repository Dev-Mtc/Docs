package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.Methode;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.repository.MethodeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class MethodeUpserter implements ReferentielUpserter {

    private final MethodeRepository repository;

    public MethodeUpserter(MethodeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Referentiel referentiel() {
        return Referentiel.METHODE;
    }

    @Override
    @Transactional
    public int upsert(List<JsonNode> items) {
        List<Methode> entities = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            String cd = JsonReader.text(item, "CdMethode");
            if (cd == null) continue;
            Methode m = repository.findById(cd).orElseGet(Methode::new);
            m.setCdMethode(cd);
            m.setNomMethode(JsonReader.text(item, "NomMethode"));
            m.setStMethode(JsonReader.text(item, "StMethode"));
            m.setAuteurMethode(JsonReader.text(item, "AuteurMethode"));
            m.setDateCreationMethode(JsonReader.dateTime(item, "DateCreationMethode"));
            m.setDateMajMethode(JsonReader.dateTime(item, "DateMajMethode"));
            m.setRawData(item);
            entities.add(m);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
