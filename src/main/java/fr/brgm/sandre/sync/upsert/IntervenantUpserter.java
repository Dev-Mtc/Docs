package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;
import fr.brgm.sandre.sync.domain.Intervenant;
import fr.brgm.sandre.sync.domain.Referentiel;
import fr.brgm.sandre.sync.repository.IntervenantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class IntervenantUpserter implements ReferentielUpserter {

    private final IntervenantRepository repository;

    public IntervenantUpserter(IntervenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Referentiel referentiel() {
        return Referentiel.INTERVENANT;
    }

    @Override
    @Transactional
    public int upsert(List<JsonNode> items) {
        List<Intervenant> entities = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            String cd = JsonReader.text(item, "CdIntervenant");
            if (cd == null) continue;
            Intervenant i = repository.findById(cd).orElseGet(Intervenant::new);
            i.setCdIntervenant(cd);
            i.setNomIntervenant(JsonReader.text(item, "NomIntervenant"));
            i.setMnIntervenant(JsonReader.text(item, "MnIntervenant"));
            i.setStIntervenant(JsonReader.text(item, "StIntervenant"));
            i.setAuteurIntervenant(JsonReader.text(item, "AuteurIntervenant"));
            i.setDateCreationIntervenant(JsonReader.dateTime(item, "DateCreationIntervenant"));
            i.setDateMajIntervenant(JsonReader.dateTime(item, "DateMajIntervenant"));
            i.setRawData(item);
            entities.add(i);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
